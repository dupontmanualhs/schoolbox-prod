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
    input.ctype("text").name(name).placeholder("hh:mm AM/PM").cls("timepicker").value(theValue).toXML % attrs % reqAttr % attrList
  }
  
  override def scripts: NodeSeq = 
    Seq(script.ctype("text/javascript")(
	"""$(function() {
      $('.timepicker').timepicker({
		showPeriod: true,
    	showLeadingZero: true
      });
    });"""),
	script.ctype("text/javascript")(
    """jQuery(function($) {
	  	$.mask.definitions['5']='[012345]';
	  	$.mask.definitions['1']='[01]';
		$.mask.definitions['`']='[apAP]';
		$('.timepicker').mask('19:59 `M', { placeholder:'_' });
  	});""")).toXML
}