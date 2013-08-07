package models.assignments.questions

import models.assignments.DbQuestion
import scala.xml._
import javax.jdo.annotations._
import math.Value
import math.ExactNumber
import math.Integer
import org.dupontmanual.forms.fields.Field
import org.dupontmanual.forms.fields.ChoiceFieldOptional

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
      val worth: ExactNumber = ExactNumber((ans \ "@worth").text).getOrElse(Integer(0))
      val ansFeedback = (ans \ "feedback").flatMap(_.child)
      if ((ans \ "text").text == "true") {
        this.trueAnswer = TrueAnswer(worth, ansFeedback)
      } else {
        this.falseAnswer = FalseAnswer(worth, ansFeedback)
      }
    }    
  }
  
  def toXml: Elem = {
    <question kind="true-false"><text>{ text }</text><feedback>{ feedback }</feedback>{ trueAnswer.toXml }{ falseAnswer.toXml }</question>
  }
    
  def populateFields() {
    val q: Elem = this.content
    assignFieldsFromXml(q)
  }

  def toFormField(name: String) = new ChoiceFieldOptional[Boolean](name, List(("True", true), ("False", false)))

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

class TrueFalseAnswer 