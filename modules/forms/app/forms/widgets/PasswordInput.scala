package forms.widgets

import scala.xml._

/**
 * A widget that creates a password field.
 */
class PasswordInput(
    required: Boolean,
    attrs: MetaData = Null,
    val renderValue: Boolean = false) extends Input(required, attrs) {

  // val show: Boolean = false;
  /**
   * Sets the input type to be password.
   */
  def inputType: String = "password"
    
  /**
   * Renders the field using xml and deleting the value if renderValue is false.
   */  
  override def render(name: String, value: Seq[String], attrList: MetaData) = {
    //super.render(name, if (renderValue) value else Nil, attrList)
    val valueAttr = value match {
      case Seq(s) => new UnprefixedAttribute("value", Text(s), Null)
      // fails silently if we get too many values for a single-valued field
      case _ => Null
    }
    if(!step2) <input type={inputType} name={ name } onkeyup='chkPass(this.value);' /> % attrs % reqAttr % attrList % valueAttr
    else <input type={inputType} step="any" name={ name } onkeyup='chkPass(this.value);' /> % attrs % reqAttr % attrList % valueAttr
  }
}