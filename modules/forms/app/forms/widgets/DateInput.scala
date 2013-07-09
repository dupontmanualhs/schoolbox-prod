package forms.widgets

import scala.xml._
import java.util.UUID
import forms.validators.ValidationError

import scalatags._

class DateInput(
  required: Boolean,
  attrs: MetaData = Null,
  uuid: UUID) extends Widget(required, attrs) {
  
  val theUuid: String = uuid.toString
  
  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    val theValue = if (value.isEmpty) "" else value(0)
    <div id="datepicker" class="datepicker input-append">
    		{<input data-format="hh:mm" type="text"/> % attrs % reqAttr % attrList ++
    		<span class="add-on">
    			<i data-time-icon="icon-time" data-date-icon="icon-calendar"></i>
    		</span>}
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
	    $('.datepicker').datetimepicker({{
		  pickTime: false
		}});
	  }});
    </script>
    
    
    
    /*Seq(script.ctype("text/javascript")(
		  changeMonth: true,
  		  changeYear: true,
  		  altField: '.${theUuid}',
  		  altFormat: 'DD, d MM, yy',
  		  showOtherMonths: true,
  		  selectOtherMonths: true,
  		  showOn: 'both',
  		  buttonImageOnly: true,
  		  buttonImage: '/assets/images/calendar.jpeg',
  		  shortYearCutoff: 99,
  		  buttonText: 'Chooser'
      });
    });"""),
    script.ctype("text/javascript")(
	"""jQuery(function($){
		$('.datepicker').mask('99/99/99?99',{ placeholder:'_' });
  	}});""")).toXML*/
}