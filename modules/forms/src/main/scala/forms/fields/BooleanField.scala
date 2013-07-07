package forms.fields
import forms.validators.ValidationError
import forms.widgets.CheckboxInput

class BooleanField(name: String) extends Field[Boolean](name) {
  override def required = false
  override def widget = new CheckboxInput(required, List("true"))
  
  def asValue(s: Seq[String]): Either[ValidationError, Boolean] = s match{
      case Seq() => Right(false)
      case Seq(str) => if(str == "true") Right(true) else if(str == "") Right(false) else Left(ValidationError("Boolean Fields need to be either selected or not."))
      case _ => Left(ValidationError("Boolean Fields need to be either selected or not."))
    }
}