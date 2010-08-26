<?php
/*
The main purpose of this script is to track IP phones (clients) that
are in-use.
*/

require_once('db.php');

# Set Global Variables
$objName = "update";
$result  = "Failure";
$reason  = "";
$list    = "";

# Grab inputs
$t_flag     = $_REQUEST['flag'];
$t_ext      = $_REQUEST['extension'];
$t_id       = $_REQUEST['id'];
$t_sid      = $_REQUEST['sessionID'];
$t_maxTime  = $_REQUEST['maxTime'];
$t_maxTime  = (int)$t_maxTime;

#Get Epoch
$t_currTime = time();

# Set sql statement
switch ($t_flag) {
    case "insert":
        $t_update = "INSERT INTO active_clients (connection_id, client, session_id, time_stamp) VALUES (\"$t_id\", \"$t_ext\", \"$t_sid\", \"$t_currTime\")";
        break;
    case "delete":
        $t_update = "DELETE FROM active_clients WHERE connection_id=\"$t_id\" AND client=\"$t_ext\" LIMIT 1";
        break;
    case "update":
        $t_update = "UPDATE active_clients SET session_id=\"$t_sid\" WHERE connection_id=\"$t_id\" AND client=\"$t_ext\"";
        break;
    case "select":
        $t_update = "SELECT session_id FROM active_clients WHERE client=\"$t_ext\"";
        break;
    case "getList":
        $t_update = "SELECT client FROM active_clients WHERE ($t_currTime - time_stamp) < $t_maxTime";
        break;
    case "dltList":
        $t_update = "DELETE FROM active_clients WHERE ($t_currTime - time_stamp) >= $t_maxTime OR client=\"$t_ext\"";
        break;
    default:
        exitresult($objName, $result);
}

# Run sql statement
$t_val = mysql_query($t_update);

# Check result
if ($t_flag == 'select') {
    if ((mysql_num_rows ($t_val)) === 1) {
        $t_row = mysql_fetch_array ($t_val);
        $result = $t_row['session_id'];
    }
    else {
        # Nothing to do here. Just exit with result == 'Faliure'
    }
}
else if ($t_flag == 'getList') {
    $result = mysql_num_rows ($t_val);
    if ($result > 0) {
        for( $i=0; $i<$result; $i++ ) {
            $t_row = mysql_fetch_array ($t_val);
            $list .= $t_row['client'] . "|";
        }
    }
    else {
        $result = 0;
    }
}
else if ((mysql_affected_rows($t_connect)) === 1)
    $result = "Success";
else {
    # Nothing to do here. Just exit with result == 'Faliure'
}

exitresult($objName, $result, $list);

# Exit PHP
function exitresult($objName, $result, $list="", $reason=""){
    echo "<?xml version=\"1.0\"?>\n";
    echo "<$objName>\n";
    echo "\t<result>$result</result>\n";
    echo "\t<clients>$list</clients>\n";
    if ($result == "Failure")
        $reason=mysql_error();
    echo "\t<reason>$reason</reason>\n";
    echo "</$objName>\n";
    exit;
}