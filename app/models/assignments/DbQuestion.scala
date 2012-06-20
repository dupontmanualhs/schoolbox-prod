package models.assignments

import javax.jdo.annotations._
import scala.xml.{NodeSeq, Elem}
import util.Helpers.{string2elem, string2nodeSeq}
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import javax.jdo.listener.{LoadCallback, StoreCallback}
import util.Format
import math.MathExactNumber
import scala.xml.Node

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
abstract class DbQuestion extends LoadCallback with StoreCallback {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _format: Int = _
  @Column(jdbcType="CLOB")
  private[this] var _content: String = _
  @Column(jdbcType="CLOB")
  private[this] var _html: String = _
  
  def id: Long = _id
  
  def format: Format = Format.fromInt(_format)
  def format_=(theFormat: Format) { _format = theFormat.toInt }
    
  protected def content: Elem = string2elem(_content)
  protected def content_=(xml: Elem) { 
    // TODO: make sure this is a <question>...</question> element
    _content = xml.toString
  }
  
  // TODO: if someone asks for the html and the content is dirty, the html should get updated
  protected def html: Elem = string2elem(_html)
  protected def html_=(html: Elem) {
    // TODO: make sure this HTML is sanitized before storing in database
    _html = html.toString
  }
  
  def toQuizHtml(label: NodeSeq, name: String, maybeId: Option[String] = None): Elem
  def toXml: Elem
  def populateFields(): Unit
  
  def jdoPreStore() {
    this.content = toXml
  }
  
  def jdoPostLoad() {
    populateFields() 
  }
}

object DbQuestion {
  def fromXml(q: Node): Option[DbQuestion] = {
    if (q.label != "question") None
    else {
      import questions._
      (q \ "@kind").text match {
        case "true-false" => TrueFalse(q)
        case "mult-choice" => MultChoice(q)
        case _ => None
      }
    }
  }
}

trait QDbQuestion extends PersistableExpression[DbQuestion] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _format: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_format")
  def format: NumericExpression[Int] = _format
  
  private[this] lazy val _content: StringExpression = new StringExpressionImpl(this, "_content")
  def content: StringExpression = _content

  private[this] lazy val _html: StringExpression = new StringExpressionImpl(this, "_html")
  def html: StringExpression = _html
}

object QDbQuestion {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QDbQuestion = {
    new PersistableExpressionImpl[DbQuestion](parent, name) with QDbQuestion
  }
  
  def apply(cls: Class[DbQuestion], name: String, exprType: ExpressionType): QDbQuestion = {
    new PersistableExpressionImpl[DbQuestion](cls, name, exprType) with QDbQuestion
  }
  
  private[this] lazy val jdoCandidate: QDbQuestion = candidate("this")
  
  def candidate(name: String): QDbQuestion = QDbQuestion(null, name, 5)
  
  def candidate(): QDbQuestion = jdoCandidate
  
  def parameter(name: String): QDbQuestion = QDbQuestion(classOf[DbQuestion], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QDbQuestion = QDbQuestion(classOf[DbQuestion], name, ExpressionType.VARIABLE)
}