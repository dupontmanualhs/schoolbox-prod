$( document ).ready( function() {
	$('#submit').click( 
	  function () {
		  var object = { "event" : $("#info").attr("event"), 
			             "session" : $("#info").attr("session"),
			             "teacher" : $("#info").attr("teacher"),
			             "slot" : $("[name='selection']:checked").val(),
			             "phone" : $("#phone").val(),
			             "altphone" : $("#altphone").val(),
			             "comments" : $("#comments").val() };
		  $.post('/conferences/checkSlot', 
				{ "event" : $("#info").attr("event"), 
				  "session" : $("#info").attr("session"),
				  "teacher" : $("#info").attr("teacher"),
				  "student" : $("#info").attr("student"),
				  "slot" : $("[name='selection']:checked").val(),
				  "phone" : $("#phone").val(),
				  "altphone" : $("#altphone").val(),
				  "comments" : $("#comments").val()
				}
		);
	  }
	);
});