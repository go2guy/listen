<?php
# Set Global Variables
$objName = "update";
$result  = "Failure";
$reason  = "";

# Grab inputs
$t_flag     = @$_REQUEST['flag'];
$t_ext      = @$_REQUEST['extension'];
$t_id       = @$_REQUEST['id'];

# Connect to mysql
$t_connect = mysql_connect("localhost","root");
if (!$t_connect)
    exitresult($objName, $result);

# Select db
if(!(mysql_select_db("ip_pbx", $t_connect)))
    exitresult($objName, $result);

# Set sql statement
if ($t_flag == 'insert')
    $t_update = "INSERT INTO active_clients (connection_id, client) VALUES (\"$t_id\", \"$t_ext\")";
else if ($t_flag == 'delete')
    $t_update = "DELETE FROM active_clients WHERE connection_id=\"$t_id\" AND client=\"$t_ext\" LIMIT 1";
else
    exitresult($objName, $result);

# Run insert/delete
mysql_query($t_update);

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