package forms.widgets

import scala.xml._
import java.util.UUID
import forms.validators.ValidationError
import java.sql.Timestamp

class TimestampInput(
    required: Boolean,
    attrs: MetaData = Null,
    uuid: UUID) extends Widget(required, attrs) {
  
	val Name: String = uuid.toString
	
	def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
	  <input type="text" name={ name } placeholder="mm/dd/yy" class={"datepicker" + Name}>{ if (value.isEmpty) "" else value(0) }</input>
	  <input type="text" name={ name } placeholder="hh:mm AM/PM" class={"timepicker" + Name}>{ if (value.isEmpty) "" else value(1) }</input>
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
  		  showOn: 'both',
  		  buttonImageOnly: true,
  		  buttonImage: '/assets/images/calendar.jpeg',
  		  shortYearCutoff: 99,
  		  buttonText: 'Chooser'
      }});
    }});
	$(function() {{
      $('.timepicker{Name}').timepicker({{
		showPeriod: true,
    	showLeadingZero: true
      }});
    }});
	</script><script type="text/javascript">
  	jQuery(function($){{
  		$('.datepicker{Name}').mask('99/99/9999', {{placeholder:' '}});
  	}});
  </script><script type="text/javascript">
  	jQuery(function($){{
  		$('.timepicker{Name}').mask('99:99 aa', {{placeholder:' '}});
  	}});
  </script>
}