package models.mastery

import javax.jdo.annotations._
import models.mastery._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable = "true")
class QuestionSet { //a QuestionSet is a list of all the questions that can be used for a certain number on a quiz (so #1 on a quiz could be addition problems, and #2 could be subtraction problems, so QuestionSets would keep them seperate)
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _listOfQuestions: List[Question] = _

  def this(listQuestions: List[Question]) = {
    this()
    _listOfQuestions=listQuestions
  }
  
  override def toString = {
    ""+_listOfQuestions
  }

}

trait QQuestionSet extends PersistableExpression[QuestionSet]{
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _listOfQuestions: ObjectExpression[List[Question]] = new ObjectExpressionImpl[List[Question]](this, "_listOfQuestions")
  def listOfQuestions: ObjectExpression[List[Question]] = _listOfQuestions
}

object QQuestionSet {
  def apply(parent: PersistableExpression[QuestionSet], listOfQuestions: List[Question]): QQuestionSet = {
    new PersistableExpressionImpl[QQuestionSet](parent, name) with QQuestionSet
  }

  def apply(cls: Class[Quiz], name: String, exprType: ExpressionType): QQuestionSet = {
    new PersistableExpressionImpl[QuestionSet](cls, name, exprType) with QQuestionSet
  }

  private[this] lazy val jdoCandidate: QQuestionSet = candidate("this")

  def candidate(name: String): QQuestionSet = QQuestionSet(null, name, 5)

  def candidate(): QQuestionSet = jdoCandidate

  def parameter(name: String): QQuestionSet = QQuestionSet(classOf[QuestionSet], name, ExpressionType.PARAMETER)

  def variable(name: String): QQuestionSet = QQuestionSet(classOf[QuestionSet], name, ExpressionType.VARIABLE)
}