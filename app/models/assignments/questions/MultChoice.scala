package models.assignments.questions

import xml.NodeSeq
import play.api.data._
import javax.jdo.annotations._
import models.assignments.DbQuestion
import scala.xml.Elem

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
  private[this] var _answers: List[MultChoiceAnswer] = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  private[this] var _explanation: NodeSeq = _
  
  def hasMultipleAnswers = _hasMultipleAnswers
  def hasMultipleAnswers_=(multAns: Boolean) { this._hasMultipleAnswers = multAns}
  
  def canScrambleAnswers = _canScrambleAnswers
  def canScrambleAnswers_=(canScramble: Boolean) { this._canScrambleAnswers = canScramble }
  
  def text = _text
  def text_=(theText: NodeSeq) { this._text = theText }
  
  def answers = _answers
  def answers_=(theAnswers: List[MultChoiceAnswer]) { this._answers = theAnswers }
  
  def explanation = _explanation
  def explanation_=(theExpl: NodeSeq) { this._explanation = theExpl }
  
  def asXml: NodeSeq = {
    <question type="MultChoice" hasMultipleAnswers={ this.hasMultipleAnswers.toString } canScrambleAnswers={ this.canScrambleAnswers.toString }>
      <text>{ text }</text>
      <answers>{ answers.flatMap(_.asXml) }</answers>
      <explanation>{ explanation }</explanation>
    </question>
  }
  
  def populateFields() {
    val xml: NodeSeq = this.content \ "question"
  }
  
  def jdoPreStore() {
    this.content = asXml
  }
  
  def jdoPostLoad() {
    
  }
}

case class MultChoiceAnswer(val text: NodeSeq, val isCorrect: Boolean) {
  def asXml: NodeSeq = {
    <answer correct={ isCorrect.toString }>{ text }</answer>
  }
}
