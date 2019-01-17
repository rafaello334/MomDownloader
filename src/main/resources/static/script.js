$(document).ready(function() {
	$body = $("body");
	$('#download-form').submit(function() {
		$body.addClass("loading");
	});
});