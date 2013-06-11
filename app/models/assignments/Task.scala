package models.assignments

import models.users.Teacher
import javax.jdo.annotations._
import scala.collection.JavaConverters._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

import scalajdo.DataStore

@PersistenceCapable(detachable = "true")
class Task {
  //TODO make work for different users
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  @Element(types = Array(classOf[DbQuestion]))
  @Join
  private[this] var _questions: java.util.List[DbQuestion] = _

  def this(questions: List[DbQuestion]) = {
    this()
    this.questions = questions
  }

  def id: Long = _id

  def questions: List[DbQuestion] = _questions.asScala.toList
  def questions_=(theQuestions: List[DbQuestion]) { _questions = theQuestions.asJava }
}

object Task {
  def getById(id: Long): Option[Task] = {
    DataStore.pm.query[Task].filter(QTask.candidate.id.eq(id)).executeOption()
  }
}

trait QTask extends PersistableExpression[Task] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _questions: CollectionExpression[java.util.List[DbQuestion], DbQuestion] =
    new CollectionExpressionImpl[java.util.List[DbQuestion], DbQuestion](this, "_questions")
  def questions: CollectionExpression[java.util.List[DbQuestion], DbQuestion] = _questions
}

object QTask {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QTask = {
    new PersistableExpressionImpl[Task](parent, name) with QTask
  }

  def apply(cls: Class[Task], name: String, exprType: ExpressionType): QTask = {
    new PersistableExpressionImpl[Task](cls, name, exprType) with QTask
  }

  private[this] lazy val jdoCandidate: QTask = candidate("this")

  def candidate(name: String): QTask = QTask(null, name, 5)

  def candidate(): QTask = jdoCandidate

  def parameter(name: String): QTask = QTask(classOf[Task], name, ExpressionType.PARAMETER)

  def variable(name: String): QTask = QTask(classOf[Task], name, ExpressionType.VARIABLE)
}

