<?php
# database access
$db_host = "localhost";
$db_user = "root";
$db_pass = "";
$database = "ip_pbx";

$t_connect = mysql_connect( $db_host, $db_user, $db_pass );
if ( !$t_connect )
    die('ERROR: Could not connect to mysql server as user [root]: ' . mysql_error());
else {
    $db_selected = mysql_select_db( $database );
    if ( !$db_selected )
        die( 'ERROR: Unable to select database [ip_pbx]: '. mysql_error());
}
