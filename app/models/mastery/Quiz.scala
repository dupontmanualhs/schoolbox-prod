package models.mastery

import javax.jdo.annotations._
import models.mastery._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import util.DataStore
import util.ScalaPersistenceManager
import play.api.mvc.{RequestHeader, Session}
import util.DbRequest
import scala.collection.JavaConverters._

@PersistenceCapable(detachable = "true")
class Quiz {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _ //DB's id
  private[this] var _name: String = _ //name of quiz (i.e. Foiling and Factoring Mastery)
  @Element(types=Array(classOf[QuizSection]))
  @Join
  private[this] var _sections: java.util.List[QuizSection] = _ //list of sections that make up a quiz
  
  def this(name: String, sections: List[QuizSection]) = {
    this()
    _name=name
    sections_=(sections)
  }
  
  def id = _id
  def name = _name
  def sections: List[QuizSection] = _sections.asScala.toList
  def sections_=(theSections: List[QuizSection]) { _sections = theSections.asJava}
    
  override def toString = { name }
}
object Quiz {
  def getById(id: Long)(implicit ipm: ScalaPersistenceManager = null): Option[models.mastery.Quiz] = {
    DataStore.execute { epm =>
      val cand=QQuiz.candidate()
      epm.query[Quiz].filter(cand.id.eq(id)).executeOption()
    }
  }
}

trait QQuiz extends PersistableExpression[Quiz]{
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _sections: ObjectExpression[List[QuizSection]] = new ObjectExpressionImpl[List[QuizSection]](this, "_sections")
  def sections: ObjectExpression[List[QuizSection]] = _sections
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