package forms.fields

import forms.validators.ValidationError
import java.sql.Time
import forms.widgets.TimeInput

abstract class BaseTimeField[T](name: String)(implicit man: Manifest[T]) extends Field[T](name) {
  
  val uuid = java.util.UUID.randomUUID()
  
  override def widget = new TimeInput(required, uuid=uuid)
}

class TimeField(name: String) extends BaseTimeField[Time](name) {
 
  def asValue(s: Seq[String]): Either[ValidationError, Time] =
    try {
      var splitString = s(0).split(" ")
      var splitTime = splitString(0).split(":")
      
      var hours = splitTime(0).toInt
      if (hours == 12 && splitString(1) == "AM") hours = 0
      if (splitString(1) == "PM" && hours != 12) hours = hours + 12
      
      splitTime(0) = hours.toString
      Right(Time.valueOf(splitTime(0) + ":" + splitTime(1) + ":00"))
    } catch {
      case _ => Left(ValidationError("Please make sure input is a valid time"))
    }
}

class TimeFieldOptional(name: String) extends BaseTimeField[Option[Time]](name) {
  override def required = false
  
  def asValue(s: Seq[String]): Either[ValidationError, Option[Time]] =
    s match {
    case Seq() => Right(None)
    case Seq(str) => try {
      var splitString = s(0).split(" ")
      var splitTime = splitString(0).split(":")
      
      var hours = splitTime(0).toInt
      if (hours == 12 && splitString(1) == "AM") hours = 0
      if (splitString(1) == "PM" && hours != 12) hours = hours + 12
      
      splitTime(0) = hours.toString
      Right(Option(Time.valueOf(splitTime(0) + ":" + splitTime(1) + ":00")))
    } catch {
      case _ => Left(ValidationError("Please make sure input is a valid time"))
    }
      
    }
  
}