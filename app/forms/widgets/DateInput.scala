package forms.widgets

import scala.xml._
import java.util.UUID

class DateInput(
  required: Boolean,
  attrs: MetaData = Null,
  uuid: UUID) extends Widget(required, attrs) {
  
  val Name:String = uuid.toString()
  
  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" name={ name } value={if(value.isEmpty) "" else value(0) } placeholder="mm/dd/yyyy" class={"datepicker"+Name}></input>
    <input disabled="true" style="color: #808080" name={ Name } is={ Name } placeholder="DD, d MM, yy" class={ Name } size="30" type="text"/>
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
  				buttonImage: 'images/calendar.jpeg'
  			}});
  		}});
  </script>

}