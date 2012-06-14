package models.assignments.questions

import models.assignments.DbQuestion
import scala.xml.Elem
import scala.xml.NodeSeq
import javax.jdo.annotations.Persistent
import javax.jdo.annotations.PersistenceModifier

class TrueFalse extends DbQuestion {
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var text: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var answer: Option[Boolean] = _
  
  def asXml: Elem = {
    val answerXml: String = answer match {
      case None => "either"
      case Some(x) => if (x) "true" else "false"
    }
    <question kind="true-false"><text answer={ answerXml }>{ text }</text></question>
  }
  
  def populateFields() {
    val q: Elem = this.content
    this.text = (q \ "text").flatMap(_.child)
    this.answer = (q \ "text" \ "@answer").toString match {
      case "true" => Some(true)
      case "false" => Some(false)
      case _ => None
    }
  }
}