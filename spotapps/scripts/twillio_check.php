<?php

$objName = "apiObj";
$status  = "Failure";
$string  = "";
$message = "";
$http_success = 2;
$options = array();
$header = "";
     
$username       = 'ACf76bef5f7f82a82813409fae74fb00c2';
$password       = '66311c2641f083f082f01da881f17708';
$method         = 'put';
$destURL        = "https://$username:$password@api.twilio.com/2010-04-01/Accounts/$username/Recordings.json";

    $method         = 'GET';
    $curlHandle = curl_init();
    $method = strtoupper($method);

    $header = array("Content-Type: text/xml");
    $options = array(CURLOPT_URL => $destURL,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_FAILONERROR => true,
        CURLOPT_FRESH_CONNECT => true,
        CURLOPT_CUSTOMREQUEST => $method,
        CURLOPT_HTTPHEADER => $header
        );

    curl_setopt_array($curlHandle, $options);

    #Execute cURL command
    $response = curl_exec($curlHandle);

    #Check for cURL errors
    if (curl_errno($curlHandle)) {
        $err = curl_error($curlHandle);
        curl_close($curlHandle);
        exitresult ($objName,"Failure",$err);
    }

    #cURL succeeded. Check Controller return code
    $rtrn = curl_getinfo($curlHandle);

    $code = $rtrn['http_code'];
    if ($code != '200') {
        $err = $rtrn['http_code'];
        curl_close($curlHandle);
        exitresult ($objName,"Failure",$err);
    }

    #Successful return
    curl_close($curlHandle);
    $string = htmlspecialchars($response);
    $status = "Success";
    exitresult ($objName,$status,$string);


#Exit php
function exitresult ($objname, $status, $string) {
    echo "<?xml version=\"1.0\"?>\n";
    echo "<{$objname}>\n";
    echo "  <Status>$status</Status>\n";
    echo "  <Result>$string</Result>";
    echo "</{$objname}>\n";
    exit;
}

?>
