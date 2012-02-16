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
  private[this] var _hasMultipleAnswers: Boolean = false
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  private[this] var _canScrambleAnswers: Boolean = true
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  private[this] var _text: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  private[this] var _answers: Seq[MultChoiceAnswer] = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  private[this] var _explanation: NodeSeq = _
  
  def hasMultipleAnswers = _hasMultipleAnswers
  def hasMultipleAnswers_=(multAns: Boolean) { this._hasMultipleAnswers = multAns}
  
  def canScrambleAnswers = _canScrambleAnswers
  def canScrambleAnswers_=(canScramble: Boolean) { this._canScrambleAnswers = canScramble }
  
  def text = _text
  def text_=(theText: NodeSeq) { this._text = theText }
  
  def answers = _answers
  def answers_=(theAnswers: Seq[MultChoiceAnswer]) { this._answers = theAnswers }
  
  def explanation = _explanation
  def explanation_=(theExpl: NodeSeq) { this._explanation = theExpl }
  
  def asXml: Elem = {
    <question type="MultChoice" hasMultipleAnswers={ this.hasMultipleAnswers.toString } canScrambleAnswers={ this.canScrambleAnswers.toString }>
      <text>{ text }</text>
      <answers>{ answers.flatMap(_.asXml) }</answers>
      <explanation>{ explanation }</explanation>
    </question>
  }
  
  def populateFields() {
    val q: Elem = this.content
    this.hasMultipleAnswers = ((q \ "@hasMultipleAnswers").text == "true")
    this.canScrambleAnswers = ((q \ "@canScrambleAnswers").text == "true")
    this.text = (q \ "text").flatMap(_.child)
    this.explanation = (q \ "explanation").flatMap(_.child)
    def xmlToAns(xml: Node): MultChoiceAnswer = {
      val isCorrect = ((xml \ "@isCorrect").text == "true")
      val text = NodeSeq.fromSeq(xml.child)
      MultChoiceAnswer(text, isCorrect)
    }
    this.answers = (q \\ "answer").map(xmlToAns(_))
  }
  
  def jdoPreStore() {
    this.content = asXml
  }
  
  def jdoPostLoad() {
    populateFields() 
  }
}

case class MultChoiceAnswer(val text: NodeSeq, val isCorrect: Boolean) {
  def asXml: NodeSeq = {
    <answer correct={ isCorrect.toString }>{ text }</answer>
  }
}
