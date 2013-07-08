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
    input.ctype("text").name(name).placeholder("mm/dd/yyyy").cls(s"datepicker${theUuid}", "datepicker").value(theValue).toXML % attrs % reqAttr % attrList ++
    input.ctype("text").style("background-color" -> "#C0C0C0").name(theUuid).placeholder("DD, d MM, yy").cls(theUuid, "datepicker").attr("disabled" -> "true", "is" -> theUuid, "size" -> "30").toXML
  }
  
  override def scripts: NodeSeq =
    Seq(script.ctype("text/javascript")(
    s"""$$(function() {
      $$('.datepicker${theUuid}').datepicker({
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
  	});""")).toXML
}