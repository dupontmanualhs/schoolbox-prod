package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.Role

import scalajdo.DataStore

@PersistenceCapable(detachable = "true")
class LabelQueueSet {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  @Persistent
  private[this] var _role: Role = _
  def role: Role = _role
  def role_=(theRole: Role) { _role = theRole }

  @Persistent
  private[this] var _title: Title = _
  def title: Title = _title
  def title_=(theTitle: Title) { _title = theTitle }

  private[this] var _copyRange: String = _
  def copyRange: String = _copyRange
  def copyRange_=(theCopyRange: String) { _copyRange = copyRange }

  def this(role: Role, title: Title, copyRange: String) = {
    this()
    _role = role
    _title = title
    _copyRange = copyRange
  }

  override def toString: String = {
    "Copies " + copyRange + " of " + title.name
  }
}

object LabelQueueSet {
  def getById(id: Long): Option[LabelQueueSet] = {
    val cand = QLabelQueueSet.candidate
    DataStore.pm.query[LabelQueueSet].filter(cand.id.eq(id)).executeOption()
  }
}

trait QLabelQueueSet extends PersistableExpression[LabelQueueSet] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _role: ObjectExpression[Role] = new ObjectExpressionImpl[Role](this, "_role")
  def role: ObjectExpression[Role] = _role

  private[this] lazy val _title: ObjectExpression[Title] = new ObjectExpressionImpl[Title](this, "_title")
  def title: ObjectExpression[Title] = _title

  private[this] lazy val _copyRange: StringExpression = new StringExpressionImpl(this, "_copyRange")
  def copyRange: StringExpression = _copyRange
}

object QLabelQueueSet {
  def apply(parent: PersistableExpression[LabelQueueSet], name: String, depth: Int): QLabelQueueSet = {
    new PersistableExpressionImpl[LabelQueueSet](parent, name) with QLabelQueueSet
  }

  def apply(cls: Class[LabelQueueSet], name: String, exprType: ExpressionType): QLabelQueueSet = {
    new PersistableExpressionImpl[LabelQueueSet](cls, name, exprType) with QLabelQueueSet
  }

  private[this] lazy val jdoCandidate: QLabelQueueSet = candidate("this")

  def candidate(name: String): QLabelQueueSet = QLabelQueueSet(null, name, 5)

  def candidate(): QLabelQueueSet = jdoCandidate

  def parameter(name: String): QLabelQueueSet = QLabelQueueSet(classOf[LabelQueueSet], name, ExpressionType.PARAMETER)

  def variable(name: String): QLabelQueueSet = QLabelQueueSet(classOf[LabelQueueSet], name, ExpressionType.VARIABLE)
}
