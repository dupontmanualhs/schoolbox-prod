package models.mastery

import javax.jdo.annotations._
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
  
  def this(questionText: String, answer: String, value: Int) = {
    this()
    _questionText=questionText
    _correctAnswer=answer
    _value=value
  }
  
  override def toString = {"question text\n" + _questionText + "\ncorrect answer:\n" + _correctAnswer + "\nvalue:\n" + _value}
}

trait QQuestion extends PersistableExpression[Question] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _questionText: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_questionText")
  def questionText: ObjectExpression[String] = _questionText
  
  private[this] lazy val _correctAnswer: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_correctAnswer")
  def correctAnswer: ObjectExpression[String] = _correctAnswer
  
  private[this] lazy val _value: ObjectExpression[Int] = new ObjectExpressionImpl[Int](this, "_value")
  def value: ObjectExpression[Int] = _value
  
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