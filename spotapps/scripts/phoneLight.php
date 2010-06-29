<?php
    #Global variables
    $objName  = "obj";
    $result   = "Failure";
    $phoneLight = "";
    $dest       ="";
    $fileURL    ="/interact/apps/spotbuild/ippbx/toggleLight.ccxml";

    #Grab inputs
    $phoneLight = @$_REQUEST['toggleLight'] or exitresult ($objName,$result,"'toggleLight' argument missing from request");
    $dest       = @$_REQUEST['accessNum']       or exitresult ($objName,$result,"'subID' argument missing from request");


    $httpRequest  = "curl http://localhost/spot/ccxml/createsession -d uri=$fileURL -d phoneLight=$phoneLight -d dest=$dest";

    // Execute http request
    $result = exec($httpRequest);
    exitresult ($objName, $result, "");
    
    #Exit php
    function exitresult ($objname, $result, $reason="") {
        echo "<?xml version=\"1.0\"?>\n";
        echo "<{$objname}>\n";
        echo "  <Status>$result</Status>\n";
        echo "  <Reason>$reason</Reason>\n";
        echo "</{$objname}>\n";
        exit;
    }
