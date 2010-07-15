<?php
# database access
$db_host = "localhost";
$db_user = "root";
$db_pass = "";
$database = "ip_pbx";

mysql_connect( $db_host, $db_user, $db_pass );
@mysql_select_db( $database ) or die( "Unable to select database");
