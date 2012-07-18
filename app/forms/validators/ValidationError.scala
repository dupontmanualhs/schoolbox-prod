package forms.validators

import scala.xml.NodeSeq

// TODO: change to list of NodeSeq so can include tags
class ValidationError(val messages: List[String]) extends Seq[String] {
  def apply(idx: Int) = messages(idx)
  def iterator = messages.iterator
  def length = messages.length
  
  def asHtml: NodeSeq = <ul class="errorlist">{ messages.flatMap(msg => <li>{ msg }</li>) }</ul> 
}

object ValidationError {
  def apply(message: String): ValidationError = ValidationError(List(message))
  
  def apply(messages: List[String]): ValidationError = new ValidationError(messages)
}