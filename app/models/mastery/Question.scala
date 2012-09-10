package models.mastery

import javax.jdo.annotations._
import scala.collection.JavaConverters._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.mastery._

@PersistenceCapable(detachable = "true")
class Question {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _ //DB's id
  private[this] var _questionText: String = _ //text displayed for question (i.e. 2x^(2) = 3)
  private[this] var _correctAnswer: String = _ //the correct answer for a question
  private[this] var _value: Int = _ //amount of points the question is worth
  private[this] var _typ: String = _ //type of answer needed
  
  def this(questionText: String, answer: String, value: Int, typ: String) = {
    this()
    _questionText=questionText
    _correctAnswer=answer
    _value=value
    _typ=typ
  }
  
  def getAnswer = _correctAnswer
  def Type = {_typ}
  override def toString = { _questionText }
}

trait QQuestion extends PersistableExpression[Question] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _questionText: StringExpression = new StringExpressionImpl(this, "_questionText")
  def questionText: StringExpression = _questionText
  
  private[this] lazy val _correctAnswer: StringExpression = new StringExpressionImpl(this, "_correctAnswer")
  def correctAnswer: StringExpression = _correctAnswer
  
  private[this] lazy val _value: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_value")
  def value: NumericExpression[Int] = _value
  
  private[this] lazy val _typ: StringExpression = new StringExpressionImpl(this, "_typ")
  def Type: StringExpression = _typ
}

object QQuestion {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QQuestion = {
    new PersistableExpressionImpl[Question](parent, name) with QQuestion
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