package models.assignments.questions

import models.assignments.DbQuestion
import scala.xml._
import javax.jdo.annotations._
import math.Value
import math.ExactNumber
import math.Integer

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class TrueFalse extends DbQuestion {
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var text: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var feedback: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var trueAnswer: TrueAnswer = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var falseAnswer: FalseAnswer = _
  
  def this(q: Node) = {
    this()
    assignFieldsFromXml(q)
  }
  
  private[this] def assignFieldsFromXml(q: Node) {
    this.text = (q \ "text").flatMap(_.child)
    this.feedback = (q \ "feedback").flatMap(_.child)
    for (ans <- (q \ "answer")) {
      val worth: ExactNumber = ExactNumber(ans \ "@worth" text).getOrElse(Integer(0))
      val ansFeedback = (ans \ "feedback").flatMap(_.child)
      if ((ans \ "text").text == "true") {
        this.trueAnswer = TrueAnswer(worth, feedback)
      } else {
        this.falseAnswer = FalseAnswer(worth, feedback)
      }
    }    
  }
  
  def toXml: Elem = {
    <question kind="true-false"><text>{ text }</text><feedback>{ feedback }</feedback>{ trueAnswer.toXml }{ falseAnswer.toXml }</question>
  }
  
  def toQuizHtml(label: NodeSeq, name: String, maybeId: Option[String] = None): Elem = {
    val id: String = maybeId.getOrElse(name)
    <div class="true-false">
      <label for={ id }>{ label }</label>
      <select name={ name } id={ id }>
        <option value="none"></option>
        <option value="true">True</option>
        <option value="false">False</option>
      </select>
      <span class="text">{ text }</span>
    </div>
  }
  
  def populateFields() {
    val q: Elem = this.content
    assignFieldsFromXml(q)
  }
}

object TrueFalse {
  def apply(q: Node): Option[TrueFalse] = {
    try {
      Some(new TrueFalse(q))
    } catch {
      case e: Exception => None
    }
  }
}