package forms.fields

import forms.validators.ValidationError
import java.sql.Date
import forms.widgets.DateInput

abstract class BaseDateField[T](name: String)(implicit man: Manifest[T]) extends Field[T](name) {
  
  val uuid=java.util.UUID.randomUUID()
  
  override def widget = new DateInput(required, uuid=uuid)
  
  def validators = Nil
  
}

class DateField(name: String) extends BaseDateField[Date](name) {
  
  def asValue(s: Seq[String]): Either[ValidationError, Date] =
    try {
      val splitdate = s(0).split("/")
      
      if(splitdate(0).length == 1) splitdate(0) = "0"+splitdate(0)
      if(splitdate(1).length == 1) splitdate(1) = "0"+splitdate(0)
      if(splitdate(2).length == 2) splitdate(2) = "20"+splitdate(2)
      if(splitdate(2).length < 4) Left(ValidationError("Make sure you input a 4 digit year."))
      
      Right(Date.valueOf(splitdate(2)+"-"+splitdate(0)+"-"+splitdate(1)))
    } catch {
      case _ => Left(ValidationError("Please make sure input is in mm/dd/yyyy format."))
    }
  
}

class DateFieldOptional(name: String) extends BaseDateField[Option[Date]](name) {
  override def required = false
  
    def asValue(s: Seq[String]): Either[ValidationError, Option[Date]] =
    try {
      val splitdate = s(0).split("/")
      
      if(splitdate(0).length == 1) splitdate(0) = "0"+splitdate(0)
      if(splitdate(1).length == 1) splitdate(1) = "0"+splitdate(0)
      if(splitdate(2).length == 2) splitdate(2) = "20"+splitdate(2)
      if(splitdate(2).length < 4) Left(ValidationError("Make sure you input a 4 digit year."))
      
      Right(Option[Date](Date.valueOf(splitdate(2)+"-"+splitdate(0)+"-"+splitdate(1))))
    } catch {
      case _ => Left(ValidationError("Please make sure input is in mm/dd/yyyy format."))
    }
}