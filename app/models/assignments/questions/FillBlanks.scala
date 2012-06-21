package models.assignments.questions

import xml.NodeSeq
import play.api.data._
import javax.jdo.annotations._
import models.assignments.DbQuestion
import scala.xml.Elem
import scala.xml.Node

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class FillBlanks extends DbQuestion {
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var text: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var feedback: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var answers: List[List[Answer]] = _
  
  def this(q: Node) = {
    this()
    assignFieldsFromXml(q)
  }
  
  private[this] def assignFieldsFromXml(q: Node) {
    this.text = (q \ "text").flatMap(_.child)
    this.feedback = (q \ "feedback").flatMap(_.child)
    this.answers = (q \ "answer").map(ans => Answer.fromXml(ans))
    
  }
  
  def toXml: Elem = {
    <question kind="blank">
      <text>{ text }</text>
      <feedback>{ feedback }</feedback>
      { answers.flatMap(_.asXml) }
    </question>
  }
  
  def toQuizHtml(label: NodeSeq, name: String, maybeId: Option[String] = None): Elem = {
    val id: String = maybeId.getOrElse(name)
    <div class="blank">
      <label for={ id }>{ label }</label>
      <span class="text">{ text }</span> <br/>
      { answers.zipWithIndex.flatMap(ansWithIndex => {
        val ans = ansWithIndex._1
        val index = ansWithIndex._2
        <input type="radio" name={ name } value={ index.toString }/> ++ { ans.text } ++ <br/>
      })}      
    </div>
  }

  
  def populateFields() {
    val q: Elem = this.content
    assignFieldsFromXml(q)
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