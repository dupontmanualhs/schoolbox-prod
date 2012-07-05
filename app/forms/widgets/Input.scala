package forms.widgets

import scala.xml._
import forms.validators.ValidationError

abstract class Input(
    attrs: MetaData = Null,
    isRequired : Boolean = false) extends Widget(attrs, isRequired) {

  def inputType: String
  
  def render(name: String, value: Seq[String], attrList: MetaData): NodeSeq = {
    val valueAttr = value match {
      case Seq(s) => new UnprefixedAttribute("value", Text(s), Null)
      // fails silently if we get too many values for a single-valued field
      case _ => Null
    }
    <input type={ inputType } name={ name } /> % attrs % attrList % valueAttr       
  }
}