<?php
require_once( 'db.php' );

if( $_SERVER['REQUEST_METHOD'] != 'POST' ) {
	throw new Exception( 'Invalid access attempt' );
}

# process post
if( array_key_exists( 'start', $_POST ) ) {
	$t_start =  strtotime( $_POST['start']);
}
if( array_key_exists( 'end', $_POST ) ) {
	$t_end =  strtotime( $_POST['end'] );
}
if( array_key_exists( 'available_prompt_id', $_POST ) ) {
	$t_available_prompt_id = (int) $_POST['available_prompt_id'];
}
if( $t_start && $t_end && $t_available_prompt_id ) {
	# insert
	$t_sql = "INSERT INTO active_prompts ( start, end, available_prompts_id ) VALUES ( $t_start, $t_end, $t_available_prompt_id )";
	$t_result = mysql_query( $t_sql );
} else {
	throw new Exception( 'Invalid form values' );
}

# redirect
header( "Location: autoAttendant.php" );
