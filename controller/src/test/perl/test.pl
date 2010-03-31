#!/usr/bin/perl

use strict;
use Getopt::Std;
use Listen;
use ListenBackup;
use ListenRestore;

my (%options);

getopt("hptd", \%options);

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


### Inserts
for (my $cnt = 14024750789; $cnt < 14024750889; ++$cnt) {
   insertTest("subscriber", @connection, $cnt);
}
#my $subid = insertTest("subscriber", @connection, 14024750789);
#my $conid = insertTest("conference", @connection, 14024750789, 123, "false");
#my $parid = insertTest("participant", @connection, 14024750789, 1, "/interact/audio/123.wav", "false", "false", "true", 987654);
#my $voiid = insertTest("voicemail", @connection, $subid, "2009-10-11T00:00:00.000", "/interact/audio/123.wav", "true");

#my $subid = insertTest("subscriber", @connection, 14024750799);

#print "Subid = $subid\n";

#deleteTest("subscriber", @connection, $subid);

### Updates
#updateTest("subscriber", @connection, $subid, 14023158299);

### Backup test. This does not do a delete so you
### will need to handle clearing out the tables.
### Easiest way would be to restart the listener.
#backupRequest("subscriber", "blah");

####Restore test
#loadRequest("subscriber", "blah");
print "\nDONE\n";
