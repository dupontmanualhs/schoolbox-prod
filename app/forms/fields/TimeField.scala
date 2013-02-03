package forms.fields

import forms.validators.ValidationError
import java.sql.Time
import forms.widgets.TimeInput

class TimeField(name: String) extends Field[Time](name) {
  override def widget = new TimeInput(required)
  
  def asValue(s: Seq[String]): Either[ValidationError, Time] =
    try {
      Right(Time.valueOf(s+":00"))
    } catch {
      case _ => Left(ValidationError("Please make sure input is a valid time"))
    }
}