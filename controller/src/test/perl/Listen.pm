use strict;

package Listen;
require Exporter;

### Uncomment this when ready for install
#use lib "/interact/listen/perl";

our @ISA = qw(Exporter);
our @EXPORT = qw(listenInsert listenUpdate listenDelete);

require REST::Client;
require XML::Parser;
require XML::XPath;

my $SUBLIMITS = 100;
my $DEBUG = 0;
my $ListenClient;

sub setListenClient {

   my ($host,$port,$timeout,$debug) = @_;
   my $sendto;

   if ($timeout eq undef) { $timeout = 5; }
   if ($debug) { $DEBUG = 1; }

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
      print STDOUT "$reqtype $id was not DELETED, error returned " . $ListenClient->responseCode() . "\n";
   }
   else {
      print STDOUT "$reqtype $id was successfully DELETED\n";
   }
}
sub listenUpdate {

   my ($id, $vid, $number, $adminpin, $started);
   my ($audio, $admin, $holding, $muted, $session);
   my ($created, $location, $isnew, $conference);

   my ($reqtype,$host,$port,$timeout,$debug,@reqdata) = @_;

   setListenClient($host,$port,$timeout,$debug);

   my $reqxml    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

   if ($reqtype eq "subscriber") {
      ($id,$number) = @reqdata;

      $reqxml .= "<$reqtype href=\"/$reqtype/$id\"><number>$number</number>";
   }

   elsif ($reqtype eq "conference") {
     ($id,$number, $adminpin, $started) = @reqdata;

      $reqxml .= "<$reqtype href=\"/$reqtype"."s/$id\"><adminPin>$adminpin</adminPin><isStarted>$started</isStarted><number>$number</number>";
   }

   elsif ($reqtype eq "participant") {
      ($id,$number, $conference, $audio, $admin,
       $holding, $muted, $session) = @reqdata;

      $reqxml .= "<$reqtype href=\"/$reqtype"."s/$id\"><audioResource>$audio</audioResource><conference href=\"/conferences/$conference\"/><isAdmin>$admin</isAdmin><isHolding>$holding</isHolding><isMuted>$muted</isMuted><number>$number</number><sessionID>$session</sessionID>";
   }

   elsif ($reqtype eq "voicemail") {
      ($vid, $id, $created, $location, $isnew) = @reqdata;

         $reqxml .= "<$reqtype href=\"/$reqtype"."s/$vid\"><dateCreated>$created</dateCreated><fileLocation>$location</fileLocation><isNew>$isnew</isNew><subscriber href=\"/subscribers/$id\"/>";
   }

   $reqxml .= "</$reqtype>";

   $ListenClient->PUT("/$reqtype"."s/$id", $reqxml, {CustomHeader => 'Content-Type: application/xml;charset=UTF-8'});

   my ($postresult) = $ListenClient->responseContent();

   if ($DEBUG) {
      print "POST Response      = ". $ListenClient->responseContent(). "\n";
      print "POST Response code = ". $ListenClient->responseCode(). "\n";
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
      print "GET Response      = ". $ListenClient->responseContent(). "\n";
      print "GET Response code = ". $ListenClient->responseCode(). "\n";
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
      ($number,$location) = @reqdata;

      $reqxml .= "<number>$number</number>";
      if ($location ne undef) {
         $reqxml .= "<voicemailGreetingLocation>$location</voicemailGreetingLocation>";
      }
   }

   elsif ($reqtype eq "conference") {
     ($number, $adminpin, $started) = @reqdata;

      $reqxml .= "<adminPin>$adminpin</adminPin><isStarted>$started</isStarted><number>$number</number>";
   }

   elsif ($reqtype eq "participant") {
      ($number, $conference, $audio, $admin,
       $admute, $holding, $muted, $session) = @reqdata;

      $reqxml .= "<audioResource>$audio</audioResource><conference href=\"/conferences/$conference\"/><isAdmin>$admin</isAdmin><isHolding>$holding</isHolding><isMuted>$muted</isMuted><isAdminMuted>$admute</isAdminMuted><number>$number</number><sessionID>$session</sessionID>";
   }

   elsif ($reqtype eq "voicemail") {
      ($id, $created, $location, $isnew) = @reqdata;

         $reqxml .= "<dateCreated>$created</dateCreated><fileLocation>$location</fileLocation><isNew>$isnew</isNew><subscriber href=\"/subscribers/$id\"/>";
   }

   $reqxml .= "</$reqtype>";

   print "XML = $reqxml\n";
   $ListenClient->POST("/$reqtype"."s",$reqxml,{CustomHeader => 'Content-Type: appliceation/xml;charset=UTF-8'});

   if ($DEBUG) {
      print "POST Response      = ". $ListenClient->responseContent(). "\n";
      print "POST Response code = ". $ListenClient->responseCode(). "\n";
   }

   my ($postresult) = $ListenClient->responseContent();

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
      print "GET Response      = ". $ListenClient->responseContent(). "\n";
      print "GET Response code = ". $ListenClient->responseCode(). "\n";
   }

   if ($getresult ne $postresult) {
      print "RESULT = $postresult\n";
      print "GET    = $getresult\n";
      print STDERR "The results do not match\n";
   }

   return $checkid[0];
}
1;
