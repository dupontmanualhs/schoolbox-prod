package forms.validators

class ValidationError(val messages: List[String]) extends Seq[String] {
  def apply(idx: Int) = messages(idx)
  def iterator = messages.iterator
  def length = messages.length
}

object ValidationError {
  def apply(message: String): ValidationError = ValidationError(List(message))
  
  def apply(messages: List[String]): ValidationError = new ValidationError(messages)
}