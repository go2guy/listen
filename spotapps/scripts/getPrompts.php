<?php

	//-------------------------
    // exitresult - return results and immediate exit
	//-------------------------	 
	function exitresultJSON($result, $files) {
        if ($result == "Success")
    		echo "{\"Status\":\"$result\",\"Files\":[$files]}";
        else
    		echo "{\"Status\":\"$result\",\"Reason\":\"$files\"}";
		exit;
	}

	// Inputs
    $FILEPATH = '/interact/apps/spotbuild/listen_autoAttendant/audio';
	$OBJNAME  = @$_REQUEST['OBJNAME'] or $OBJNAME="filelist";
	$RETURN  = @$_REQUEST['RETURNTYPE'] or $RETURN="JSON";
    $FIRST = "true";

    if (is_dir($FILEPATH)) {
        if ($dh = opendir($FILEPATH)) {
            while (($file = readdir($dh)) !== false) {
                if ($file != ".")
                {
                    if ($file != "..")
                    {
                        if ($FIRST == "true"){
                            $FILES = "{\"fileName\":\"$file\"}";
                            $FIRST = "false";
                        }
                        else
                            $FILES = "$FILES,{\"fileName\":\"$file\"}";                
                    }
                }
            }
        closedir($dh);
        exitresultJSON("Success","$FILES");
        }
        else
            exitresultJSON("Failure","Could not open directory - $FILEPATH");
    }
    else
        exitresultJSON("Failure","Could not find directory - $FILEPATH");
?>
