package forms.widgets

import scala.xml._

class TextInput(
    attrs: MetaData = Null,
    isRequired: Boolean = false) extends Input(attrs, isRequired) {

  def inputType: String = "text"
}

object TextInput extends WidgetCompanion {
  
}