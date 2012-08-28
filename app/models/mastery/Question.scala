package models.mastery

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.mastery._

@PersistenceCapable(detachable = "true")
class Question {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _typ: String = _
  private[this] var _questionText: String = _
  private[this] var _correctAnswer: String = _
  private[this] var _value: Int = _
  private[this] var _section: Section = _
  
  def this(id: Long, tpe: String, questionText: String, answer: String, value: Int, section: Section) = {
    this()
    _id=id
    _typ=tpe
    _questionText=questionText
    _correctAnswer=answer
    _value=value
    _section=section
  }
  
  override def toString = {_questionText}
}

trait QQuestion extends PersistableExpression[Question] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _typ: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_typ")
  def typ: ObjectExpression[String] = _typ
  
  private[this] lazy val _questionText: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_questionText")
  def questionText: ObjectExpression[String] = _questionText
  
  private[this] lazy val _correctAnswer: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_correctAnswer")
  def correctAnswer: ObjectExpression[String] = _correctAnswer
  
  private[this] lazy val _value: ObjectExpression[Int] = new ObjectExpressionImpl[Int](this, "_value")
  def value: ObjectExpression[Int] = _value
  
  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section
}

object QQuestion {
  def apply(parent: PersistableExpression[Question], typ: String, questionText: String, correctAnswer: String, value: Int, section: Section): QQuestion = {
    new PersistableExpressionImpl[QQuestion](parent, name) with QQuestion
  }

  def apply(cls: Class[Question], name: String, exprType: ExpressionType): QQuestion = {
    new PersistableExpressionImpl[Question](cls, name, exprType) with QQuestion
  }

  private[this] lazy val jdoCandidate: QQuestion = candidate("this")

  def candidate(name: String): QQuestion = QQuestion(null, name, 5)

  def candidate(): QQuestion = jdoCandidate

  def parameter(name: String): QQuestion = QQuestion(classOf[Question], name, ExpressionType.PARAMETER)

  def variable(name: String): QQuestion = QQuestion(classOf[Question], name, ExpressionType.VARIABLE)
}