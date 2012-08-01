package forms.widgets

import scala.xml._

class Textarea(
    required: Boolean,
    attrMap: MetaData = Null,
    tinyMCE: Boolean = false) extends 
    	Widget(required, 
    	    new UnprefixedAttribute("cols", Text("40"), 
    	    	new UnprefixedAttribute("rows", Text("10"), Null).append(attrMap))) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null): NodeSeq = {
    val tinymce = if (tinyMCE) new UnprefixedAttribute("class", Text("tinymce"), Null) else Null 
    <textarea name={ name }>{ if (value.isEmpty) "" else value(0) }</textarea> % attrs.append(attrList) % tinymce
  }

}