package models.quizzes

import java.sql.Timestamp
import javax.jdo.annotations._

import scala.collection.JavaConverters._

import org.joda.time.DateTime

import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

import models.users.Student

@PersistenceCapable(detachable="true")
class Quiz {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  @Persistent(defaultFetchGroup="true")
  private[this] var _student: Student = _
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _date: Timestamp = _
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _questions: java.util.List[Question] = _
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _answers: java.util.List[String] = _
  
  def this(student: Student, date: DateTime, questions: List[Question], answers: List[String]) = {
    this()
    student_=(student)
    date_=(date)
    questions_=(questions)
    answers_=(answers)
  }
  
  def student: Student = _student
  def student_=(theStudent: Student) { _student = student }
  
  def date: DateTime = new DateTime(_date.getTime)
  def date_=(theDate: DateTime) { _date = new Timestamp(theDate.getMillis) }
  
  def questions: List[Question] = _questions.asScala.toList
  def questions_=(theQuestions: List[Question]) { _questions = theQuestions.asJava }
  
  def answers: List[String] = _answers.asScala.toList
  def answers_=(theAnswers: List[String]) { _answers = theAnswers.asJava }
}

trait QQuiz extends PersistableExpression[Quiz] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student
  
  private[this] lazy val _date: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Timestamp](this, "_date")
  def date: DateExpression[java.util.Date] = _date

  private[this] lazy val _questions: CollectionExpression[java.util.List[Question], Question] = 
      new CollectionExpressionImpl[java.util.List[Question], Question](this, "_questions")
  def questions: CollectionExpression[java.util.List[Question], Question] = _questions

  private[this] lazy val _answers: CollectionExpression[java.util.List[String], String] = 
      new CollectionExpressionImpl[java.util.List[String], String](this, "_answers")
  def answers: CollectionExpression[java.util.List[String], String] = _answers
}

object QQuiz {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QQuiz = {
    new PersistableExpressionImpl[Quiz](parent, name) with QQuiz
  }
  
  def apply(cls: Class[Quiz], name: String, exprType: ExpressionType): QQuiz = {
    new PersistableExpressionImpl[Quiz](cls, name, exprType) with QQuiz
  }
  
  private[this] lazy val jdoCandidate: QQuiz = candidate("this")
  
  def candidate(name: String): QQuiz = QQuiz(null, name, 5)
  
  def candidate(): QQuiz = jdoCandidate
  
  def parameter(name: String): QQuiz = QQuiz(classOf[Quiz], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QQuiz = QQuiz(classOf[Quiz], name, ExpressionType.VARIABLE)
}
