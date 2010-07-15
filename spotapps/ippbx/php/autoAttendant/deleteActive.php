<?php
require_once('db.php');

if( $_SERVER['REQUEST_METHOD'] != 'POST' ) {
	throw new Exception( 'Invalid access attempt' );
}
# process post
if( array_key_exists( 'active', $_POST ) ) {
	$t_ids =  $_POST['active'];
}

# make sure they're ints
foreach( $t_ids AS $t_key=>$t_id ) {
	if( !is_numeric( $t_id ) ) {
		throw new Exception( 'Invalid data' );
	}
	$t_ids[$t_key] = (int) $t_id;	
}

# delete 
$t_sql = "DELETE FROM active_prompts WHERE id IN ( " . join( ", ", $t_ids ) . " )";
$t_result = mysql_query( $t_sql );
$t_affected = mysql_affected_rows( $t_result );

# redirect
header( "Location: autoAttendant.php" );


