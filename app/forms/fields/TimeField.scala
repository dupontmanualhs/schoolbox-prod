package forms.fields

import forms.validators.ValidationError
import java.sql.Time
import forms.widgets.TimeInput

abstract class BaseTimeField[T](name: String)(implicit man: Manifest[T]) extends Field[T](name) {
  
  override def widget = new TimeInput(required)
}

class TimeField(name: String) extends BaseTimeField[Time](name) {
 
  def asValue(s: Seq[String]): Either[ValidationError, Time] =
    try {
      val splitString = s(0).split(" ")
      val splitTime = splitString(0).split(":")
      splitString(1)=splitString(1).capitalize
      
      var hours = splitTime(0).toInt
      if (hours > 12) Left(ValidationError("Please make sure you input a valid time."))
      else if (splitTime(1).toInt > 59) Left(ValidationError("Please make sure input is a valid time."))
      else {
        if (hours == 12 && splitString(1) == "AM") hours = 0
        if (splitString(1) == "PM" && hours != 12) hours = hours + 12
      
        splitTime(0) = hours.toString
        Right(Time.valueOf(splitTime(0) + ":" + splitTime(1) + ":00"))
      }
    } catch {
      case e: IllegalArgumentException => Left(ValidationError("Please make sure input is a valid time"))
      case t: Throwable => Left(ValidationError("Please make sure input is a valid time."))
    }
}

class TimeFieldOptional(name: String) extends BaseTimeField[Option[Time]](name) {
  def asValue(s: Seq[String]): Either[ValidationError, Option[Time]] =
    s match {
    case Seq() => Right(None)
    case Seq(str) => try {
      val splitString = s(0).split(" ")
      val splitTime = splitString(0).split(":")
      splitString(1)=splitString(1).capitalize
      
      var hours = splitTime(0).toInt
      if (hours > 12) Left(ValidationError("Please make sure input is a valid time."))
      else {
    	if (hours == 12 && splitString(1) == "AM") hours = 0
    	if (splitString(1) == "PM" && hours != 12) hours = hours + 12
      
    	splitTime(0) = hours.toString
    	Right(Option(Time.valueOf(splitTime(0) + ":" + splitTime(1) + ":00")))
      }
    } catch {
      case e: IllegalArgumentException => Left(ValidationError("Please make sure input is a valid time."))
      case t: Throwable => Left(ValidationError("Please make sure input is a valid time."))
    }
      
    }
  
}