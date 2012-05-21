package models.quizzes

import javax.jdo.annotations._

import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class Kind {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _
  
  def this(name: String) = {
    this()
    name_=(name)
  }
  
  def id: Long = _id
  
  def name: String = _name
  def name_=(theName: String) { _name = theName }
}

trait QKind extends PersistableExpression[Kind] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
}

object QKind {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QKind = {
    new PersistableExpressionImpl[Kind](parent, name) with QKind
  }
  
  def apply(cls: Class[Kind], name: String, exprType: ExpressionType): QKind = {
    new PersistableExpressionImpl[Kind](cls, name, exprType) with QKind
  }
  
  private[this] lazy val jdoCandidate: QKind = candidate("this")
  
  def candidate(name: String): QKind = QKind(null, name, 5)
  
  def candidate(): QKind = jdoCandidate
  
  def parameter(name: String): QKind = QKind(classOf[Kind], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QKind = QKind(classOf[Kind], name, ExpressionType.VARIABLE)
}
