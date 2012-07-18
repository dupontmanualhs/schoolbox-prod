package forms.validators

import Numeric._

abstract class Validator[T] extends Function[T, ValidationError] {
}

object Validator {
  def apply[T](f: Function[T, ValidationError]): Validator[T] = {
    new Validator[T] {
      def apply(t: T) = f(t)
    }
  }
}

class MinLengthValidator(minLength: Int, msg: (String => String)) extends Validator[String] {
  def apply(str: String): ValidationError = {
    if (str.length < minLength) ValidationError(msg(str)) else ValidationError(Nil)
  }
} 

class MaxLengthValidator(maxLength: Int, msg: (String => String)) extends Validator[String] {
  def apply(str: String): ValidationError = {
    if (str.length > maxLength) ValidationError(msg(str)) else ValidationError(Nil)
  }
}

class MinValueValidator[T](minValue: T, msg: (T => String))(implicit n: Numeric[T]) extends Validator[T] {
  def apply(value: T): ValidationError = {
    if (n.lt(value, minValue)) ValidationError(msg(value)) else ValidationError(Nil)
  }
}

class MaxValueValidator[T](maxValue: T, msg: (T => String))(implicit n: Numeric[T]) extends Validator[T] {
  def apply(value: T): ValidationError = {
    if (n.gt(value, maxValue)) ValidationError(msg(value)) else ValidationError(Nil)
  }
}