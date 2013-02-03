package forms.widgets

import scala.xml._
import forms.validators.ValidationError
import java.sql.Date

class DateInput(
  required: Boolean,
  attrs: MetaData = Null) extends Widget(required, attrs) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" name={ name } placeholder="mm/dd/yyyy" class="datepicker">{ if (value.isEmpty) "" else value(0) }</input>
    <input disabled="true" id="alternateDP" class="alternateDP" size="30" type="text"/>
  }
  
  override def scripts: NodeSeq = 
  <script>
    $(function() {{
      $( '.datepicker' ).datepicker({
		  //changeMonth: true,
		  //changeYear: true,
		  //showOtherMonths: true,
		  //selectOtherMonths: true,
		  //showOn: "button",
		  //buttonImage: "images/calendar.gif",
		  //buttonImageOnly: true,
		  //altField: ".alternateDP",
		  //altFormat: "DD, d MM, yy"
      });
    }});
  </script>
}