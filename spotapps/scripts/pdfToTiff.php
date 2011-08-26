<?php
    #Initialize Variables
    $tiff       = "";
    $pdf        = "";
    $result     = "";
    $objName    = "appObj";
    $status     = "Failure";
    $logFile    = "/interact/logs/exceptionLog";

    #Grab inputs
    $action         = @$_REQUEST['action'];
    $artifact       = @$_REQUEST['artifact'];
    $tiff           = @$_REQUEST['fileName'];
    $importedValue  = @$_REQUEST['importedValue'];

    $date   = date('m/d/Y G:i:s');
    $log    = "LVL:2 $date \t DIRECT_MESSAGE \t ";

    switch ($action) {
        case 'SEND_FAX':
            $pdfToTiff = "gs -q -sDEVICE=tiffg4 -r204x192 -dBATCH -dPDFFitPage -dNOPAUSE -sOutputFile=$tiff";
            $tmpArray = explode(",", $artifact);
            foreach ($tmpArray as &$value) {
                if(strlen($value) > 0) {
                    $pdfToTiff = $pdfToTiff." $value";
                }
            }
            @exec ($pdfToTiff, $output, $return_var);
            if ($return_var == 0) {
                $uri = "/interact/apps/iistart.ccxml";
                $appObj = urlencode ($importedValue);
                $sendFaxRequest = "curl -s http://localhost/spot/ccxml/createsession -d uri=$uri -d II_SB_importedValue=\"$appObj\"";
                @exec($sendFaxRequest, $output, $return_var);
                if ($return_var != 0) {
                    // Write exceptionLog: Error starting send fax application
                    $log = $log."Error starting send fax application. Attempting to fax [$tiff]\n";
                    error_log($log, 3, $logFile);
                }
            } else {
                // Write exceptionLog: Error converting pdf to tiff
                $log = $log."Error converting PDF file(s) [$artifact] to tiff\n";
                error_log($log, 3, $logFile);
            }
            break;
        case 'CREATE_PDF':
            $pdf = substr($tiff, 0, -3)."pdf";
            $tiffToPDF = "tiff2pdf -p letter -j -q 75 -f -o $pdf $tiff";
            @exec ($tiffToPDF, $output, $return_var);
            if ($return_var == 0) {
                $status = "Success";
                $result = $pdf;
                if (!(@unlink($tiff))) {
                    $log = $log."Unable to delete file [$tiff] after PDF conversion\n";
                    error_log($log, 3, $logFile);
                }
            } else {
                // Write exceptionLog: Error converting tiff to pdf
                $log = $log."Error converting TIFF file [$tiff] to PDF\n";
                error_log($log, 3, $logFile);
            }
            exitresult ($objName, $status, $result);
            break;
        default:
            // Write exceptionLog: Invalid request
            $log = $log."Invalid request [$action] for PDF or Tiff conversion\n";
            error_log($log, 3, $logFile);
    }

    exit;

    function exitresult ($objName, $status, $result) {
        echo "<?xml version=\"1.0\"?>\n";
        echo "<{$objName}>\n";
        echo "  <Status>$status</Status>\n";
        echo "  <Result>$result</Result>\n";
        echo "</{$objName}>\n";
    }
