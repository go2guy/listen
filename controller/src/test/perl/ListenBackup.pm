use strict;

package ListenBackup;
require Exporter;

our @ISA = qw (Exporter);
our @EXPORT = qw(backupRequest);

use REST::Client;
use XML::Simple qw(:strict);
use Data::Dumper;

my $filedate;

sub backupRequest {

   my ($reqtype, $filename) = @_;

   print "Input = $reqtype $filename\n";

   my $client = REST::Client->new ({
              host => 'kevin_scp2:8080',
              timeout => 5,
              });

   $client->GET("/$reqtype"."s");

   my $reqlist = $client->responseContent();

   my $xs = XML::Simple->new(KeyAttr => 'subscribers',ForceArray=>1,KeepRoot => 1);
   my $ref = $xs->XMLin($reqlist);

   #print Dumper($ref);

   my $count = $ref->{subscribers}[0]->{count};
   my $total = $ref->{subscribers}[0]->{total};
   my $next = $ref->{subscribers}[0]->{next};

   if ($count > 0) {

      open(OUTFILE, ">$filename") || die "Can't open [$filename] $!\n";

      my $done = 0;

      while (!$done) {

         for (my $x = 0; $x < $count; ++$x) {
            $client->GET($ref->{subscribers}[0]->{subscriber}[$x]->{href});
            print OUTFILE $client->responseContent() . "\n";
         }

         if ($next eq undef) {
            $done = 1;
         }
         else {
            $client->GET($next);
            $reqlist = $client->responseContent();
            $ref = $xs->XMLin($reqlist);
            $count = $ref->{subscribers}[0]->{count};
            $total = $ref->{subscribers}[0]->{total};
            $next = $ref->{subscribers}[0]->{next};
            print "count = $count total = $total next = $next\n";
         }

      }
      close(INFILE);
   }
}

1;
