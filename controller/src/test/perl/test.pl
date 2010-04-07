#!/usr/bin/perl

use strict;
use Getopt::Std;
use Listen;
use ListenBackup;
use ListenRestore;

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
### Inserts
for (my $cnt = 14024750789; $cnt < 14024750791; ++$cnt) {
   $subid = listenInsert("subscriber", @connection, $cnt, "/interact/audio/greeting/");
   if ($subid <= 0) { print STDOUT "Error processing subscriber request. [$subid] Exiting.\n"; last;}
   $voiid = listenInsert("voicemail", @connection, $subid, "2009-10-11T00:00:00.000", "/interact/audio/123.wav", "true");
   if ($voiid <= 0) { print STDOUT "Error processing voicemail request. [$voiid] Exiting.\n"; last;}

   $conid = listenInsert("conference", @connection, $cnt, 123, "false");
   if ($conid <= 0) { print STDOUT "Error processing conference request. [$conid] Exiting.\n"; last;}
   $parid = listenInsert("participant", @connection, $cnt, 1, "/interact/audio/123.wav", "false", "false", "false", "true", $cnt);
   if ($parid <= 0) { print STDOUT "Error processing participant request. [$parid] Exiting.\n"; last;}
}

#print "Subid = $subid\n";

#listenUpdate("subscriber", @connection, $subid, 14024750789);
listenUpdate("subscriber", @connection, 2, 14024750789);
#listenDelete("subscriber", @connection, $subid);
### Updates

print "\nDONE\n";
