package models.mastery

import javax.jdo.annotations._
import scala.collection.JavaConverters._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.mastery._
import config.users.UsesDataStore


object M {
  def apply(s: String): String = "\\(\\displaystyle{" + s + "}\\)"
}

@PersistenceCapable(detachable = "true")
class Question extends Serializable {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _ //DB's id
  def id: Long = _id

  private[this] var _text: String = _ // text displayed for question (i.e. 2x^(2) = 3)
  def text = _text
  def text_=(theText: String) { _text = theText }
  
  @Persistent(defaultFetchGroup="true")
  @Element(types=Array(classOf[String]))
  @Join
  private[this] var _answer: java.util.List[String] = _ // the correct answers for a question
  def answer: List[String] = _answer.asScala.toList
  def answer_=(theAnswer: List[String]) { _answer = theAnswer.asJava }
  
  private[this] var _value: Int = _ // number of points the question is worth
  def value = _value
  def value_=(theValue: Int) { _value = theValue }
  
  private[this] var _isMath: Boolean = _
  def isMath = _isMath
  def isMath_=(iM : Boolean) { _isMath = iM }
    
  def this(text: String, answer: List[String], isMath: Boolean, value: Int) = {
    this()
    _text = text
    answer_=(answer)
    _value = value
    _isMath = isMath
  }
  
  def this(text: String, answer: List[String], isMath:Boolean) = this(text, answer, isMath, 1)
   def this(text: String, answer: List[String]) = this(text, answer, false)
  override def toString = { text }
}

object Question extends UsesDataStore {
  def getById(id: Long): Option[Question] = {
    dataStore.execute { pm =>
      pm.query[Question].filter(QQuestion.candidate.id.eq(id)).executeOption()
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
  
  private[this] lazy val _isMath: BooleanExpression = new BooleanExpressionImpl[Boolean](this, "_isMath")
  def isMath: BooleanExpression = _isMath
  
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