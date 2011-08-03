<?php
	$dest 	 = @$_REQUEST['dest'];
	$destDir = @$_REQUEST['destDir'];

    if (!file_exists($destDir)) {
        if(!mkdir($destDir,0777,true))
    		exitresult("Failure", "Unable to create $destDir");
    }

    foreach($_FILES AS $field => $value)
    {
        $dest = $destDir.$dest;
        $real = $_FILES[$field]['name'];
        if(move_uploaded_file($_FILES[$field]['tmp_name'], $dest)) {
            chmod($dest,0777);
            exitresult("Success", "");
        }
        else
            exitresult("Failure", "Unable to move uploaded file from tmp directory to $destDir");
    }

	function exitresult($result, $reason="") {
		echo "<?xml version=\"1.0\"?>\n";
		echo "<uploads>\n";
		echo "  <Result>{$result}</Result>\n";
 		echo "  <Reason>{$reason}</Reason>\n";
		echo "</uploads>\n";
		exit;
	}
?>