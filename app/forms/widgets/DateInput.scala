package forms.widgets

import scala.xml._
import java.util.UUID
import forms.validators.ValidationError
import java.sql.Date

class DateInput(
  required: Boolean,
  attrs: MetaData = Null,
  uuid: UUID) extends Widget(required, attrs) {
  
  val Name:String = uuid.toString
  
  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" name={ name } placeholder="mm/dd/yyyy" class={"datepicker"+Name}>{ if (value.isEmpty) "" else value(0) }</input>
    <input disabled="true" name={ Name } id={ Name } placeholder="DD, d MM, yy" class={ Name } size="30" type="text"/>
  }
  
  override def scripts: NodeSeq = 
  <script>
    $(function() {{
      $('.datepicker{Name}').datepicker({{
		  changeMonth: true,
		  changeYear: true,
		  altField: '.{Name}',
		  altFormat: 'DD, d MM, yy',
		  showOtherMonths: true,
		  selectOtherMonths: true,
		  showOn: 'button',
  		  buttonImageOnly: false,
		  buttonImage: 'images/calendar.gif'
      }});
    }});
  </script>
}