package forms.fields

import scala.xml.{Attribute, MetaData, Null, Text}


import forms.validators._
import forms.widgets._

class TextField(
	required: Boolean = true,
    widget: Option[Widget] = None,
    label: Option[String] = None,
    initial: Option[String] = None,
    helpText: Option[String] = None,
    extraErrorMessages: Map[String, String] = Map(),
    extraValidators: List[Validator[String]] = Nil,
    localize: Boolean = false,
    val minLength: Option[Int] = None,
    val maxLength: Option[Int] = None) 
        extends Field[String](required, widget, label, 
            initial, helpText, 
            extraErrorMessages, 
            extraValidators ++ TextField.minAndMaxValidators(minLength, maxLength),
            localize) {
    
  def asValue(strs: Seq[String]): Either[ValidationError, Option[String]] = {
    strs match {
      case Seq() => Right(None)
      case Seq(s) => Right(if (s == "") None else Some(s))
      case _ => Left(ValidationError("Got multiple values for a single TextField."))
    }
  }
  
  override def widgetAttrs(widget: Widget): MetaData = {
    val maxLengthAttr: MetaData = if (this.maxLength.isDefined && (widget.isInstanceOf[TextInput] || widget.isInstanceOf[PasswordInput])) {
      Attribute("maxlength", Text(maxLength.get.toString), Null)
    } else Null
    super.widgetAttrs(widget).append(maxLengthAttr)
  }
}

object TextField {
  def minAndMaxValidators(minLength: Option[Int], maxLength: Option[Int]): List[Validator[String]] = {
    val min = minLength match {
      case None => Nil
      case Some(min) => List(new MinLengthValidator(min, (s => "This value must have at least %d characters. (It has %d.)".format(min, s.length))))
    }
    val max = maxLength match {
      case None => Nil
      case Some(max) => List(new MaxLengthValidator(max, (s => "This value must have no more than %d characters. (It has %d.)".format(max, s.length))))
    }
    min ++ max
  }
}