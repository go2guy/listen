<?php
# Set Global Variables
$objName = "fwdExt";
$result  = "Failure";

# Grab inputs
$t_flag     = @$_REQUEST['flag']            or exitresult ($objName,$result,"'flag' argument missing from request");
$t_ext      = @$_REQUEST['extension']       or exitresult ($objName,$result,"'extension' argument missing from request");
$t_dest     = @$_REQUEST['destination'];

# Connect to mysql
$t_connect = mysql_connect("localhost","root");
if (!$t_connect)
    exitresult($objName, $result);

# Select db
if(!(mysql_select_db("ip_pbx", $t_connect)))
    exitresult($objName, $result);

if($t_flag == 'select') {
# Set sql statement
    $t_query = "SELECT destination FROM forwarded_extensions WHERE origination=\"$t_ext\"";
}
else {
# Otherwise, first delete any records
$t_query = "DELETE FROM forwarded_extensions WHERE origination=\"$t_ext\"";
}

$t_val = mysql_query($t_query);

# Check result
if ($t_flag == 'select') {
    if ((mysql_num_rows ($t_val)) === 1) {
        #Found a record
        $t_row = mysql_fetch_array ($t_val);
        $result = $t_row['destination'];
    }
    elseif (!$t_val) {
        #Looks like our requery return an error
        $result = 'Falure';
    }
    else {
        #We didn't find a record
        $result = 'EMPTY';
    }
    exitresult($objName, $result);
}
else {
    if ((mysql_affected_rows($t_connect)) == 1)
        $result  = "Success";
    else
        $result  = "Failure";

    # Check flag to see if insert is needed
    if (($t_flag == 'forward') && $result) {
        $t_currTime = time();
        $t_query = "INSERT INTO forwarded_extensions (origination, destination, time_stamp) VALUES (\"$t_ext\", \"$t_dest\", \"$t_currTime\")";

        # Run insert
        if (mysql_query($t_query))
            $result = "Success";
        else
            $result = "Failure";
    }
    exitresult($objName,$result);
}

# Exit PHP
function exitresult($objName, $result, $reason=""){
    echo "<?xml version=\"1.0\"?>\n";
    echo "<$objName>\n";
    echo "\t<Result>$result</Result>\n";
    if ($result == "Failure")
        $reason=mysql_error();
    echo "\t<Reason>$reason</Reason>\n";
    echo "</$objName>\n";
    exit;
}
