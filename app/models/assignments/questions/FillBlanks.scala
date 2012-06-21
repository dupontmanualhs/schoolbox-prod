package models.assignments.questions

import xml._
import play.api.data._
import javax.jdo.annotations._
import models.assignments.DbQuestion
import scala.xml.transform.BasicTransformer

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class FillBlanks extends DbQuestion {
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var text: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var feedback: NodeSeq = _
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  var answerList: List[List[Answer]] = _
  
  def this(q: Node) = {
    this()
    assignFieldsFromXml(q)
  }
  
  private[this] def assignFieldsFromXml(q: Node) {
    this.text = (q \ "text").flatMap(_.child)
    this.feedback = (q \ "feedback").flatMap(_.child)
    this.answerList = (0 to (q \\ "blank").length).map(i => {
      (q \ "answer").filter(q => (q \ "@blank").toString == i.toString).toList.map(ans => Answer.fromXml(ans))
    }).toList    
  }
  
  def toXml: Elem = {
    <question kind="fill-blanks" format="html">
      <text>{ text }</text>
      <feedback>{ feedback }</feedback>
      { answerList.zipWithIndex.flatMap(answersWithBlankNum => {
        val answers = answersWithBlankNum._1
        val blankNum = answersWithBlankNum._2
        (answers.flatMap(ans => {
          val elem = ans.toXml
          elem.copy(attributes=new UnprefixedAttribute("blank", blankNum.toString, elem.attributes))
        })) })
      }
    </question>
  }
  
  def toQuizHtml(label: NodeSeq, name: String, maybeId: Option[String] = None): Elem = {
    val id: String = maybeId.getOrElse(name)
    <div class="fill-blanks">
      <label for={ id }>{ label }</label>
      <span class="text">{ new BlankReplacer(name, id).transform(text) }</span>
    </div>
  }

  
  def populateFields() {
    val q: Elem = this.content
    assignFieldsFromXml(q)
  }  
}

object FillBlanks {
  def apply(q: Node): Option[FillBlanks] = {
    try {
      Some(new FillBlanks(q))
    } catch {
      case e: Exception => None
    }
  }
}

class BlankReplacer(name: String, id: String) extends BasicTransformer {
  var i = 0

  override def transform(n: Node): NodeSeq = n match {
    case <blank/> => {
      i += 1
      <input type="text" name={ "%s[%d]".format(name, i) } id={ "%s[%d]".format(id, i) }/>
    }
    case elem: Elem => elem.copy(child=elem.child.flatMap(transform _))
    case _ => n
  }
}