#!/usr/bin/perl

use strict;
use Getopt::Std;
use ListenBackup;
use ListenRestore;

my (%options);

getopts("brh:p:t:ds:c:n:", \%options);

check_options();

sub check_options {
   if (%options->{h} eq undef) {
      print "You need to provide a host to send requests to\n\n\n";
      usage();
      exit;
   }
   if (%options->{n}) { return; }

   if (%options->{s} eq undef && %options->{c} eq undef) {
      print "You need to provide either a subscriber and/or conference file to restore\n\n\n";
      usage();
      exit;
   }
   if (%options->{b} eq undef && %options->{r} eq undef) {
      print "You need to provide [r]estore or [b]ackup option\n\n\n";
      usage();
      exit;
   }
}

sub usage {
   print STDOUT "usage: llr.pl -<b or r> -h <host> -p <port> -t <timeout> -n <subscriber> -s <subscriber file> -c <conference file>\n";
   print STDOUT "\n\t\t -b is for backup.\n";
   print STDOUT "\n\t\t -r is for restore.\n";
   print STDOUT "\n\t\t -p <port> is optional. If your host requires a port, use this\n";
   print STDOUT "\n\t\t -t <timeout> is optional and defaults to 5.\n";
   print STDOUT "\n\t\t -d is debug.\n";
   print STDOUT "\n\t\t -n <subscriber number> this option is for backing up an individual subscriber.\n";
   print STDOUT "\t\t You will not need the -s option if using -n. The filename will be the subscribers number\n";
}

my @connection = (%options->{h}, %options->{p}, %options->{t}, %options->{d});

if (%options->{n} ne undef) {
   if (%options->{b}) {
      BackupSubscriber(@connection, %options->{n});
   }
   elsif (%options->{r}) {
      Restore("subscriber", @connection, %options->{n} . ".xml");
   }
}

elsif (%options->{s} ne undef) {
   if (%options->{b}) {
      Backup("subscriber", @connection, %options->{s});
   }
   elsif (%options->{r}) {
      Restore("subscriber", @connection, %options->{s});
   }
}

elsif (%options->{c} ne undef) {
   if (%options->{b}) {
      Backup("conference", @connection, %options->{c});
   }
   elsif (%options->{r}) {
      Restore("conference", @connection, %options->{c});
   }
}

print "\nDONE\n";
