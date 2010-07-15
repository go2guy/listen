$(document).ready( function() {
	/* Add datepicker for fields with .datepicker class */
	$(function(){
		$('.datepicker').each( function() {
			$(this).AnyTime_picker();
		});
	});
});
