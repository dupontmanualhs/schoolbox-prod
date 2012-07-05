package forms.widgets

import scala.xml._

class PasswordInput(
    attrs: MetaData = Null,
    isRequired: Boolean = false,
    val renderValue: Boolean = false) extends Input(attrs, isRequired) {

  def inputType: String = "password"
    
  override def render(name: String, value: Seq[String], attrList: MetaData) = {
    super.render(name, if (renderValue) value else Nil, attrList)
  }
}