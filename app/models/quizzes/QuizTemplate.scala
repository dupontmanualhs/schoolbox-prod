package models.quizzes

import javax.jdo.annotations._

import scala.collection.JavaConverters._

import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class QuizTemplate {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _
  
  @Element(types=Array(classOf[Kind]))
  @Join
  private[this] var _questions: java.util.List[Kind] = _
  
  def this(name: String, questions: List[Kind]) = {
    this()
    name_=(name)
    questions_=(questions)
  }
  
  def id: Long = _id
  
  def name: String = _name
  def name_=(theName: String) { _name = theName }
  
  def questions: List[Kind] = _questions.asScala.toList
  def questions_=(theQuestions: List[Kind]) { _questions = theQuestions.asJava }
}

trait QQuizTemplate extends PersistableExpression[QuizTemplate] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _questions: CollectionExpression[java.util.List[Kind], Kind] = 
      new CollectionExpressionImpl[java.util.List[Kind], Kind](this, "_questions")
  def questions: CollectionExpression[java.util.List[Kind], Kind] = _questions
}

object QQuizTemplate {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QQuizTemplate = {
    new PersistableExpressionImpl[QuizTemplate](parent, name) with QQuizTemplate
  }
  
  def apply(cls: Class[QuizTemplate], name: String, exprType: ExpressionType): QQuizTemplate = {
    new PersistableExpressionImpl[QuizTemplate](cls, name, exprType) with QQuizTemplate
  }
  
  private[this] lazy val jdoCandidate: QQuizTemplate = candidate("this")
  
  def candidate(name: String): QQuizTemplate = QQuizTemplate(null, name, 5)
  
  def candidate(): QQuizTemplate = jdoCandidate
  
  def parameter(name: String): QQuizTemplate = QQuizTemplate(classOf[QuizTemplate], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QQuizTemplate = QQuizTemplate(classOf[QuizTemplate], name, ExpressionType.VARIABLE)
}
