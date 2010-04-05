use strict;

package ListenBackup;
require Exporter;

use constant TRUE => 1;
use constant FALSE => 0;

our @ISA = qw (Exporter);
our @EXPORT = qw(Backup BackupSubscriber);

use REST::Client;
use XML::Simple qw(:strict);
use Data::Dumper;

my $ListenClient;
my $DEBUG = FALSE;

sub setListenClient {

   my ($host, $port, $timeout, $debug) = @_;
   my $sendto;

   if ($timeout eq undef) { $timeout = 5; }
   if ($debug) { $DEBUG = TRUE; }

   if ($DEBUG) {
      print "host = $host port = $port timeout = $timeout\n";
   }

   if ($port eq undef) { $sendto = "http://$host"; }
   else { $sendto = "http://$host:$port"; }

   $ListenClient = REST::Client->new({
                 host => $sendto,
                 timeout => $timeout,
                 });
}

sub BackupSubscriber {

   my ($host, $port, $timeout, $debug, $number) = @_;

   setListenClient($host, $port, $timeout, $debug);

   my $reqlist = $ListenClient->GET("/subscribers?number=$number&_uniqueResult=true")->responseContent();

   if ($ListenClient->responseCode() != 200) {
      print STDOUT "Subscriber number [$number] was not found.\n";
      return 0;
   }

   print "RESULT = $reqlist CODE = ". $ListenClient->responseCode() . "\n";

   open(OUTFILE, ">$number.xml") || die "Can't open [$number.xml] $!\n";

   print OUTFILE "$reqlist\n";

   my $subxs = XML::Simple->new(KeyAttr=>"subscriber", ForceArray=>1,KeepRoot=>1);
   my $subref = $subxs->XMLin($reqlist);

   if ($DEBUG) {
      print Dumper($subref);
   }

   backupDependencies($subref->{subscriber}[0]->{voicemails}[0]->{href}, "voicemail");

   close OUTFILE;
}

sub Backup {
   my ($reqtype, $host, $port,
       $timeout, $debug, $filename) = @_;

   setListenClient($host, $port, $timeout, $debug);

   if ($DEBUG) {
      print "Input = $reqtype $filename\n";
   }

   if ($reqtype eq "subscriber" || $reqtype eq "conference") {
      backupRequest($reqtype, $filename);
   }
   else {
      print STDERR "You entered an invalid thing to backup\n";
   }
}

sub backupRequest {

   my ($reqtype, $filename) = @_;

   my $reqlist = $ListenClient->GET("/$reqtype"."s")->responseContent();

   my $xs = XML::Simple->new(KeyAttr => "$reqtype"."s",ForceArray=>1,KeepRoot => 1);
   my $ref = $xs->XMLin($reqlist);

   if ($DEBUG) {
      print Dumper($ref);
   }

   my $count = $ref->{$reqtype."s"}[0]->{count};
   my $total = $ref->{$reqtype."s"}[0]->{total};
   my $next  = $ref->{$reqtype."s"}[0]->{next};

   if ($count > 0) {

      open(OUTFILE, ">$filename") || die "Can't open [$filename] $!\n";

      my $done = 0;

      while (!$done) {

         for (my $x = 0; $x < $count; ++$x) {
            my ($response) = $ListenClient->GET($ref->{$reqtype."s"}[0]->{$reqtype}[$x]->{href})->responseContent();
            print OUTFILE "$response\n";

            my $subxs = XML::Simple->new(KeyAttr=>"$reqtype", ForceArray=>1,KeepRoot=>1);
            my $subref = $subxs->XMLin($response);

            if ($DEBUG) {
               print Dumper($subref);
            }

            if ($reqtype eq "subscriber") {
               backupDependencies($subref->{$reqtype}[0]->{voicemails}[0]->{href}, "voicemail");
            }
            elsif ($reqtype eq "conference") {
               backupDependencies($subref->{$reqtype}[0]->{participants}[0]->{href}, "participant");
            }
         }

         if ($next eq undef) {
            $done = 1;
         }
         else {
            $ListenClient->GET($next);
            $reqlist = $ListenClient->responseContent();
            $ref     = $xs->XMLin($reqlist);
            $count   = $ref->{subscribers}[0]->{count};
            $total   = $ref->{subscribers}[0]->{total};
            $next    = $ref->{subscribers}[0]->{next};

            if ($DEBUG) {
              print "count = $count total = $total next = $next\n";
            }
         }

      }
      close(INFILE);
   }

   else {
      print STDOUT "There were no $reqtype" . "s found! Backup file was not created.\n";
   }
}

sub backupDependencies {

   my ($href, $reqtype) = @_;

   my $done = 0;
   my $reqlist = $ListenClient->GET("$href")->responseContent();

   my $xs = XML::Simple->new(KeyAttr => "$reqtype"."s",ForceArray=>1,KeepRoot => 1);
   my $ref = $xs->XMLin($reqlist);

   if ($DEBUG) {
      print STDOUT "href = $href reqtype = $reqtype\n";
      print Dumper($ref);
   }

   my $count = $ref->{$reqtype."s"}[0]->{count};
   my $total = $ref->{$reqtype."s"}[0]->{total};
   my $next  = $ref->{$reqtype."s"}[0]->{next};

   while (!$done) {
      for (my $x = 0; $x < $count; ++$x) {
         my ($response) = $ListenClient->GET($ref->{$reqtype."s"}[0]->{$reqtype}[$x]->{href})->responseContent();
         print OUTFILE "$response\n";

         my $subxs = XML::Simple->new(KeyAttr=>"$reqtype", ForceArray=>1,KeepRoot=>1);
         my $subref = $subxs->XMLin($response);
      }

      if ($next eq undef) {
         $done = 1;
      }
      else {
         $ListenClient->GET($next);
         $reqlist = $ListenClient->responseContent();
         $ref     = $xs->XMLin($reqlist);
         $count   = $ref->{subscribers}[0]->{count};
         $total   = $ref->{subscribers}[0]->{total};
         $next    = $ref->{subscribers}[0]->{next};
         if ($DEBUG) {
            print "count = $count total = $total next = $next\n";
         }
      }
   }
}

1;
