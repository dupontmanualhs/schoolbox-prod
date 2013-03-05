package forms.widgets

import scala.xml._
import java.util.UUID
import forms.validators.ValidationError
import java.sql.Time

class TimeInput(
  required: Boolean,
  attrs: MetaData = Null, 
  uuid: UUID) extends Widget(required, attrs) {
  
  val Name: String = uuid.toString 

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" name={ name } placeholder="hh:mm AM/PM" class={"timepicker" + Name}>{ if (value.isEmpty) "" else value(0) }</input>
  }
  
  override def scripts: NodeSeq = 
  <script>
    $(function() {{
      $('.timepicker{Name}').timepicker({{
		showPeriod: true,
    	showLeadingZero: true
      }});
    }});
  </script>
}