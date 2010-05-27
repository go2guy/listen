<?php
    #Global variables
    $objName = "apiObj";
    $status  = "Failure";
    $string  = "";
    $message = "";
    $http_success = 2;
    $options = array();
    $header = "";

    #Grab inputs
    $method     = @$_REQUEST['request']     or exitresult ($objName,$status,$string,"'request' argument missing from request");
    $rsrc       = @$_REQUEST['resource']    or exitresult ($objName,$status,$string,"'resource' argument missing from request");
    $data       = @$_REQUEST['params']      or exitresult ($objName,$status,$string,"'params' argument missing from request");
    $ID         = @$_REQUEST['id']          or $ID="";
    $destURL    = @$_REQUEST['cntrlURL']    or exitresult ($objName,$status,$string,"'cntrlURL' argument missing from request");

    #Set up cURL options
    $curlHandle = curl_init();
    $method = strtoupper($method);
    switch ($method) {
        case 'GET':
            $destURL .= "/".$rsrc.$data;
            $header = array("Accept: application/json");
            $options = array(CURLOPT_URL => $destURL,
                            CURLOPT_HTTPHEADER => $header,
                            CURLOPT_RETURNTRANSFER => true,
                            CURLOPT_FAILONERROR => true,
                            CURLOPT_FRESH_CONNECT => true
                            );
            break;
        case 'POST':
            $destURL .= "/".$rsrc;
            $header = array("Content-Type: application/json");
            $options = array(CURLOPT_URL => $destURL,
                            CURLOPT_RETURNTRANSFER => true,
                            CURLOPT_FAILONERROR => true,
                            CURLOPT_FRESH_CONNECT => true,
                            CURLOPT_POST => true,
                            CURLOPT_POSTFIELDS => $data,
                            CURLOPT_HTTPHEADER => $header
                            );
            break;
        case 'PUT':
        case 'DELETE':
            $destURL .= "/".$rsrc.$ID;
            $header = array("Content-Type: application/json", "Content-Length: ". strlen($data));
            $options = array(CURLOPT_URL => $destURL,
                            CURLOPT_RETURNTRANSFER => true,
                            CURLOPT_FAILONERROR => true,
                            CURLOPT_FRESH_CONNECT => true,
                            CURLOPT_CUSTOMREQUEST => $method,
                            CURLOPT_POSTFIELDS => $data,
                            CURLOPT_HTTPHEADER => $header
                            );
            break;
        default:
        $err = "Invalid method [$method]";
        exitresult ($objName,$status,$string,$err);
    }
    curl_setopt_array($curlHandle, $options);

    #Execute cURL command
    $response = curl_exec($curlHandle);

    #Check for cURL errors
    if (curl_errno($curlHandle)) {
        $err = curl_error($curlHandle);
        curl_close($curlHandle);
        exitresult ($objName,$status,$string,$err);
    }

    #cURL succeeded. Check Controller return code
    $rtrn = curl_getinfo($curlHandle);
    $code = $rtrn['http_code'];
    if (substr($code,0,1) != $http_success) {
        $err = $rtrn['http_code'];
        curl_close($curlHandle);
        exitresult ($objName,$status,$string,$err);
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
