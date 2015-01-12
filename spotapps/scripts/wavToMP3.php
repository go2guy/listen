<?php
    #Initialize Variables
    $objName    = "appObj";
    $PCM        = "";
    $MP3        = "";
    $tmpFile    = "";
    
    #Grab inputs
    $inputFile      = @$_REQUEST['fileLocation']       or exitresult ($objName,$status,"'fileLocation' argument missing from request");

    $tmpFile = substr($inputFile,0, -4);
    $convertToPCM  = "/interact/listen/bin/logtolinwav < $inputFile > $tmpFile.pcm.wav";
    $convertToMP3  = "/interact/listen/bin/lame --resample 32 -b 32 --silent $tmpFile.pcm.wav $tmpFile.mp3";

    // Execute 
    @exec("$convertToPCM", $output, $return_var);
    $PCM = "$return_var";

    @exec("$convertToMP3", $output, $return_var);
    $MP3 = "$return_var";

    @unlink("$tmpFile.pcm.wav");

    exitresult($objName,$PCM,$MP3);
    
    #Exit php
    function exitresult ($objName, $PCM, $MP3) {
        header('Content-type: application/xml');
        echo "<?xml version=\"1.0\"?>\n";
        echo "<{$objName}>\n";
        echo "  <pcm>{$PCM}</pcm>\n";
        echo "  <mp3>{$MP3}</mp3>\n";
        echo "</{$objName}>\n";
        exit;
    }
