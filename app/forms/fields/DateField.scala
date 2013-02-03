package forms.fields
import forms.validators.ValidationError
import java.sql.Date
import forms.widgets.DateInput

class DateField(name: String) extends Field[Date](name) {
  override def widget = new DateInput(required)
  
  def asValue(s: Seq[String]): Either[ValidationError, Date] =
    try {
      val splitdate = s(0).split("/")

      if (splitdate(0).length == 1) splitdate(0) = "0" + splitdate(0)
      if (splitdate(1).length == 1) splitdate(1) = "0" + splitdate(1)
      if (splitdate(2).length == 2) Left(ValidationError("make sure you input a 4 digit year"))

      Right(Date.valueOf(splitdate(2) + "-" + splitdate(0) + "-" + splitdate(1)))
    } catch {
      case _ => Left(ValidationError("Please make sure input is in mm/dd/yyyy format."))
    }
}