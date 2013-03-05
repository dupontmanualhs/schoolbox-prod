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
    <input type="text" name={ name } value={if(value.isEmpty) "" else value(0) } placeholder="mm/dd/yyyy" class={"datepicker"+Name}></input>
    <input disabled="true" style="background-color: #C0C0C0" name={ Name } is={ Name } placeholder="DD, d MM, yy" class={ Name } size="30" type="text"/>
  }
  
  override def scripts: NodeSeq =
    <script>
		  $(function() {{
		  	$('.datepicker{Name}').datepicker({{
  				changeMonth: true,
  				changeYear: true,
  				altField: '.{Name}',
  				altFormat: 'DD, MM, yy',
  				showOtherMonths: true,
  				selectOtherMonths: true,
  				showOn: 'both',
  				buttonImageOnly: true,
  				buttonImage: '/assets/images/calendar.jpeg',
  				shortYearCutoff: 99,
  				buttonText: 'Chooser'
  			}});
  		}});
  </script>

}