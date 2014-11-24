<?php
    #Global variables
    $objName = "apiObj";
    $status  = "Failure";
    $string  = "";
    $message = "";
    $http_success = 2;
    $options = array();
    $header = "";
    $logFile    = "/interact/logs/exceptionLog";

    $lstnChannel= @$_REQUEST['lstnChannel'] or $lstnChannel="";
    $lstnSub    = @$_REQUEST['lstnSub']     or $lstnSub="";
    $fromCCXML  = @$_REQUEST['fromCCXML']   or $fromCCXML="";

    $logdate   = date('m/d/Y G:i:s');
    $log    = "LVL:2 $logdate \t $fromCCXML \t ";

    $date = date('D, d M Y H:i:s T');

    $ID         = @$_REQUEST['id']          or $ID="";
    $ContentType= @$_REQUEST['contentType'] or $ContentType="application/json";

    #Grab data parameters
    $data       = @$_REQUEST['params'];
    $cnt        = sizeof($_REQUEST);

    if ((strlen($data) == 0) && ($cnt == 0)) {
        exitresult ($objName,$status,$string,"both 'params' and query stirng arguments missing from request");
    }
    else if (strlen($data) == 0) {
        $ContentType='application/x-www-form-urlencoded';
        $data = 'args={';
        foreach ($_REQUEST as $key => $value) {
            $data .= "'$key':'$value',";
            #$log    = "LVL:2 $logdate \t $fromCCXML \t ";
            #$log = $log."query string [$key][$value]:[$data]\n";
            #error_log($log, 3, $logFile);
        }
        $data .= '}';
    }

    #Grab request method
    $method     = @$_REQUEST['request'];
    if (!$method) {
        $method = @$_SERVER['REQUEST_METHOD'];
    }

    $rsrc       = @$_REQUEST['resource'];

    #Grab resource or set it
    $rsrc       = @$_REQUEST['resource'];
    if (!$rsrc) {
        $rsrc = "api/sipRegister";
    }

    $destURL       = @$_REQUEST['cntrlURL'];
    if (!$destURL) {
        $destURL = 'http://10.10.123.51:8080/listen';
        $destURL = 'http://localhost:8080/listen-controller';
    }

    $signature = sign($date);

    #Set up cURL options
    $curlHandle = curl_init();
    $method = strtoupper($method);

    switch ($method) {
        case 'POST':
            $destURL .= "/".$rsrc;
            $header = array("Content-Type: $ContentType", "X-Listen-AuthenticationType: U1lTVEVN", "Date: $date", "X-Listen-Signature: $signature");

            if ($lstnChannel != "") array_push($header, "X-Listen-Channel: ". $lstnChannel);
            if ($lstnSub != "") array_push($header, "X-Listen-Subscriber: ".  $lstnSub);
            $options = array(CURLOPT_URL => $destURL,
                            CURLOPT_RETURNTRANSFER => true,
                            CURLOPT_FAILONERROR => true,
                            CURLOPT_FRESH_CONNECT => true,
                            CURLOPT_POST => true,
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
        if (strlen($fromCCXML) > 0) {
            $log = $log."Error [$err] while attempting [$method] on [$rsrc] with [$data]\n";
            error_log($log, 3, $logFile);
            exit;
        } else {
            exitresult ($objName,$status,$string,$err);
        }
    }

    #cURL succeeded. Check Controller return code
    $rtrn = curl_getinfo($curlHandle);
    $code = $rtrn['http_code'];
    if (substr($code,0,1) != $http_success) {
        $err = $rtrn['http_code'];
        curl_close($curlHandle);
        if (strlen($fromCCXML) > 0) {
            $log = $log."Error [$err] while attempting [$method] on [$rsrc] with [$data]\n";
            error_log($log, 3, $logFile);
            exit;
        } else {
            exitresult ($objName,$status,$string,$err);
        }
    }

    #Successful return
    curl_close($curlHandle);
    $status = "Success";
    $string = htmlspecialchars($response);
    $string = urldecode($string);

    exitresult ($objName,$status,$string,$message);

    #Exit php
    function exitresult ($objname, $status, $string, $reason="") {
        if ($status == "Success") {
            echo "$string\n";
        } else {
            echo "<?xml version=\"1.0\"?>\n";
            echo "<{$objname}>\n";
            echo "  <Status>$status</Status>\n";
            echo "  <Result>$string</Result>\n";
            echo "  <Reason>$reason</Reason>\n";
            echo "</{$objname}>\n";
        }
        exit;
    }

    // create a request signature for the given date
    function sign($date) {
        $key = base64_decode('MIIEowIBAAKCAQEA5NmI8GIsupuvOMXR4yfs8hK2RUmX/CKlHmLEr/b1mPr/gx+fenafOSKoXs7OUvUxF/CR0EW6iUJvJ9lmTACTZFsTfF+mSJZ76dpuh6J8BQj9lpnH+AEp05LqLr2zvlzkksrjYSW4hfaZqfUZDk8YvGsdBqDpWrEykD16R5Jv5Iw/Y13Jd99F5zYU+Z3M+XdBSrFSaDwU5GeiOQVyl8q/Bt9gY7O1HfqYY9udXmAzPfEaZdCqCj7B8V8Sj7Wc92TZ/fHabZFKzfhVwfHCAzK4mQZ1be8snJd1f9R2peqjzBEINGdOnAnm0rGItKZJY91LMYi5H6Wh/Qh21CYY4Ne2wwIDAQABAoIBAEKXQg+goZ9TOfNtLJvKvFncNAmJVp5Zfm6PEuiZFfID52HCS+eYqNA5U4Dy8HqXOkfbCrLt90+Fc07HJcsrx7fGAK+KLZqlnzz3AH6bOzdD3HZ8HQH/ZKpZ76bWMH1ODnzgaLWWAlGI5kHcPgQ549q/2FxbakunkC0ElpZI+CIqWEf0zNfTXOpExMWx+FENntk/qpHijE+zcbh1/cy8Bsmj0WcXZ3aTDElG3XCC21rPd7zgrFeL6Sy26Br0eqxqddatghZHB5PhZYE4BYM3AlZTZVfuDHogXn4BXILTyV+oQHNUzHKjStfVryJnslnWVAF5whaXGm1fyRY3WBAEmuECgYEA+AivBccqnKfgB7BTDTUMCdt7v+vVbR7CEZc5/jCRy+N0R6NN/L/+LdAAjsodjzVmkLOUzPbZhw72hHXA63RijAhSDeg8IdCUY1YVygyr5KZpHRnn96XJOucPCCFWFfiToJ2Qz+JfZMBUt9HhsSWGS2jF2hUGjixhRwFwTd5xtYsCgYEA7DMe0bJeF336UtgrY9MNVC1sc/A8sEeYtlr47rcLlRDboaO9PRsuIMssxiZ/9uRkYP1FDmS3S8pgwg4UuoVUYHg5Tt95iq3aK0BRQnpKuGIerbBCwFUBZMwpP1DW3fMKtOQ5pjoV74tqR+df7gD0hWlfMFV8WUP9vXLa+XBcWqkCgYEA85sbw3IEsQ3UY9jTCSKzqy7NUQcgfGb8NmiwBa7QU08XUpDatMYgsAAdvCBYfeH11WL7X3+G0DZq+lfo3ZhWfbBiXtRb0t5YD2RqTCK75PtoO7PI95r1lAuB4PtU4Ile/R4kL3jnNj4MNupFX0Y6qu/BetqxsIt4E1QfZ+t1BNcCgYBrDiCB2t5at3al5eSEsjvwU0Y8pj5bh5fnzwPU7pIJVkK12IkFETSvGGeKyBhnxszYSPLruyp455lDWy55+8RqlRMkdJWaDYI86EHsZ5FGUPKmtqUKl3yyOvbXA8TfhDDuHCMk/F7E2+OoA26vaS9q6H+EYLqjmvV+0Hf/ZrX1QQKBgGd8r9wi+fPG3IgGXHBQjmnVgaPNsvQzBKFmHrER0/iLZuA9A2R5x7DxZdHUSRWaPADIaHiU1O9jbNJCk7Jtnqn7M85Q0SRsqZhA2+28/1bmqrTkQmT7T9Q4+hUEN+qehZx83BkRYaP1QWuH11UcRxFr+O3HNXlC9mAG/zt6HhuV');
        $sig = hash_hmac('SHA1', $date, $key, true);
        $sig = base64_encode($sig);
        return $sig;
    }