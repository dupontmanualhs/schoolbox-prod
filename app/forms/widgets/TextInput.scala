package forms.widgets

import scala.xml._

class TextInput(
    required : Boolean,
    attrs: MetaData = Null) extends Input(required, attrs) {
  
  def inputType: String = "text"
}
