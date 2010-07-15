<?php
require_once( 'db.php' );
# get list of available prompts
$t_sql = "SELECT * FROM available_prompts";
$t_result = mysql_query( $t_sql );
$t_available = array();
$t_available_count = 0; 
if( $t_result !== false ) {
	$t_available_count = mysql_num_rows( $t_result );
	for( $i=0; $i< $t_available_count; $i++ ) {
		$t_id = mysql_result( $t_result, $i, 'id' );
		$t_description = mysql_result( $t_result, $i, 'description' );
		$t_available[$t_id] = $t_description;	
	}
}

# get list of active prompts
$t_sql = "SELECT active.*, available.description FROM active_prompts active, available_prompts available WHERE active.available_prompts_id=available.id";

$t_result = mysql_query( $t_sql );
$t_active_greetings = array();
$t_active_count = 0; 
if( $t_result !== false ) {
	$t_active_count = mysql_num_rows( $t_result );
	for( $i=0; $i< $t_active_count; $i++ ) {
		$t_id = mysql_result( $t_result, $i, 'id' );
		$t_start = mysql_result( $t_result, $i, 'start' );
		$t_end = mysql_result( $t_result, $i, 'end' );
		$t_description = mysql_result( $t_result, $i, 'description' );

		$t_active_greetings[$t_id] = array( 'start'=>$t_start, 'end'=>$t_end, 'description'=>$t_description );
	}
}
mysql_close();
include_once( "templates/layout.phtml" );
