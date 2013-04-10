#=======================================================
# Name: ListenRestore.pm
#
# Description: Library for handling tbe restore requests
#    for the Listen product.
#=======================================================
use strict;
use lib "/interact/listen/perl";

package ListenRestore;
require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(Restore);

use constant TRUE  => 1;
use constant FALSE => 0;

use REST::Client;
use XML::Simple qw(:strict);
use Data::Dumper;
use Listen;
use Log::Log4perl;

Log::Log4perl->init("./log4perl.cfg");

my $ListenClient;
my $DEBUG = FALSE;
my @CONNECTION;
my $LOGGER = Log::Log4perl->get_logger("Listen");

=head1 NAME

ListenRestore - Listen restore functions

=head1 SYNOPSIS

   use ListenRestore;
   Restore($reqtype, $host, $port, $timeout, $debug, $filename);

=head1 DESCRIPTION
  
   A collection of subroutines to facilitate the restoring of 
      listen data from files.

=head1 FUNCTIONS
=over 4

=item * Restore($reqtype, $host, $port, $timeout, $debug, $filename);

   Accepts $reqtype, $host, $port, $timeout, $debug, $filename

   Returns 0 for success, else error number
=back

=head1 AUTHOR
Kevin Murray

=head1 COPYRIGHT
Copyright 2013 NewNet Communication Technologies.

=cut

sub Restore {
   my ($reqtype, $host, $port,
       $timeout, $debug, $filename) = @_;

   setListenClient($host, $port, $timeout, $debug);

   if ($DEBUG) {
      $LOGGER->debug("Input = $reqtype $filename\n");
   }

   return (restoreRequest($reqtype, $filename));
}

sub setListenClient {

   my ($host, $port, $timeout, $debug) = @_;
   my $sendto;

   if ($timeout eq undef) { $timeout = 5; }
   if ($debug) { $DEBUG = TRUE; }

   if ($DEBUG) {
      $LOGGER->debug("host = $host port = $port timeout = $timeout\n");
   }

   if ($port eq undef) { $sendto = "http://$host"; }
   else { $sendto = "http://$host:$port"; }

   $ListenClient = REST::Client->new({
                 host => $sendto,
                 timeout => $timeout,
                 });
   @CONNECTION = ($host,$port,$timeout,$debug);
}

sub restoreRequest {

   my ($reqtype, $filename) = @_;

   open(INFILE, "<$filename") || die "Can't open [$filename] $!\n";

   my $reqid;
   while (<INFILE>) {

      my ($reqxml) = $_;

      my $xs = XML::Simple->new(KeyAttr => $reqtype."s",ForceArray => 1, KeepRoot => 1);
      my $ref = eval { $xs->XMLin($reqxml); };

      if ($@) {
         $@ = ! s/at \/.*?$//s;
         $LOGGER->error("\nERROR in '$reqxml':\n$@\n");
         return 1;
      }

      if ($DEBUG) {
         $LOGGER->debug(Dumper($ref));
      }

      if ($ref->{subscriber} ne undef) {
         my ($number)   = $ref->{$reqtype}[0]->{number}[0];
         my ($voiceloc) = $ref->{$reqtype}[0]->{voicemailGreetingLocation}[0];

         if ($DEBUG) { $LOGGER->debug("NUMBER = $number VOICE  = $voiceloc\n"); }
         $reqid = listenInsert("subscriber", @CONNECTION, $number, $voiceloc);
      }

      if ($ref->{voicemail} ne undef) {
         my ($created)  = $ref->{voicemail}[0]->{dateCreated}[0];
         my ($location) = $ref->{voicemail}[0]->{fileLocation}[0];
         my ($isnew)    = $ref->{voicemail}[0]->{isNew}[0];

         if ($DEBUG) { $LOGGER->debug("id = $reqid created = $created location = $location isnew = $isnew\n"); }
         listenInsert("voicemail", @CONNECTION, $reqid, $created, $location, $isnew);
      }

      if ($ref->{conference} ne undef) {
         my ($number)   = $ref->{conference}[0]->{number}[0];
         my ($adminpin) = $ref->{conference}[0]->{adminPin}[0];
         my ($started)  = $ref->{conference}[0]->{isStarted}[0];

         if ($DEBUG) { $LOGGER->debug( "number = $number adminPin = $adminpin started = $started\n"); }
         $reqid = listenInsert("conference", @CONNECTION, $number, $adminpin, $started);
      }

      if ($ref->{participant} ne undef) {
         my ($number) = $ref->{participant}[0]->{number}[0];
         my ($audio)  = $ref->{participant}[0]->{audioResource}[0];
         my ($admin)  = $ref->{participant}[0]->{isAdmin}[0];
         my ($admute) = $ref->{participant}[0]->{isAdminMuted}[0];
         my ($hold)   = $ref->{participant}[0]->{isHolding}[0];
         my ($muted)  = $ref->{participant}[0]->{isMuted}[0];
         my ($sess)   = $ref->{participant}[0]->{sessionID}[0];

         listenInsert("participant", @CONNECTION, $number, $reqid,
                    $audio, $admin, $admute, $hold, $muted, $sess);
         }
   }

   close(INFILE);
}

1;
