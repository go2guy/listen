<?php
    // create a request signature for the given date
    function sign($date) {
        $key = base64_decode('MIIEowIBAAKCAQEA5NmI8GIsupuvOMXR4yfs8hK2RUmX/CKlHmLEr/b1mPr/gx+fenafOSKoXs7OUvUxF/CR0EW6iUJvJ9lmTACTZFsTfF+mSJZ76dpuh6J8BQj9lpnH+AEp05LqLr2zvlzkksrjYSW4hfaZqfUZDk8YvGsdBqDpWrEykD16R5Jv5Iw/Y13Jd99F5zYU+Z3M+XdBSrFSaDwU5GeiOQVyl8q/Bt9gY7O1HfqYY9udXmAzPfEaZdCqCj7B8V8Sj7Wc92TZ/fHabZFKzfhVwfHCAzK4mQZ1be8snJd1f9R2peqjzBEINGdOnAnm0rGItKZJY91LMYi5H6Wh/Qh21CYY4Ne2wwIDAQABAoIBAEKXQg+goZ9TOfNtLJvKvFncNAmJVp5Zfm6PEuiZFfID52HCS+eYqNA5U4Dy8HqXOkfbCrLt90+Fc07HJcsrx7fGAK+KLZqlnzz3AH6bOzdD3HZ8HQH/ZKpZ76bWMH1ODnzgaLWWAlGI5kHcPgQ549q/2FxbakunkC0ElpZI+CIqWEf0zNfTXOpExMWx+FENntk/qpHijE+zcbh1/cy8Bsmj0WcXZ3aTDElG3XCC21rPd7zgrFeL6Sy26Br0eqxqddatghZHB5PhZYE4BYM3AlZTZVfuDHogXn4BXILTyV+oQHNUzHKjStfVryJnslnWVAF5whaXGm1fyRY3WBAEmuECgYEA+AivBccqnKfgB7BTDTUMCdt7v+vVbR7CEZc5/jCRy+N0R6NN/L/+LdAAjsodjzVmkLOUzPbZhw72hHXA63RijAhSDeg8IdCUY1YVygyr5KZpHRnn96XJOucPCCFWFfiToJ2Qz+JfZMBUt9HhsSWGS2jF2hUGjixhRwFwTd5xtYsCgYEA7DMe0bJeF336UtgrY9MNVC1sc/A8sEeYtlr47rcLlRDboaO9PRsuIMssxiZ/9uRkYP1FDmS3S8pgwg4UuoVUYHg5Tt95iq3aK0BRQnpKuGIerbBCwFUBZMwpP1DW3fMKtOQ5pjoV74tqR+df7gD0hWlfMFV8WUP9vXLa+XBcWqkCgYEA85sbw3IEsQ3UY9jTCSKzqy7NUQcgfGb8NmiwBa7QU08XUpDatMYgsAAdvCBYfeH11WL7X3+G0DZq+lfo3ZhWfbBiXtRb0t5YD2RqTCK75PtoO7PI95r1lAuB4PtU4Ile/R4kL3jnNj4MNupFX0Y6qu/BetqxsIt4E1QfZ+t1BNcCgYBrDiCB2t5at3al5eSEsjvwU0Y8pj5bh5fnzwPU7pIJVkK12IkFETSvGGeKyBhnxszYSPLruyp455lDWy55+8RqlRMkdJWaDYI86EHsZ5FGUPKmtqUKl3yyOvbXA8TfhDDuHCMk/F7E2+OoA26vaS9q6H+EYLqjmvV+0Hf/ZrX1QQKBgGd8r9wi+fPG3IgGXHBQjmnVgaPNsvQzBKFmHrER0/iLZuA9A2R5x7DxZdHUSRWaPADIaHiU1O9jbNJCk7Jtnqn7M85Q0SRsqZhA2+28/1bmqrTkQmT7T9Q4+hUEN+qehZx83BkRYaP1QWuH11UcRxFr+O3HNXlC9mAG/zt6HhuV');
        $sig = hash_hmac('SHA1', $date, $key, true);
        $sig = base64_encode($sig);
        return $sig;
    }

    function getInfo($curlHandle, $controller, $data, $date, $signature) {
        $header     = array("Accept: application/json", "X-Listen-AuthenticationType: U1lTVEVN", "Date: $date", "X-Listen-Signature: $signature");
        $controller.= $data;
        $options    = array(CURLOPT_URL => $controller,
                            CURLOPT_HTTPHEADER => $header,
                            CURLOPT_RETURNTRANSFER => true,
                            CURLOPT_FAILONERROR => true,
                            CURLOPT_FRESH_CONNECT => true
                            );

        curl_setopt_array($curlHandle, $options);
        $response = curl_exec($curlHandle);
        if (curl_errno($curlHandle)) {
            curl_close($curlHandle);
            formXML ('', '');
        }
        else {
            return (json_decode($response));
        }
    }

    function formXML($id, $result) {
        echo "<?xml version=\"1.0\"?>\n";
        echo "<client username=\"$id\" id=\"$id\" password=\"$result\"/>\n";
        exit;
    }

    $orgID      = '';
    $clientName = '';
    $controller = "http://localhost:9091/spotApi/";
    $curlHandle = curl_init();
    $date       = date('D, d M Y H:i:s T');
    $signature  = sign($date);

    $id         = @$_REQUEST['id'];
    $tmpVal     = explode ('.',$id);
    if ($tmpVal[0] == $id) {
        $orgID = 1;
        $clientName = str_replace ('ext', '', $id);
    }
    else {
        $orgID = $tmpVal[0];
        $clientName = str_replace ('ext', '', $tmpVal[1]);
    }

    $data       = "lookupAccessNumber?number=$clientName&organization=/organization/$orgID";
    $result = getInfo($curlHandle, $controller, $data, $date, $signature);

    if ($result->count == 1) {
        $clientID = explode ('/', $result->results[0]->subscriber->href);
        $data   = "getUser?id=$clientID[2]";
        $result = getInfo($curlHandle, $controller, $data, $date, $signature);
        $result = $result->voicemailPin;
    }
    else {
        $result = '';
    }

    curl_close($curlHandle);
    formXML ($id, $result);
?>

