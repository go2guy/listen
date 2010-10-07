<?php

$objName = "apiObj";
$status  = "Failure";
$string  = "";
$message = "";
$http_success = 2;
$options = array();
$header = "";
     


$response       = @$_REQUEST['TranscriptionText']       or exitresult ($objName,"Failure","Failure","'request' argument missing from request");
$method     = 'put';
$rsrc       = 'voicemails';
$destURL    = 'http://apps4:9091/api';

echo file_put_contents("log","$response");

$vmID = split(" ",$response);
$count = 0;

foreach ($vmID as $value)
{
    if($count == 0)
        $word = $value;
    elseif($count == 1)
        $number = $value;
    elseif($count == 2)
        $voicemail = $value;
    else
        $voicemail = "$voicemail $value";
    $count = $count + 1;
}

switch ($word) {

	case "Zero":
        $word = 0;
		break;
	case "One":
        $word = 1;
		break;
	case "Two":
        $word = 2;
		break;
	case "Three":
        $word = 3;
		break;
	case "Four":
        $word = 4;
        break;
	case "Five":
        $word = 5;
		break;
	case "Six":
        $word = 6;
		break;
	case "Seven":
        $word = 7;
		break;
	case "Eight":
        $word = 8;
		break;
	case "Nine":
        $word = 9;
       	break;
    default:
        break;
}

$data = "{\"transcription\":\"$voicemail\"}";
$ID = "$number\n";

$curlHandle = curl_init();
$method = strtoupper($method);

$destURL .= "/".$rsrc."/".(int)$ID;
$header = array("Content-Type: application/json", "Content-Length: ". strlen($data));
$options = array(CURLOPT_URL => $destURL,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_FAILONERROR => true,
    CURLOPT_FRESH_CONNECT => true,
    CURLOPT_CUSTOMREQUEST => $method,
    CURLOPT_POSTFIELDS => $data,
    CURLOPT_HTTPHEADER => $header
    );

curl_setopt_array($curlHandle, $options);

#Execute cURL command
$response = curl_exec($curlHandle);

#Check for cURL errors
if (curl_errno($curlHandle)) {
    $err = curl_error($curlHandle);
    curl_close($curlHandle);
    exitresult ($objName,$status,'cURL errors',$err);
}

#cURL succeeded. Check Controller return code
$rtrn = curl_getinfo($curlHandle);
$code = $rtrn['http_code'];
if (substr($code,0,1) != $http_success) {
    $err = $rtrn['http_code'];
    curl_close($curlHandle);
    exitresult ($objName,$status,'controller return',$err);
}

#Successful return
curl_close($curlHandle);
$status = "Success";
$string = htmlspecialchars($response);
exitresult ($objName,$status,$string,$message);

#Exit php
function exitresult ($objname, $status, $string, $reason="") {
    echo "<?xml version=\"1.0\"?>\n";
    echo "<{$objname}>\n";
    echo "  <Status>$status</Status>\n";
    echo "  <Result>$string</Result>\n";
    echo "  <Reason>$reason</Reason>\n";
    echo "</{$objname}>\n";
    exit;
}

?>