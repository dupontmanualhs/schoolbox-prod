package forms.widgets

import scala.xml._

class Textarea(
    attrMap: MetaData = Null,
    isRequired: Boolean = false)
    extends Widget(new UnprefixedAttribute("cols", Text("40"), 
        new UnprefixedAttribute("rows", Text("10"), Null).append(attrMap)), 
        isRequired) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null): NodeSeq = {
    <textarea name={ name }>{ if (value.isEmpty) "" else value(0) }</textarea> % attrs.append(attrList)
  }

}