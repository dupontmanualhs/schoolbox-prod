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
  var hasMultipleAnswers: Boolean = false
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var canScrambleAnswers: Boolean = true
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var text: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var answers: Seq[Answer] = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var explanation: NodeSeq = _
  
  def toXml: Elem = {
    <question type="MultChoice" hasMultipleAnswers={ this.hasMultipleAnswers.toString } canScrambleAnswers={ this.canScrambleAnswers.toString }>
      <text>{ text }</text>
      <answers>{ answers.flatMap(_.asXml) }</answers>
      <explanation>{ explanation }</explanation>
    </question>
  }
  
  def toQuizHtml(label: NodeSeq, name: String, maybeId: Option[String] = None): Elem = {
    <span></span>
  }

  
  def populateFields() {
    val q: Elem = this.content
    this.hasMultipleAnswers = ((q \ "@hasMultipleAnswers").text == "true")
    this.canScrambleAnswers = ((q \ "@canScrambleAnswers").text == "true")
    this.text = (q \ "text").flatMap(_.child)
    this.explanation = (q \ "explanation").flatMap(_.child)
    this.answers = (q \ "answer").map(ans => Answer.fromXml(ans))
  }  
}

object MultChoice {
  def apply(q: Node): Option[MultChoice] = None
}