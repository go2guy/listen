<?php
    #Global variables
    $objName = "saveMsg";
    $status  = "Failure";
    $string  = "";
    $message = "";

    #Grab inputs
    $fileLocation  	= @$_REQUEST['fileLocation'] or exitresult ($objName,$status,"","'fileLocation' argument missing from request");
    $destURL        = @$_REQUEST['controller']  or exitresult  ($objName,$status,"","'controller' argument missing from request");
    $subID          = @$_REQUEST['referenceID'] or exitresult  ($objName,$status,"","'referenceID' argument missing from request");
    $isNew  	    = @$_REQUEST['isNew']       or $isNew="new";

    #Set up json object for voicemail
    $obj = json_encode (
                        array (
                            'fileLocation' => $fileLocation,
                            'isNew' => $isNew,
                            'subscriber' => array (
                                            'href' => "/subscribers/$subID",
                                            ),
                        )
            );

    #Set up cURL options
    $curlHandle = curl_init();
    $destURL .= "/voicemails";
    $header = array("Content-Type: application/json");
    $options = array(CURLOPT_URL => $destURL,
                    CURLOPT_RETURNTRANSFER => true,
                    CURLOPT_FAILONERROR => true,
                    CURLOPT_FRESH_CONNECT => true,
                    CURLOPT_POST => true,
                    CURLOPT_POSTFIELDS => $obj,
                    CURLOPT_HTTPHEADER => $header
                    );
    curl_setopt_array($curlHandle, $options);

    #Execute cURL command
    $response = curl_exec($curlHandle);

    #Check for errors
    if (curl_errno($curlHandle)) {  #cURL errors
        $err = curl_error($curlHandle);
        curl_close($curlHandle);
        exitresult ($objName,$status,$string,"$err");
    }

    $http_success = 2;
    $rtrn = curl_getinfo($curlHandle);  #Controller errors
    $code = $rtrn['http_code'];
    if (substr($code,0,1) != $http_success) {
        $err = $rtrn['http_code'];
        curl_close($curlHandle);
        exitresult ($objName,$status,$string,"$err");
    }

    #cURL succeeded. Decode response
    curl_close($curlHandle);
    $obj = json_decode($response, true);

    if (strlen($obj['dateCreated']) > 0) {
        $status = "Success";
        $message = "Message saved";
    }
    else {
        $message = "Unable to process response from $destURL";
    }
    exitresult ($objName,$status,$string,$message);
    
    #Exit php
    function exitresult ($objname, $status, $string, $reason="") {
        echo "<?xml version=\"1.0\"?>\n";
        echo "<{$objname}>\n";
        echo "  <Status>{$status}</Status>\n";
        echo "  <Result>{$string}</Result>\n";
        echo "  <Reason>{$reason}</Reason>\n";
        echo "</{$objname}>\n";
        exit;
    }
