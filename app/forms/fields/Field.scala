package forms.fields

import scala.xml._

import forms.widgets._
import forms.validators._

abstract class Field[T](
    val required: Boolean = true,
    overrideWidget: Option[Widget] = None,
    val label: Option[String] = None,
    initialVal: Option[T] = None,
    val helpText: Option[String] = None,
    extraErrorMessages: Map[String, String] = Map(),
    extraValidators: List[Validator[T]] = Nil,
    localize: Boolean = false) {
  val widget: Widget = overrideWidget.getOrElse(new TextInput()) 
  val initial: Seq[String] = asStringSeq(initialVal)
    
  private[this] lazy val _validators: List[Validator[T]] = extraValidators
  def validators = _validators
  
  val spacesSameAsBlank = true
  
  private[this] lazy val _errorMessages: Map[String, String] = {
    Map("required" -> "This field is required.",
        "invalid" -> "Enter a valid value.") ++ extraErrorMessages
  }
  def errorMessages = _errorMessages
  
  def asStringSeq(value: Option[T]): Seq[String] = value match {
    case Some(t) => List(t.toString)
    case None => Nil
  }
  
  def asValue(s: Seq[String]): Either[ValidationError, Option[T]]
  
  def clean(rawData: String): Either[ValidationError, Option[T]] = {
    clean(List(rawData))
  }
  
  def clean(rawData: Seq[String]): Either[ValidationError, Option[T]] = {
    checkRequired(rawData).fold(
        Left(_), 
        asValue(_).fold(
        	Left(_), validate(_)))
  }
  
  def checkRequired(rawData: Seq[String]): Either[ValidationError, Seq[String]] = {
    rawData match {
      case Seq() => if (this.required) Left(ValidationError(errorMessages("required")))
      	  else Right(rawData)
      case Seq(strs@_*) => {
        if (strs.exists(s => (if (spacesSameAsBlank) s.trim else s) != "")) {
          Right(rawData)
        } else if (required) {
          Left(ValidationError(errorMessages("required")))
        } else {
          Right(Nil)
        }
      } 
    }
  }
  
  def validate(value: Option[T]): Either[ValidationError, Option[T]] = value match {
    case None => Right(None)
    case Some(t) => {
      val errors = ValidationError(this.validators.flatMap(_.apply(t)))
      if (errors.isEmpty) Right(Some(t)) else Left(errors)
    }
  } 
  
  def boundData(data: Seq[String], initial: Seq[String]): Seq[String] = data 
  
  def widgetAttrs(widget: Widget): MetaData = Null
}

object Field {
  
}