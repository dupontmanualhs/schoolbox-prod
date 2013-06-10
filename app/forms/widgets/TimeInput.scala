package forms.widgets

import scala.xml._
import java.util.UUID
import forms.validators.ValidationError
import java.sql.Time

class TimeInput(
  required: Boolean,
  attrs: MetaData = Null) extends Widget(required, attrs) { 

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" name={ name } placeholder="hh:mm AM/PM" class="timepicker" value={ if (value.isEmpty) "" else value(0) }></input> % attrs % reqAttr % attrList
  }
  
  override def scripts: NodeSeq = 
  <script>
	$(function() {{
      $('.timepicker').timepicker({{
		showPeriod: true,
    	showLeadingZero: true
      }});
    }});
	</script><script type="text/javascript">
	jQuery(function($){{
		$.mask.definitions['`']='[apAP]';
		$('.timepicker').mask('99:99 `M',{{placeholder:'_'}});
  	}});
  </script>
}