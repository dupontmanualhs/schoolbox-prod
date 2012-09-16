package models.mastery

import javax.jdo.annotations._
import scala.collection.JavaConverters._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.mastery._
import util.ScalaPersistenceManager
import util.DataStore

@PersistenceCapable(detachable = "true")
class Question extends Serializable {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _ //DB's id
  private[this] var _text: String = _ // text displayed for question (i.e. 2x^(2) = 3)
  private[this] var _answer: String = _ // the correct answer for a question
  private[this] var _value: Int = _ // number of points the question is worth
  private[this] var _kind: String = _ // type of answer needed
  
  def this(text: String, answer: String, value: Int, kind: String) = {
    this()
    _text = text
    _answer = answer
    _value = value
    _kind = kind
  }
  
  def id = _id
  
  def text = _text
  def text_=(theText: String) { _text = theText }
  
  def answer = _answer
  def answer_=(theAnswer: String) { _answer = theAnswer }
  
  def value = _value
  def value_=(theValue: Int) { _value = theValue }
  
  def kind = _kind
  def kind_=(theKind: String) { _kind = theKind }
  
  override def toString = { text }
}

object Question {
  def getById(id: Long)(implicit pm: ScalaPersistenceManager = null): Option[Question] = {
    DataStore.execute { epm =>
      epm.query[Question].filter(QQuestion.candidate.id.eq(id)).executeOption()
    }
  }
  
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