<?php
    #Global variables
    $objName = "validate";
    $status  = "Failure";
    $string  = "";
    $message = "";

    #Grab inputs
    $validateItem   = @$_REQUEST['dataItem'] or exitresult ($objName,$status,$string,"'dataItem' argument missing from request");
    $rsrcName       = @$_REQUEST['whichRsrc'] or exitresult ($objName,$status,$string,"'whichRsrc' argument missing from request");
    $destURL        = @$_REQUEST['controller'] or exitresult ($objName,$status,$string,"'controller' argument missing from request");
    $field          = @$_REQUEST['whichField'] or $field = "_fields=id,number&number";

    #Set up cURL options
    $curlHandle = curl_init();
    $destURL .= "/$rsrcName?$field=$validateItem";
    $header = array("Accept: application/json");
    $options = array(CURLOPT_URL => $destURL,
                    CURLOPT_HTTPHEADER => $header,
                    CURLOPT_RETURNTRANSFER => true,
                    CURLOPT_FAILONERROR => true,
                    CURLOPT_FRESH_CONNECT => true
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

    if (($obj['count']) == 1) {
        $status = "Success";
        $string = $obj['results'][0]['id'];
        $message = "$rsrcName:$validateItem found";
    }
    else if (($obj['count']) == 0) {
        $message = "$rsrcName:$validateItem not found";
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
