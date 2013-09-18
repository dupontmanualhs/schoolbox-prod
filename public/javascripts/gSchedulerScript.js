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
				$.param(object),
				function (data) {
			      if(data == "success") { 
			        window.location.replace("/conferences/myConferences");
			      } else {
			    	alert(data);  
			      }
		        }
		  );
	  }
	);
});