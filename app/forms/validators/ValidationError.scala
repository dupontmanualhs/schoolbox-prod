package forms.validators

import scala.xml.{NodeSeq, Text}

/**
 * A ValidationError is a list of NodeSeq that explains what is wrong
 * with a Field or Form. An empty ValidationError means nothing is wrong.
 */
class ValidationError(val messages: List[NodeSeq]) extends Seq[NodeSeq] {
  def apply(idx: Int) = messages(idx)
  def iterator = messages.iterator
  def length = messages.length
  
  def asHtml: NodeSeq = <ul class="errorlist">{ messages.flatMap(msg => <li>{ msg }</li>) }</ul> 
}

object ValidationError {
  def apply(message: String): ValidationError = ValidationError(List(Text(message)))
    
  def apply(messages: List[NodeSeq]): ValidationError = new ValidationError(messages)
}