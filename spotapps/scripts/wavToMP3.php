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
    $convertToMP3  = "/interact/listen/bin/lame $tmpFile.pcm.wav $tmpFile.mp3";

    // Execute 
    exec("$convertToPCM", $output, $return_var);
    $PCM = "$return_var";

    exec("$convertToMP3", $output, $return_var);
    $MP3 = "$return_var";

    exitresult($objName,$PCM,$MP3);
    
    #Exit php
    function exitresult ($objname, $PCM, $MP3) {
        echo "<?xml version=\"1.0\"?>\n";
        echo "<{$objname}>\n";
        echo "  <pcm>{$PCM}</pcm>\n";
        echo "  <mp3>{$MP3}</mp3>\n";
        echo "</{$objname}>\n";
        exit;
    }
