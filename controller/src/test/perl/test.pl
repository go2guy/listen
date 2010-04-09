#!/usr/bin/perl

use strict;
use Getopt::Std;
use Listen;
use ListenBackup;
use ListenRestore;
use XML::Simple;
use REST::Client;

my (%options);

getopts("h:p:t:d", \%options);

check_options();

sub check_options {
   if (%options->{h} eq undef) {
      print "You need to provide a host to send requests to\n\n\n";
      usage();
      exit;
   }
}

sub usage {
   print STDOUT "usage: test.pl -h <host> -p <port> -t <timeout>\n";
   print STDOUT "\n\t\t -p <port> is optional. If your host requires a port, use this\n";
   print STDOUT "\n\t\t -t <timeout> is optional and defaults to 5.\n";
}

my @connection = (%options->{h}, %options->{p}, %options->{t}, %options->{d});

my ($subid, $conid, $parid, $voiid);
my $startvalue;
### Inserts
for (my $cnt = 14024750789; $cnt < 14024750791; ++$cnt) {
   $subid = listenInsert("subscriber", @connection, $cnt, "/interact/audio/greeting/", undef);
   if ($subid <= 0) { print STDOUT "Error processing subscriber request. [$subid] Exiting.\n"; last;}

   if (!$startvalue) { $startvalue = $subid; }

   $voiid = listenInsert("voicemail", @connection, $subid, "2009-10-11T00:00:00.000", "/interact/audio/123.wav", "true");
   if ($voiid <= 0) { print STDOUT "Error processing voicemail request. [$voiid] Exiting.\n"; last;}

   $conid = listenInsert("conference", @connection, $cnt, 123, "false");
   if ($conid <= 0) { print STDOUT "Error processing conference request. [$conid] Exiting.\n"; last;}

   $parid = listenInsert("participant", @connection, $cnt, $conid, "/interact/audio/123.wav", "false", "false", "false", "true", $cnt);
   if ($parid <= 0) { print STDOUT "Error processing participant request. [$parid] Exiting.\n"; last;}
}

print "DONE Inserts\n";

### Updates
my $subcount   = $startvalue;
my $voicecount = $startvalue;
my $concount   = $startvalue;
my $partcount  = $startvalue;
my $rc;
for (my $cnt = 14034750789; $cnt < 14034750791; ++$cnt) {
   $rc = listenUpdate("subscriber", @connection, $subcount, $cnt);
   if ($rc <= 0) { print STDOUT "Error updating subscriber $subcount number $cnt. [$rc] Exiting.\n";}
   $rc = listenUpdate("voicemail", @connection, $voicecount++, $subcount++, "2010-03-01T00:00:00.001", "/interact/audio/456.wav", "false");
   if ($rc <= 0) { print STDOUT "Error updating voicemail " . ($voicecount - 1) . " subscriber " . ($subcount - 1) . ". [$rc] Exiting.\n";}
   $rc = listenUpdate("conference", @connection, $concount, $cnt, 666, "true");
   if ($rc <= 0) { print STDOUT "Error updating conference $concount number $cnt. [$rc] Exiting.\n";}
   $rc = listenUpdate("participant", @connection, $partcount++, $cnt, $concount++, "/interact/audio/999.wav", "false", "true", "true", "false", ($cnt * 3));
   if ($rc <= 0) { print STDOUT "Error updating participant " . ($partcount - 1) .  " number $cnt conference " . ($concount - 1) . ". [$rc] Exiting.\n";}
}

print "DONE Updates\n";
my @results = listenGetIDs("voicemail", @connection);

foreach my $id (@results) {
   my $rc = listenDelete("voicemail", @connection, $id);

   if ($rc != 0) { print "Error Deleting voicemail $id Error [$rc].\n"; }
}

print "DONE voicemail deletes\n";
@results = listenGetIDs("subscriber", @connection);
foreach my $id (@results) {
   my $rc = listenDelete("subscriber", @connection, $id);

   if ($rc != 0) { print "Error Deleting subscriber $id Error [$rc].\n"; }
}

print "DONE subscriber deletes\n";
@results = listenGetIDs("participant", @connection);
foreach my $id (@results) {
   my $rc = listenDelete("participant", @connection, $id);

   if ($rc != 0) { print "Error Deleting participant $id Error [$rc].\n"; }
}

print "DONE participant deletes\n";
@results = listenGetIDs("conference", @connection);
foreach my $id (@results) {
   my $rc = listenDelete("conference", @connection, $id);

   if ($rc != 0) { print "Error Deleting subscriber $id Error [$rc].\n"; }
}

print "DONE conference deletes\n";
print "\nDONE deletes\n";
