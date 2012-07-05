package forms.widgets

import scala.xml._

class HiddenInput(
    attrs: MetaData = Null,
    isRequired: Boolean = false) 
    extends Input(attrs, isRequired) {
  
  override def isHidden: Boolean = true
  
  def inputType: String = "hidden"
}

object HiddenInput extends WidgetCompanion {
  
}