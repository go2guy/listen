<?php
    #Global variables
    $objName  = "obj";
    $result   = "Failure";

    #Grab inputs
    $sessionID  = @$_REQUEST['sessionID']   or exitresult ($objName,$result,"'sessionID' argument missing from request");
    $argList    = @$_REQUEST['argList']     or exitresult ($objName,$result,"'argList' argument missing from request");

    $httpRequest  = "curl http://localhost/spot/ccxml/basichttp -d sessionid=$sessionID -d II_SB_basichttpEvent=\"CREATESESSION\" -d II_SB_argument=$argList";

    // Execute http request
    $result = exec($httpRequest);
    if ($result == "")
        $result = "Success";
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
