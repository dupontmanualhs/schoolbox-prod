package models.mastery

import javax.jdo.annotations._
import models.mastery._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.courses.Section

@PersistenceCapable(detachable = "true")
class Quiz {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _sections: List[Section] = _
  private[this] var _name: String = _
  
  def this(name: String, sections: List[Section]) = {
    this()
    _name=name
    _sections=sections
  }
  
  override def toString = {
    _name
  }
}

trait QQuiz extends PersistableExpression[Quiz]{
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_name")
  def name: ObjectExpression[String] = _name
  
  private[this] lazy val _sections: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_sections")
  def sections: ObjectExpression[Section] = _sections
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