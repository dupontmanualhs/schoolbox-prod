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
  @Element(types=Array(classOf[String]))
  @Join
  @Persistent(defaultFetchGroup="true")
  private[this] var _answer: java.util.List[String] = _ // the correct answers for a question
  private[this] var _value: Int = _ // number of points the question is worth
  
  def this(text: String, answer: List[String], value: Int) = {
    this()
    _text = text
    answer_=(answer)
    _value = value
  }
  
  def id = _id
  
  def text = _text
  def text_=(theText: String) { _text = theText }
  
  def answer: List[String] = _answer.asScala.toList
  def answer_=(theAnswer: List[String]) { _answer = theAnswer.asJava }
  
  def value = _value
  def value_=(theValue: Int) { _value = theValue }
  
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
  
  private[this] lazy val _answer: ObjectExpression[List[String]] = new ObjectExpressionImpl[List[String]](this, "_answer")
  def answer: ObjectExpression[List[String]] = _answer
  
  private[this] lazy val _value: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_value")
  def value: NumericExpression[Int] = _value
  
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