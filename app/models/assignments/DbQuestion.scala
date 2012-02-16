package models.assignments

import javax.jdo.annotations._
import scala.xml.{NodeSeq, Elem}
import util.Helpers.{string2elem, string2nodeSeq}
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import javax.jdo.listener.{LoadCallback, StoreCallback}

@PersistenceCapable(detachable="true")
abstract class DbQuestion extends LoadCallback with StoreCallback {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Column(allowsNull="false")
  private[this] var _subject : Subject = _
  @Column(allowsNull="false")
  private[this] var _source: Source = _
  private[this] var _number: String = _
  private[this] var _content: String = _
  
  def id: Long = _id
  
  def subject: Subject = _subject
  def subject_=(theSubject: Subject) { _subject = theSubject }
  
  def source: Source = _source
  def source_=(theSource: Source) { _source = theSource }
  
  def number: String = _number
  def number_=(theNumber: String) { _number = theNumber }
  
  protected def content: Elem = string2elem(_content)
  protected def content_=(xml: Elem) { 
    // make sure this is a <question>...</question> element
    _content = xml.toString
  }
}

trait QDbQuestion extends PersistableExpression[DbQuestion] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _subject: ObjectExpression[Subject] = new ObjectExpressionImpl[Subject](this, "_subject")
  def subject: ObjectExpression[Subject] = _subject
  
  private[this] lazy val _source: ObjectExpression[Source] = new ObjectExpressionImpl[Source](this, "_source")
  def source: ObjectExpression[Source] = _source
  
  private[this] lazy val _number: StringExpression = new StringExpressionImpl(this, "_number")
  def number: StringExpression = _number
  
  private[this] lazy val _content: StringExpression = new StringExpressionImpl(this, "_content")
  def content: StringExpression = _content
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