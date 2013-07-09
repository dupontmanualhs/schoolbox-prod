package forms.widgets

import scala.xml._
import java.util.UUID
import forms.validators.ValidationError

import scalatags._

class DateInput(
  required: Boolean,
  attrs: MetaData = Null,
  uuid: UUID) extends Widget(required, attrs) {
  
  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    val attrsToAppend: MetaData = attrs.append(reqAttr).append(attrList).append(new UnprefixedAttribute("id", Text(uuid.toString), Null))
    Seq(div.cls("well")(
      div.cls("input-append")(
      input.ctype("text").attr("data-format" -> "MM/dd/yyyy"),
      span.cls("add-on")(i.attr("data-time-icon" -> "icon-time", "data-date-icon" -> "icon-calendar")(" "))).toXML % attrsToAppend),
      script.ctype("text/javascript")(s"""$$(function() { $$('#${uuid.toString}').datetimepicker({ language: 'en', pick12HourFormat: true, pickTime: false }); });""")).toXML

  }
  
  override def scripts: NodeSeq = {
    Seq(
      stylesheet(controllers.routes.Assets.at("stylesheets/bootstrap-datetimepicker.min.css").toString),
      javascript(controllers.routes.Assets.at("javascripts/bootstrap-datetimepicker.min.js").toString)
    ).toXML
  }
}