package forms.widgets

import scala.xml._
import java.util.UUID
import forms.validators.ValidationError
import java.sql.Time

import scalatags._

class TimeInput(
  required: Boolean,
  attrs: MetaData = Null) extends Widget(required, attrs) { 

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    val theValue = if (value.isEmpty) "" else value(0)
    <div id="timepicker" class="timepicker input-append">
    		<input data-format="hh:mm" type="text"/> % attrs % reqAttr % attrList ++
    		<span class="add-on">
    			<i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
    		</span>
    </div>
  }
  
  override def scripts: NodeSeq = 
    <script type="text/javascript"
     src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.8.3/jquery.min.js">
    </script> 
    <script type="text/javascript"
     src="http://netdna.bootstrapcdn.com/twitter-bootstrap/2.2.2/js/bootstrap.min.js">
    </script>
    <script type="text/javascript"
     src="http://tarruda.github.com/bootstrap-datetimepicker/assets/js/bootstrap-datetimepicker.min.js">
    </script>
    <script type="text/javascript"
     src="http://tarruda.github.com/bootstrap-datetimepicker/assets/js/bootstrap-datetimepicker.pt-BR.js">
    </script>
    <script type="text/javascript">
      $(function(){{
		  $('.timepicker').datetimepicker({{
		  	pickDate: false,
		  	pickSeconds: false,
		  	pick12HourFormat: true
		  }});
	  }});
    </script>
    
    
    /*Seq(script.ctype("text/javascript")(
	"""$(function() {{
      $('.timepicker').timepicker({{
		showPeriod: true,
    	showLeadingZero: true
      }});
    }});"""),
	script.ctype("text/javascript")(
    """jQuery(function($){{
	  	$.mask.definitions['5']='[012345]';
	  	$.mask.definitions['1']='[01]';
		$.mask.definitions['`']='[apAP]';
		$('.timepicker').mask('19:59 `M',{{placeholder:'_'}});
  	}});""")).toXML*/
}