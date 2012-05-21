package models.quizzes

import javax.jdo.annotations._

import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class Question {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Column(allowsNull="false")
  private[this] var _text: String = _
  
  @Column(allowsNull="false")
  private[this] var _answer: String = _
  
  @Column(allowsNull="false")
  private[this] var _kind: Kind = _
  
  def this(text: String, answer: String, kind: Kind) = {
    this()
    text_=(text)
    answer_=(answer)
    kind_=(kind)
  }
  
  def id: Long = _id
  
  def text: String = _text
  def text_=(theText: String) { _text = theText }
  
  def answer: String = _answer
  def answer_=(theAnswer: String) { _answer = theAnswer }
  
  def kind: Kind = _kind
  def kind_=(theKind: Kind) { _kind = theKind }
}

trait QQuestion extends PersistableExpression[Question] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _text: StringExpression = new StringExpressionImpl(this, "_text")
  def text: StringExpression = _text
  
  private[this] lazy val _answer: StringExpression = new StringExpressionImpl(this, "_answer")
  def answer: StringExpression = _answer
  
  private[this] lazy val _kind: ObjectExpression[Kind] = new ObjectExpressionImpl[Kind](this, "_kind")
  def kind: ObjectExpression[Kind] = _kind
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

