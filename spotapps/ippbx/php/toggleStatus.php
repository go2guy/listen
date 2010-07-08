<?php
# Set Global Variables
$objName = "clientToggle";
$result  = "Failure";
$reason  = "";

# Grab inputs
$t_ext      = @$_REQUEST['extension'];
$t_status   = @$_REQUEST['status'];

# Connect to mysql
$t_connect = mysql_connect("localhost","root");
if (!$t_connect)
    exitresult($objName, $result);

# Select db
if(!(mysql_select_db("ip_pbx", $t_connect)))
    exitresult($objName, $result);

# Run query
$t_query = "UPDATE extension_map SET in_use=\"$t_status\" WHERE ext=\"$t_ext\"";
mysql_query($t_query);

# Check result
if ((mysql_affected_rows($t_connect)) != 1)
    exitresult($objName, $result);
else {
    $result = "Success";
    exitresult($objName,$result);
}

# Exit PHP
function exitresult($objName, $result, $reason=""){
    echo "<?xml version=\"1.0\"?>\n";
    echo "<$objName>\n";
    echo "\t<result>$result</result>\n";
    if ($result == "Failure")
        $reason=mysql_error();
    echo "\t<reason>$reason</reason>\n";
    echo "</$objName>\n";
    exit;
}