package models.mastery

import javax.jdo.annotations._
import models.mastery._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import play.api.mvc.{RequestHeader, Session}
import scala.collection.JavaConverters._

import scalajdo.DataStore

@PersistenceCapable(detachable = "true")
class Quiz {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _ //DB's id
  def id: Long = _id

  private[this] var _name: String = _ //name of quiz (i.e. Foiling and Factoring Mastery)
  def name: String = _name
  def name_=(theName: String) { _name = theName }

  @Persistent
  @Element(types=Array(classOf[QuizSection]))
  @Join
  private[this] var _sections: java.util.List[QuizSection] = _ //list of sections that make up a quiz
  def sections: List[QuizSection] = _sections.asScala.toList
  def sections_=(theSections: List[QuizSection]) { _sections = theSections.asJava}
      
  def this(theName: String, theSections: List[QuizSection]) = {
    this()
    name_=(theName)
    sections_=(theSections)
  }
  
  override def toString = { name }
}
object Quiz {
  def getById(id: Long): Option[models.mastery.Quiz] = {
    val cand=QQuiz.candidate()
    DataStore.pm.query[Quiz].filter(cand.id.eq(id)).executeOption()
  }
}

trait QQuiz extends PersistableExpression[Quiz]{
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _sections: ListExpression[java.util.List[QuizSection], QuizSection] = new ListExpressionImpl[java.util.List[QuizSection], QuizSection](this, "_sections")
  def sections: ListExpression[java.util.List[QuizSection], QuizSection] = _sections
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