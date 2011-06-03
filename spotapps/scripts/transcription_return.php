<?php

$objName = "apiObj";
$status  = "Failure";
$string  = "";
$message = "";
$options = array();
$header = "";
     
$transcription  = @$_REQUEST['TranscriptionText']       or exitresult ($objName,"Failure","Failure","'TranscriptionText' argument missing from request");
$id             = @$_REQUEST['RecordingSid'];
$username       = 'ACf76bef5f7f82a82813409fae74fb00c2';
$password       = '66311c2641f083f082f01da881f17708';
$destURL        = 'http://hathor-v:9091/api';
$twilioURL        = "https://$username:$password@api.twilio.com/2010-04-01/Accounts/$username/Recordings";

$deleteStatus = curlDeleteCommand($twilioURL, $id, $username, $password, $header );

$vmID = split(" ",$transcription);
$count = 0;

foreach ($vmID as $value)
{
    if($count == 0)
        $word = $value;
    elseif($count == 1)
        $number = $value;
    elseif($count == 2)
        $spacerWord = $value;
    elseif($count == 3)
        $voicemail = $value;
    else
        $voicemail = "$voicemail $value";
    $count = $count + 1;
}

$data = "{\"transcription\":\"$spacerWord $voicemail\"}";
$newNumber = substr($word, 1);
$ID = "$newNumber\n";

echo file_put_contents("log","destURL: $destURL\nID: $ID\ndata: $data\n");
echo "\n-----------------\n";

$curlResult = curlPostCommand($destURL, $ID, $data);
echo "$ID";
echo "$curlResult\n";

if ($curlResult != 'Success') {
    $ID = "$word\n";
    $curlResult = curlPostCommand($destURL, $ID, $data);
    echo "$ID";
    echo "$curlResult\n";

    if ($curlResult != 'Success') {
        $newNumber = substr($word, 1);
        $newNumber = substr($newNumber, 0, -1);
        $ID = "$newNumber\n";
        $curlResult = curlPostCommand($destURL, $ID, $data);
        echo "$ID";
        echo "$curlResult\n";
    
        $data = "{\"transcription\":\"$voicemail\"}";
    
        if ($curlResult != 'Success') {
            $newNumber = substr($number, 0, -1);
            $ID = "$newNumber\n";
            $curlResult = curlPostCommand($destURL, $ID, $data);
            echo "$ID";
            echo "$curlResult\n";

            if ($curlResult != 'Success') {
                $ID = "$number\n";
                    
                $curlResult = curlPostCommand($destURL, $ID, $data);
                echo "$ID";
                echo "$curlResult\n";

                if ($curlResult != 'Success') {
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

                    $ID = "$word$number\n";
                    $curlResult = curlPostCommand($destURL, $ID, $data);
                    echo "$ID";
                    echo "$curlResult\n";
                }
            }
        }
    }
}

function curlPostCommand($destURL, $ID, $data ){
    $http_success = 2;
    $method         = 'put';
    $date = date('D, d M Y H:i:s T');
    $signature = sign($date);

    $curlHandle = curl_init();
    $method = strtoupper($method);

    $destURL .= "/voicemails/".(int)$ID;
    $header = array("Content-Type: application/json", "Content-Length: ". strlen($data), "X-Listen-AuthenticationType: U1lTVEVN", "Date: $date", "X-Listen-Signature: $signature");
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
        return "Failure";
    }

    #cURL succeeded. Check Controller return code
    $rtrn = curl_getinfo($curlHandle);
    $code = $rtrn['http_code'];
    if (substr($code,0,1) != $http_success) {
        $err = $rtrn['http_code'];
        curl_close($curlHandle);
        echo "$err\n";
        return "Failure";
    }

    #Successful return
    curl_close($curlHandle);
    return "Success";
}

#Exit php
function exitresult ($objname, $status, $string, $reason="",$deleteStatus,$message) {
    echo "<?xml version=\"1.0\"?>\n";
    echo "<{$objname}>\n";
    echo "  <Status>$status</Status>\n";
    echo "  <Result>$reason</Result>\n";
    echo "</{$objname}>\n";
    echo file_put_contents("error_log","FAILURE\n");
    exit;
}

function curlDeleteCommand($destURL, $id, $username, $password, $header ){

    $method         = 'DELETE';
    $curlHandle = curl_init();
    $method = strtoupper($method);

    $destURL .= "/".$id;
    $header = array("Content-Type: application/json");
    $options = array(CURLOPT_URL => $destURL,
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_FAILONERROR => true,
        CURLOPT_FRESH_CONNECT => true,
        CURLOPT_CUSTOMREQUEST => $method,
        CURLOPT_HTTPHEADER => $header
        );

#    echo file_put_contents("log","Destination URL - $destURL\n");

    curl_setopt_array($curlHandle, $options);

    #Execute cURL command
    $response = curl_exec($curlHandle);

    #Check for cURL errors
    if (curl_errno($curlHandle)) {
        $err = curl_error($curlHandle);
        curl_close($curlHandle);
        return "Failure - cURL error - $err";
    }

    #cURL succeeded. Check Controller return code
    $rtrn = curl_getinfo($curlHandle);
    $code = $rtrn['http_code'];
    if ($code != '204') {
        $err = $rtrn['http_code'];
        curl_close($curlHandle);
        return "Failure - cURL succeed - $err";
    }

    #Successful return
    curl_close($curlHandle);
    $status = "Delete Success - $code";
    return $status;
}

// create a request signature for the given date
function sign($date) {
    $key = base64_decode('MIIEowIBAAKCAQEA5NmI8GIsupuvOMXR4yfs8hK2RUmX/CKlHmLEr/b1mPr/gx+fenafOSKoXs7OUvUxF/CR0EW6iUJvJ9lmTACTZFsTfF+mSJZ76dpuh6J8BQj9lpnH+AEp05LqLr2zvlzkksrjYSW4hfaZqfUZDk8YvGsdBqDpWrEykD16R5Jv5Iw/Y13Jd99F5zYU+Z3M+XdBSrFSaDwU5GeiOQVyl8q/Bt9gY7O1HfqYY9udXmAzPfEaZdCqCj7B8V8Sj7Wc92TZ/fHabZFKzfhVwfHCAzK4mQZ1be8snJd1f9R2peqjzBEINGdOnAnm0rGItKZJY91LMYi5H6Wh/Qh21CYY4Ne2wwIDAQABAoIBAEKXQg+goZ9TOfNtLJvKvFncNAmJVp5Zfm6PEuiZFfID52HCS+eYqNA5U4Dy8HqXOkfbCrLt90+Fc07HJcsrx7fGAK+KLZqlnzz3AH6bOzdD3HZ8HQH/ZKpZ76bWMH1ODnzgaLWWAlGI5kHcPgQ549q/2FxbakunkC0ElpZI+CIqWEf0zNfTXOpExMWx+FENntk/qpHijE+zcbh1/cy8Bsmj0WcXZ3aTDElG3XCC21rPd7zgrFeL6Sy26Br0eqxqddatghZHB5PhZYE4BYM3AlZTZVfuDHogXn4BXILTyV+oQHNUzHKjStfVryJnslnWVAF5whaXGm1fyRY3WBAEmuECgYEA+AivBccqnKfgB7BTDTUMCdt7v+vVbR7CEZc5/jCRy+N0R6NN/L/+LdAAjsodjzVmkLOUzPbZhw72hHXA63RijAhSDeg8IdCUY1YVygyr5KZpHRnn96XJOucPCCFWFfiToJ2Qz+JfZMBUt9HhsSWGS2jF2hUGjixhRwFwTd5xtYsCgYEA7DMe0bJeF336UtgrY9MNVC1sc/A8sEeYtlr47rcLlRDboaO9PRsuIMssxiZ/9uRkYP1FDmS3S8pgwg4UuoVUYHg5Tt95iq3aK0BRQnpKuGIerbBCwFUBZMwpP1DW3fMKtOQ5pjoV74tqR+df7gD0hWlfMFV8WUP9vXLa+XBcWqkCgYEA85sbw3IEsQ3UY9jTCSKzqy7NUQcgfGb8NmiwBa7QU08XUpDatMYgsAAdvCBYfeH11WL7X3+G0DZq+lfo3ZhWfbBiXtRb0t5YD2RqTCK75PtoO7PI95r1lAuB4PtU4Ile/R4kL3jnNj4MNupFX0Y6qu/BetqxsIt4E1QfZ+t1BNcCgYBrDiCB2t5at3al5eSEsjvwU0Y8pj5bh5fnzwPU7pIJVkK12IkFETSvGGeKyBhnxszYSPLruyp455lDWy55+8RqlRMkdJWaDYI86EHsZ5FGUPKmtqUKl3yyOvbXA8TfhDDuHCMk/F7E2+OoA26vaS9q6H+EYLqjmvV+0Hf/ZrX1QQKBgGd8r9wi+fPG3IgGXHBQjmnVgaPNsvQzBKFmHrER0/iLZuA9A2R5x7DxZdHUSRWaPADIaHiU1O9jbNJCk7Jtnqn7M85Q0SRsqZhA2+28/1bmqrTkQmT7T9Q4+hUEN+qehZx83BkRYaP1QWuH11UcRxFr+O3HNXlC9mAG/zt6HhuV');
    $sig = hash_hmac('SHA1', $date, $key, true);
    $sig = base64_encode($sig);
    return $sig;
}

?>
