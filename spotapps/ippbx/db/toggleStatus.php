<?php

# Grab inputs
$t_ext      = @$_REQUEST['extension'];
$t_status   = @$_REQUEST['status'];

# Connect to db
$t_connect = mysql_connect("localhost","root","super");
if (!$t_connect) {
    die('ERROR: Unable to connect - ' . mysql_error());
}

mysql_select_db("ip_pbx", $t_connect);
$t_query = "UPDATE extension_map SET in_use=$t_status WHERE ext=$t_ext";

if (mysql_query($t_query)) {
    echo "<?xml version=\"1.0\"?>\n";
    echo "<clientToggle>\n";
    echo "\t<result>Success</result>\n";
    echo "</clientToggle>\n";
} else {
    echo "<?xml version=\"1.0\"?>\n";
    echo "<clientToggle>\n";
    echo "\t<result>Failure</result>\n";
    echo "</clientToggle>\n";
}
