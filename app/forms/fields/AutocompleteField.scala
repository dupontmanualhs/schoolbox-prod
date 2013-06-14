package forms.fields

import forms.validators.ValidationError
import forms.widgets.AutocompleteInput
import forms._

abstract class BaseAutocompleteField[T](name: String, list: List[Any])(implicit man: Manifest[T]) extends Field[T](name) {
  
  val uuid=java.util.UUID.randomUUID()
  
  override def widget = new AutocompleteInput(required, toJsArray(list), uuid=uuid)
  
  def toJsArray(s: List[Any]): String = "[" + s.map(x => "\""+x.toString+"\"").reduce(_+","+_) + "]"
}

class AutocompleteField(name: String, list: List[Any]) extends BaseAutocompleteField[String](name, list) {
  
  def asValue(s: Seq[String]): Either[ValidationError, String] = 
    if(s.isEmpty) Left(ValidationError("This Field is Required"))
    else Right(s(0))

}

class AutocompleteFieldOptional(name: String, list: List[Any]) extends BaseAutocompleteField[Option[String]](name, list) {
  override def required = false
  
    def asValue(s: Seq[String]): Either[ValidationError, Option[String]] = 
      if(s.isEmpty) Right(None)
      else Right(Some(s(0)))
}