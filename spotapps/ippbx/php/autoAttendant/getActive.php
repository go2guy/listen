<?php
require_once('../db.php');
# get list of available prompts
$t_now = time();
$t_sql = "SELECT name FROM available_prompts p, active_prompts a WHERE p.id=a.available_prompts_id AND '$t_now' BETWEEN start AND end ORDER BY start DESC LIMIT 0, 1";
$t_result = mysql_query( $t_sql );
$t_available_count = 0; 
$t_prompt_name = 0;
if( $t_result !== false ) {
	$t_available_count = mysql_num_rows( $t_result );
	if( $t_available_count == 1 ) {
		$t_prompt_name = mysql_result( $t_result, 0 );
	}
}
header("content-type: text/xml");
include('templates/getActivePrompt.phtml');
exit;
