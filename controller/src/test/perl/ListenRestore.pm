use strict;

package ListenRestore;
require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(loadRequest);

use REST::Client;

my $filedate;

sub loadRequest {

   my ($reqtype, $filename) = @_;

   print "Input $reqtype $filename\n";

   my $client = REST::Client->new({
              host => 'kevin_scp2:8080',
              timeout => 5,
              });

   open(INFILE, "<$filename") || die "Can't open [$filename] $!\n";

   while (<INFILE>) {

      my ($reqxml) = $_;

      $client->POST("/$reqtype"."s", $reqxml,{CustomHeader => 'Content-Type: application/xml;charset=UTF-8'});
   }

   close(INFILE);
}

1;
