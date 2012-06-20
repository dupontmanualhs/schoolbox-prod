package models.assignments.questions

import xml.NodeSeq
import play.api.data._
import javax.jdo.annotations._
import models.assignments.DbQuestion
import scala.xml.Elem
import scala.xml.Node

@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
class MultChoice extends DbQuestion {
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var singleAnswer: Boolean = false
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var scramble: Boolean = true
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var text: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var feedback: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var answers: Seq[Answer] = _
  
  def this(q: Node) = {
    this()
    assignFieldsFromXml(q)
  }
  
  private[this] def assignFieldsFromXml(q: Node) {
    this.singleAnswer = ((q \ "@singleAnswer").isEmpty || (q \ "@singleAnswer").text == "true")
    this.scramble = ((q \ "@scramble").isEmpty || (q \ "@scramble").text == "true")
    this.text = (q \ "text").flatMap(_.child)
    this.feedback = (q \ "feedback").flatMap(_.child)
    this.answers = (q \ "answer").map(ans => Answer.fromXml(ans))
    
  }
  
  def toXml: Elem = {
    <question kind="mult-choice" singleAnswer={ this.singleAnswer.toString } scramble={ this.scramble.toString }>
      <text>{ text }</text>
      <feedback>{ feedback }</feedback>
      { answers.flatMap(_.asXml) }
    </question>
  }
  
  def toQuizHtml(label: NodeSeq, name: String, maybeId: Option[String] = None): Elem = {
    <span></span>
  }

  
  def populateFields() {
    val q: Elem = this.content
  }  
}

object MultChoice {
  def apply(q: Node): Option[MultChoice] = {
    try {
      Some(new MultChoice(q))
    } catch {
      case e: Exception => None
    }
  }
}