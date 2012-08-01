package forms.fields

import forms.widgets.SelectInput

abstract class BaseChoiceField[T](name: String, choices: List[(String, T)]) extends Field[T](name) {
  override def widget = new SelectInput(required, choices.map(_._1))
}

class ChoiceField[T](name: String, choices: List[(String, T)]) extends BaseChoiceField[T](name, choices) {
  
}