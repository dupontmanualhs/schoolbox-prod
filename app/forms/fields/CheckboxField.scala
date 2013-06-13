package forms.fields

import forms.widgets._
import forms.validators.ValidationError

abstract class BaseCheckboxField[T, U](name: String, choices: List[(String, T)])(implicit man: Manifest[U]) extends Field[U](name) {
  override def widget = new CheckboxInput(required, choices.map(_._1))
  override def asStringSeq(value: Option[U]): Seq[String] = {
    value match {
      case None => List("-1")
      case Some(t) => t match {
        case e: List[T] => {
        	var listReturned = List[String]()
        	for (value2 <- e) {
        		listReturned = choices.map(_._2).indexOf(e).toString :: listReturned
        	}
        	listReturned
        }
        case _ => List("-1")
      }
    }
  }
}

class CheckboxField[T](name: String, choices: List[(String, T)])(implicit man: Manifest[T]) extends BaseCheckboxField[T, List[T]](name, choices) {
  def asValue(s: Seq[String]): Either[ValidationError, List[T]] = {
    try {
    	val LOI = s.map(str => str.toInt) //LOI = listOfIndexes
    	if(LOI.isEmpty) Left(ValidationError("This field is required."))
    	else {
    	  Right(LOI.map(index => choices(index)._2).toList) //returns the objects
    	}
    } catch {
      	case i: IndexOutOfBoundsException => Left(ValidationError("IndexOutOfBoundsException"))
      	case n: NumberFormatException => Left(ValidationError("NumberFomatException"))
    	case e: Throwable => Left(ValidationError("Invalid Input: "+e.toString))
    }
  }
}

class CheckboxFieldOptional[T](name: String, choices: List[(String, T)])(implicit man: Manifest[T]) extends BaseCheckboxField[T, Option[List[T]]](name, choices) {
  override def required = false
  def asValue(s: Seq[String]): Either[ValidationError, Option[List[T]]] = {
    try {
    	val LOI = s.map(str => str.toInt) //LOI = listOfIndexes
    	if(LOI.isEmpty) Right(None)
    	else {
    	  Right(Some(LOI.map(index => choices(index)._2).toList))
    	}
    } catch {
      	case i: IndexOutOfBoundsException => Left(ValidationError("IndexOutOfBoundsException"))
      	case n: NumberFormatException => Left(ValidationError("NumberFomatException"))
    	case e: Throwable => Left(ValidationError("Invalid Input: "+e.toString))
    }
  }
}