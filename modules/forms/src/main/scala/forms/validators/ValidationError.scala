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
  
  def render: NodeSeq = messages match {
    case Nil => NodeSeq.Empty
    case _ => messages.flatMap(msg => <div class="alert alert-error">{ msg }</div>)
  }
}

object ValidationError {
  def apply(message: String): ValidationError = ValidationError(List(Text(message)))
    
  def apply(messages: List[NodeSeq]): ValidationError = new ValidationError(messages)
}