<?php

	//-------------------------
    // exitresult - return results and immediate exit
	//-------------------------
	function exitresult($objname, $result, $reason="", $size="", $accessed="", $modified="", $changed="") {
        header('Content-type: application/xml');
		echo "<?xml version=\"1.0\"?>\n";
		echo "<{$objname}>\n";
		echo "  <Result>{$result}</Result>\n";
 		echo "  <Reason>{$reason}</Reason>\n";
		if ($size != "") 		echo "  <Size>{$size}</Size>\n";
		if ($accessed != "") 	echo "  <LastAccess>{$accessed}</LastAccess>\n";
		if ($modified != "") 	echo "  <LastModified>{$modified}</LastModified>\n";
		if ($changed != "") 	echo "  <LastChanged>{$changed}</LastChanged>\n";
		echo "</{$objname}>\n";
		exit;
	}

	// Inputs
	$OBJNAME  	= @$_REQUEST['OBJNAME'] 	or $OBJNAME="fileOBJ";
	$FILE1 		= @$_REQUEST['FILE1'] 		or exitresult($OBJNAME,"Failure","No FILE1 Argument");
	$FILE2 		= @$_REQUEST['FILE2'] 		or $FILE2="";
	$OPERATION 	= @$_REQUEST['OPERATION'] 	or exitresult($OBJNAME,"Failure","No OPERATION Argument");;

	// Handle the operations
	switch ($OPERATION) {

		case "stat":
            if (!(strpos($FILE1,'://'))) {
			    $aStat = @stat($FILE1);
			    if ($aStat)
				    exitresult($OBJNAME, "Success", "", $aStat['size'], $aStat['atime'], $aStat['mtime'],$aStat['ctime']);
            }
            else {
                if (@fopen($FILE1, 'r'))
        		    exitresult($OBJNAME, "Success", "");
            }
    	    exitresult($OBJNAME, "Failure", "Could not stat [$FILE1]");
			break;

		case "copy":
			$position = strrpos($FILE2, "/");
			$DIRECTORY = substr($FILE2, 0, $position);
			if(is_dir($DIRECTORY)){
				if (copy($FILE1,$FILE2)) {
					exitresult($OBJNAME, "Success", "");
				} else {
					exitresult($OBJNAME, "Failure", "Could not copy [$FILE1] to [$FILE2]");
				}
			}
			else{
				if(mkdir($DIRECTORY, 0777)){
					if(copy($FILE1,$FILE2)){
						exitresult($OBJNAME, "Success", "");

					}
					else {
						exitresult($OBJNAME, "Failure", "Could not copy [$FILE1] to [$FILE2]");			}
				}
				else{
					exitresult($OBJNAME, "Failure", "Could not copy [$FILE1] to [$FILE2] FAILED to  make Directory");
				}
			}
			break;

		case "delete":
            if ((is_dir($FILE1)) && (dltDir($FILE1))){
                //Success
            }
			elseif (@unlink($FILE1)) {
                //Success
			} else {
    			exitresult($OBJNAME, "Failure", "Could not delete [$FILE1]");
            }
    		exitresult($OBJNAME, "Success", "");
			break;

		case "rename":
			if (@rename($FILE1,$FILE2)) {
				exitresult($OBJNAME, "Success", "");
			} else {
				exitresult($OBJNAME, "Failure", "Could not rename [$FILE1] to [$FILE2]");
            }
			break;

        case "create":
            if (@file_exists($FILE1)){
				exitresult($OBJNAME, "Success", "Directory structure exists");
			} else {
                if(@mkdir($FILE1,0777,true)){
                    exitresult($OBJNAME, "Success", "Directory structure exists");
			    } else {
    				exitresult($OBJNAME, "Failure", "File path does not exist for file [$FILE1]");
                }
            }
			break;

		default:
			exitresult($OBJNAME, "Failure", "Uknown OPERATION [$OPERATION]");
            break;
    }

    function dltDir($dirPath) {
        $d = opendir ($dirPath);
        if (!$d) {
            return false;
        }
        while($afile = readdir($d)) {
            if (($afile!= ".") && ($afile!= "..")) {
                if (is_dir($dirPath."/".$afile)) {
                    dltDir($dirPath."/".$afile);
                }
                else {
                    unlink($dirPath."/".$afile);
                }
            }
        }
        closedir($d);
        return (rmdir($dirPath));
    }

END;

?>