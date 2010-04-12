#=======================================================
# Name: Listen.pm
#
# Description: Library for handling insert/update/delete 
#    for the Listen product.
#=======================================================
use strict;

package Listen;
require Exporter;

use constant TRUE  => 1;
use constant FALSE => 0;
### Uncomment this when ready for install
#use lib "/interact/listen/perl";

our @ISA    = qw(Exporter);
our @EXPORT = qw(listenInsert listenUpdate listenDelete listenGetIDs);

use REST::Client;
use XML::Parser;
use XML::XPath;
use XML::Simple;
use Data::Dumper;

my $DEBUG = FALSE;
my $ListenClient;

=head1 NAME

Listen - Main Listen functions. 

=head1 SYNOPSIS

   use Listen;
   listenInsert($reqtype, $host, $port, $timeout, $debug, ...)
   listenUpdate($reqtype, $host, $port, $timeout, $debug, ...)
   listenDelete($reqtype, $host, $port, $timeout, $debug, $id)

=head1 DESCRIPTION

   A collection of subroutines to handle the inserting, deleting, and 
   updating of Listen data.

=head1 FUNCTIONS

=over 4

=item * listenInsert($reqtype, $host, $port, $timeout, $debug, ...)
 
Accepts 
   $reqtype = "subscriber"        
            $number, $voicemailGreetingLocation, $voicemailPin

   $reqtype = "conference"        
            $number, $adminpin, $isstarted

   $reqtype = "participant"        
            $number, $conference, $audioResource, $isAdmin,
            $isAdminMuted, $isHolding, $isMuted, $sessionID

   $reqtype = "voicemail"        
            $subid, $dateCreated, $fileLocation, $isNew

Returns
   The id of the reqtype created which is > 0, else error number <= 0

=item * listenUpdate($reqtype, $host, $port, $timeout, $debug, ...)

Accepts
   $reqtype = "subscriber"
            $subid, $number, $voicemailGreetingLocation, $voicemailPin

   $reqtype = "conference"
            $confid, $number, $adminPin, $isStarted

   $reqtype = "participant"
            $partid, $number, $confid, $audioResource, $isAdmin,
            $isAdminMuted, $isHolding, $isMuted, $sessionID

   $reqtype = "voicemail"
            $voiceid, $subid, $dateCreated, $fileLocation, $isNew

Returns
   The id of the reqtype created which is > 0, else error number <= 0

=item * listenDelete($reqtype, $host, $port, $timeout, $debug, $id)

Accepts
   $reqtype = "subscriber", "conference", "participant", or "voicemail"
   $id      = the id number of the reqtype you wish to delete

Returns
   0 for success, else error number

=back

=head1 AUTHOR

Kevin Murray (murrayk@iivip.com)

=head1 COPYRIGHT

Copyright 2010 Interact Incorporated.

=cut

sub setListenClient {

   my ($host,$port,$timeout,$debug) = @_;
   my $sendto;

   if ($timeout eq undef) { $timeout = 5; }
   if ($debug) { $DEBUG = TRUE; }

   if ($DEBUG) {
      print "host = $host port = $port timeout = $timeout\n";
   }

   if ($port eq undef) {
      $sendto = "http://$host";
   }
   else {
      $sendto = "http://$host:$port";
   }

   $ListenClient = REST::Client->new({
                host => $sendto,
                timeout => $timeout,
                });
}

sub listenGetIDs {
   
   my ($reqtype,$host,$port,$timeout,$debug) = @_;

   my @resultarray;

   setListenClient($host,$port,$timeout,$debug);

   my $reqlist = $ListenClient->GET("/$reqtype"."s")->responseContent();

   my $xs = XML::Simple->new(KeyAttr => "$reqtype"."s", ForceArray => 1, KeepRoot => 1);
   my $ref = $xs->XMLin($reqlist);

   my $count = $ref->{$reqtype."s"}[0]->{count};
   my $total = $ref->{$reqtype."s"}[0]->{total};
   my $next = $ref->{$reqtype."s"}[0]->{next};

   if ($DEBUG) { print "Count $count Total $total Next $next\n"; }

   if ($count > 0) {

      my $done = FALSE;

      while (!$done) {
         print "Count $count Total $total Next $next\n";

         for (my $x = 0; $x < $count; ++$x) {

            my ($response) = $ListenClient->GET($ref->{$reqtype."s"}[0]->{$reqtype}[$x]->{href})->responseContent();

            if ($ListenClient->responseCode() != 200) {
               print STDERR "There was an error getting requests, error returned [" . $ListenClient->responseCode() . "]\n";
               return -($ListenClient->responseCode());
            }

            my $subxs = XML::Simple->new(KeyAttr => "$reqtype", ForceArray => 1, KeepRoot => 1);
            my $subref = $subxs->XMLin($response);     

            if ($DEBUG) { print Dumper($subref); }

            push (@resultarray, $subref->{$reqtype}[0]->{id}[0]);
         }
         if ($next eq undef) {
            $done = TRUE;
         }
         else {
            $ListenClient->GET($next);
            $reqlist = $ListenClient->responseContent();
            $ref     = $xs->XMLin($reqlist);
            $count   = $ref->{$reqtype."s"}[0]->{count};
            $total   = $ref->{$reqtype."s"}[0]->{total};
            $next    = $ref->{$reqtype."s"}[0]->{next};
         }
      }
   }
   return @resultarray;
}

sub listenDelete {
   my $id;

   my ($reqtype,$host,$port,$timeout,$debug,$id) = @_;

   setListenClient($host,$port,$timeout,$debug);

   $ListenClient->DELETE("/$reqtype"."s/$id");

   if ($DEBUG) {
      print "Response Code " . $ListenClient->responseCode() . "\n";
   }

   if ($ListenClient->responseCode() != 200 &&
       $ListenClient->responseCode() != 204 ) {
      print STDOUT "$reqtype $id was not DELETED, error returned [" . $ListenClient->responseCode() . "] [" .
                   $ListenClient->responseContent() . "]\n";
      return -($ListenClient->responseCode());
   }
   else {
      if ($DEBUG) { print STDOUT "$reqtype $id was successfully DELETED\n"; }
      return 0;
   }
}

sub getVersion {
 
   my ($reqtype, $reqid) = @_;

   my ($getresult) = $ListenClient->GET("/$reqtype"."s/$reqid")->responseContent();
   
   my $xs = XML::Simple->new(KeyAttr => "$reqtype"."s", ForceArray => 1, KeepRoot => 1);
   my $ref = $xs->XMLin($getresult);

   if ($ListenClient->responseCode != 200) {
      print STDERR "Error with version GET [" . $ListenClient->responseCode() . "] [$getresult]\n";
      return -($ListenClient->responseCode());
   }
   return $ref->{$reqtype}[0]->{version}[0];
}

sub listenUpdate {

   my ($id, $vid, $number, $adminpin, $started, $admute);
   my ($audio, $admin, $holding, $muted, $session);
   my ($created, $location, $isnew, $conference, $version);

   my ($reqtype,$host,$port,$timeout,$debug,@reqdata) = @_;

   setListenClient($host,$port,$timeout,$debug);

   my $reqxml    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

   if ($reqtype eq "subscriber") {
      ($id,$number,$location,$adminpin) = @reqdata;

      $version = getVersion($reqtype, $id);
      $reqxml .= "<$reqtype href=\"/$reqtype"."s/$id\">
                     <number>$number</number>
                     <version>$version</version>";

      if ($location ne undef) {
         $reqxml .= "<voicemailGreetingLocation>$location</voicemailGreetingLocation>";
      }
      if ($adminpin ne undef) {
         $reqxml .= "<voicemailPin>$adminpin</voicemailPin>";
      }
   }

   elsif ($reqtype eq "conference") {
     ($id,$number, $adminpin, $started) = @reqdata;

      $version = getVersion($reqtype, $id);
      $reqxml .= "<$reqtype href=\"/$reqtype"."s/$id\">
                     <adminPin>$adminpin</adminPin>
                     <isStarted>$started</isStarted>
                     <version>$version</version>
                     <number>$number</number>";
   }

   elsif ($reqtype eq "participant") {
      ($id,$number, $conference, $audio, $admin,
       $admute, $holding, $muted, $session) = @reqdata;

      $version = getVersion($reqtype, $id);
      $reqxml .= "<$reqtype href=\"/$reqtype"."s/$id\">
                     <audioResource>$audio</audioResource>
                     <conference href=\"/conferences/$conference\"/>
                     <isAdmin>$admin</isAdmin>
                     <isHolding>$holding</isHolding>
                     <isAdminMuted>$admute</isAdminMuted>
                     <isMuted>$muted</isMuted>
                     <number>$number</number>
                     <version>$version</version>
                     <sessionID>$session</sessionID>";
   }

   elsif ($reqtype eq "voicemail") {
      ($vid, $id, $created, $location, $isnew) = @reqdata;

      $version = getVersion($reqtype, $id);
      $reqxml .= "<$reqtype href=\"/$reqtype"."s/$vid\">
                     <dateCreated>$created</dateCreated>
                     <fileLocation>$location</fileLocation>
                     <isNew>$isnew</isNew>
                     <version>$version</version>
                     <subscriber href=\"/subscribers/$id\"/>";
   }

   $reqxml .= "</$reqtype>";

   if ($version < 0) { return $version; }

   $ListenClient->PUT("/$reqtype"."s/$id", $reqxml, {CustomHeader => 'Content-Type: application/xml;charset=UTF-8'});

   my ($postresult) = $ListenClient->responseContent();

   if ($DEBUG) {
      print "XML = $reqxml\n";
      print "PUT Response      = " . $ListenClient->responseContent() . "\n";
      print "PUT Response code = " . $ListenClient->responseCode() . "\n";
   }

   my $parser = XML::Parser->new(ErrorContext => 2);

   if ($ListenClient->responseCode() != 200) {
      print STDERR "Error calling PUT [" . $ListenClient->responseCode() . "] [" 
                   . $ListenClient->responseContent() . "]\n";
      return -($ListenClient->responseCode());
   }

   eval {
      $parser->parse($postresult);
   };

   if ($@) {
      $@ =~ s/at \/.*?$//s;
      print STDERR "\nERROR in '$postresult':\n$@\n";
      return 0;
   }

   ####
   # Let's do a get to verify the info in the table is correct
   ####
   my $tree    = XML::XPath->new(xml => $postresult);
   my $nodeset = $tree->find('//id');
   my @checkid = map($_->string_value, $nodeset->get_nodelist);

   my ($getresult) = $ListenClient->GET("/$reqtype"."s/$checkid[0]")->responseContent();

   if ($DEBUG) {
      print "GET Response      = " . $ListenClient->responseContent() . "\n";
      print "GET Response code = " . $ListenClient->responseCode() . "\n";
   }

   if ($ListenClient->responseCode() != 200) {
      print STDERR "Error calling PUT [" . $ListenClient->responseCode() . "] [" . $ListenClient->responseContent() . "]\n";
      return -($ListenClient->responseCode());
   }

   if ($getresult ne $postresult) {
      print "RESULT = $postresult\n";
      print "GET    = $getresult\n";
      print STDERR "The results do not match\n";
   }

   return $checkid[0];
}

sub listenInsert {

   my ($id, $number, $adminpin, $started, $admute);
   my ($audio, $admin, $holding, $muted, $session);
   my ($created, $location, $isnew, $conference);

   my ($reqtype,$host,$port,$timeout,$debug,@reqdata) = @_;

   setListenClient($host,$port,$timeout,$debug);

   my $countsxml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><$reqtype"."s href=\"/$reqtype"."s\">";
   my $reqxml    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><$reqtype href=\"/$reqtype"."s/1\">";

   if ($reqtype eq "subscriber") {
      ($number,$location,$adminpin) = @reqdata;

      $reqxml .= "<number>$number</number>";
      if ($location ne undef) {
         $reqxml .= "<voicemailGreetingLocation>$location</voicemailGreetingLocation>";
      }
      if ($adminpin ne undef) {
         $reqxml .= "<voicemailPin>$adminpin</voicemailPin>";
      }
   }

   elsif ($reqtype eq "conference") {
     ($number, $adminpin, $started) = @reqdata;

      $reqxml .= "<adminPin>$adminpin</adminPin>
                  <isStarted>$started</isStarted>
                  <number>$number</number>";
   }

   elsif ($reqtype eq "participant") {
      ($number, $conference, $audio, $admin,
       $admute, $holding, $muted, $session) = @reqdata;

      $reqxml .= "<audioResource>$audio</audioResource>
                  <conference href=\"/conferences/$conference\"/>
                  <isAdmin>$admin</isAdmin>
                  <isHolding>$holding</isHolding>
                  <isMuted>$muted</isMuted>
                  <isAdminMuted>$admute</isAdminMuted>
                  <number>$number</number>
                  <sessionID>$session</sessionID>";
   }

   elsif ($reqtype eq "voicemail") {
      ($id, $created, $location, $isnew) = @reqdata;

         $reqxml .= "<dateCreated>$created</dateCreated>
                     <fileLocation>$location</fileLocation>
                     <isNew>$isnew</isNew>
                     <subscriber href=\"/subscribers/$id\"/>";
   }

   $reqxml .= "</$reqtype>";

   if ($DEBUG) { print "XML = $reqxml\n"; }
   $ListenClient->POST("/$reqtype"."s",$reqxml,{CustomHeader => 'Content-Type: appliceation/xml;charset=UTF-8'});

   if ($DEBUG) {
      print "POST Response      = " . $ListenClient->responseContent() . "\n";
      print "POST Response code = " . $ListenClient->responseCode() . "\n";
   }

   my ($postresult) = $ListenClient->responseContent();

   if ($ListenClient->responseCode() != 201) {
      print STDERR "Error Could not create record $reqtype with POST [" . $ListenClient->responseCode() . "] [" .
                    $ListenClient->responseContent() . "]\n";
      return -($ListenClient->responseCode());
   }

   my $parser = XML::Parser->new(ErrorContext => 2);

   eval {
      $parser->parse($postresult);
   };

   if ($@) {
      $@ =~ s/at \/.*?$//s;
      print STDERR "\nERROR in '$postresult':\n$@\n";
      return 0;
   }

   ####
   # Let's do a get to verify the info in the table is correct
   ####
   my $tree    = XML::XPath->new(xml => $postresult);
   my $nodeset = $tree->find('//id');
   my @checkid = map($_->string_value, $nodeset->get_nodelist);

   my ($getresult) = $ListenClient->GET("/$reqtype"."s/$checkid[0]")->responseContent();

   if ($DEBUG) {
      print "GET Response      = " . $ListenClient->responseContent() . "\n";
      print "GET Response code = " . $ListenClient->responseCode() . "\n";
      print "GET Response ID = " . $checkid[0] ."\n";
   }

   if ($ListenClient->responseCode() != 200) {
      print STDERR "Error with GET [" . $ListenClient->responseCode() . "] [" . $ListenClient->responseContent() . "]\n";
      return -($ListenClient->responseCode());
   }

   if ($getresult ne $postresult) {
      print "RESULT = $postresult\n";
      print "GET    = $getresult\n";
      print STDERR "The results do not match\n";
   }

   return $checkid[0];
}
1;
