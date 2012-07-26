package forms.fields

import scala.xml.{Attribute, MetaData, Null, Text}

import forms.validators._
import forms.widgets._

class TinyMCEField(name: String) extends TextField(name) {
  override def widget = new Textarea(required)
}

class OptionalTinyMCEField(name: String) extends TextFieldOptional(name) {
  override def widget = new Textarea(required)
}
