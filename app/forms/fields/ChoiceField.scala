package forms.fields

import forms.widgets.SelectInput
import forms.validators.ValidationError

abstract class BaseChoiceField[T, U](name: String, choices: List[(String, T)]) extends Field[U](name) {
  def validators = Nil
  override def widget = new SelectInput(required, choices.map(_._1))
}

class ChoiceField[T](name: String, choices: List[(String, T)]) extends BaseChoiceField[T, T](name, choices) {
  def asValue(s: Seq[String]): Either[ValidationError, T] = {
    s match {
      case Seq(s) => try {
        val index = s.toInt
        if (index < 0 || index > choices.length - 1) {
          Left(ValidationError("Illegal value submitted."))
        } else {
          Right(choices(index)._2)
        }
      } catch {
        case e: NumberFormatException => Left(ValidationError("Illegal value submitted."))
      }
      case _ => Left(ValidationError("Expected a single value, got none or multiple."))
    }
  }
  
  override def asStringSeq(value: Option[T]): Seq[String] = value match {
    case None => List("-1")
    case Some(t) => List(choices.map(_._2).indexOf(t).toString)
  }
}

/*class ChoiceFieldOptional[T](name: String, choices: List[(String, T)]) extends BaseChoiceField[T, Option[T]](name, choices) {
  
}

class ChoiceFieldMultiple[T](name: String, choices: List[(String, T)]) extends BaseChoiceField[T, List[T]](name, choices) {
  
}*/