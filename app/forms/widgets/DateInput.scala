package forms.widgets

import scala.xml._
import forms.validators.ValidationError
import java.sql.Date

class DateInput(
  required: Boolean,
  attrs: MetaData = Null) extends Widget(required, attrs) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    
/*      
  <meta charset="utf-8" />
  <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.0/themes/base/jquery-ui.css" />
  <script src="http://code.jquery.com/jquery-1.8.3.js"></script>
  <script src="http://code.jquery.com/ui/1.10.0/jquery-ui.js"></script>
  <link rel="stylesheet" href="/resources/demos/style.css" />
  <script>
  $(function() {
    $( ".datepicker" ).datepicker({
		showOn: "button",
		buttonImage: "images/calendar.gif",
		buttonImageOnly: true,
		changeMonth: true,
		changeYear:true
	});
  });
  </script>
*/
    
    
    <input disabled="true" type="text" name={ name } value="mm/dd/yyyy" class=".datepicker">{ if (value.isEmpty) "" else value(0) }</input>
  }
}