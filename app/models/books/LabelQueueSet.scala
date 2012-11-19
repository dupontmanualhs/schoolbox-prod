package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.Perspective

@PersistenceCapable(detachable="true")
class LabelQueueSet {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _perspective: Perspective = _
  private[this] var _title: Title = _
  private[this] var _copyRange: String = _

  def this(perspective: Perspective, title: Title, copyRange: String) = {
    this()
    _perspective = perspective
    _title = title
    _copyRange = copyRange
  }

  def id: Long = _id

  def perspective: Perspective = _perspective
  def perspective_=(thePerspective: Perspective) { _perspective = thePerspective }

  def title: Title = _title
  def title_=(theTitle: Title) { _title = theTitle }

  def copyRange: String = _copyRange
  def copyRange_=(theCopyRange: String) { _copyRange = copyRange }

  override def toString: String = {
    "Copies " + copyRange + " of " + title.name
  }
}

object LabelQueueSet {
}

trait QLabelQueueSet extends PersistableExpression[LabelQueueSet] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _perspective: ObjectExpression[Perspective] = new ObjectExpressionImpl[Perspective](this, "_perspective")
  def perspective: ObjectExpression[Perspective] = _perspective

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
