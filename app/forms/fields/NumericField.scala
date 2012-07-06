package forms.fields

import forms.widgets._
import forms.validators._

class NumericField[T](
    required: Boolean = true,
    widget: Option[Widget] = None,
    label: Option[String] = None,
    initial: Option[T] = None,
    helpText: Option[String] = None,
    extraErrorMessages: Map[String, String] = Map(),
    extraValidators: List[Validator[T]] = Nil,
    localize: Boolean = false,
    val minValue: Option[T] = None,
    val maxValue: Option[T] = None)(implicit n: Numeric[T], man: Manifest[T])
        extends Field[T](required, widget, label, initial, helpText,
            extraErrorMessages,
            extraValidators ++ NumericField.minAndMaxValidators[T](minValue, maxValue),
            localize) {
  
  def asValue(strs: Seq[String]): Either[ValidationError, Option[T]] = {
    val (toT, errorMsg) = NumericField.conversionFunction[T]
    strs match {
      case Seq() => Right(None)
      case Seq(s) => try {
        Right(Some(toT(s.trim)))
      } catch {
        case e: NumberFormatException => Left(ValidationError(errorMsg(s)))
      }
      case _ => Left(ValidationError("Got multiple values for a single NumericField."))
    }
  }
}

object NumericField {
  def minAndMaxValidators[T](minValue: Option[T], maxValue: Option[T])(implicit n: Numeric[T]): List[Validator[T]] = {
    val min = minValue match {
      case None => Nil
      case Some(min) => List(new MinValueValidator[T](min, (x => "This value must be at least %s.".format(min))))
    }
    val max = maxValue match {
      case None => Nil
      case Some(max) => List(new MaxValueValidator[T](max, (x => "This value must be at most %s.".format(max))))
    }
    min ++ max
  }
  
  def conversionFunction[T](implicit man: Manifest[T]): ((String => T), (String => String)) = {
    if (man.erasure == classOf[Int]) {
      ((s: String) => s.toInt.asInstanceOf[T], (s: String) => "This value must be a positive or negative whole number.")
    } else if (man.erasure == classOf[Double]) {
      ((s: String) => s.toDouble.asInstanceOf[T], (s: String) => "This value must be a number.")
    } else {
      throw new Exception("Numeric field only supported for Int and Double.")
    }
  }
}