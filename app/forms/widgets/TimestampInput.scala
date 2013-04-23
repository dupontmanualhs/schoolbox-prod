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
	  <input type="text" name={ name } placeholder="mm/dd/yyyy" class={"datepicker" + Name+" datepicker"} value={ if (value.isEmpty) "" else value(0) }></input> % attrs % reqAttr % attrList ++
	  <input type="text" name={ name } placeholder="hh:mm AM/PM" class="timepicker" value={ if (value.isEmpty) "" else value(1) }></input> % attrs % reqAttr % attrList
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
</script><script>
	$(function() {{
      $('.timepicker').timepicker({{
		showPeriod: true,
    	showLeadingZero: true
      }});
    }});
	</script><script type="text/javascript">
	jQuery(function($){{
		$('.datepicker').mask('99/99/9999',{{placeholder:'_'}});
  	}});
  </script><script type="text/javascript">
	jQuery(function($){{
		$.mask.definitions['`']='[apAP]';
		$('.timepicker').mask('99:99 `M',{{placeholder:'_'}});
  	}});
  </script>
}