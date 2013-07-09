package forms.widgets

import scala.xml._
import scalatags._
import forms.validators.ValidationError

class AutocompleteInput(
  required: Boolean,
  array: String = "",
  uuid: java.util.UUID,
  attrs: MetaData = Null) extends Widget(required, attrs) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    val valueAttr = if (value.isEmpty) "" else value(0)
    input.ctype("text").name(name).cls("ac" + uuid.toString).value(valueAttr).attr(attrList.asAttrMap.toList :_*).toXML
  }

  override def scripts: NodeSeq =
    javascript()(s"""$$(function() { var availableTags = ${ Unparsed(array.toString) };
  $$('.ac${uuid.toString}').autoComplete({ source: availableTags }); });""").toXML
}