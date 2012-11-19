package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.Perspective

@PersistenceCapable(detachable="true")
class Checkout {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _perspective: Perspective = _
  private[this] var _copy: Copy = _
  @Persistent
  private[this] var _startDate: java.sql.Date = _
  @Persistent
  private[this] var _endDate: java.sql.Date = _

  def this(perspective: Perspective, copy: Copy, startDate: java.sql.Date, endDate: java.sql.Date) = {
    this()
    _perspective = perspective
    _copy = copy
    _startDate = startDate
    _endDate = endDate
  }

  def id: Long = _id

  def perspective: Perspective = _perspective
  def perspective_=(thePerspective: Perspective) { _perspective = thePerspective }

  def copy: Copy = _copy
  def copy_=(theCopy: Copy) { _copy = theCopy }

  def startDate: java.sql.Date = _startDate
  def startDate_=(theStartDate: java.sql.Date) { _startDate = theStartDate }

  def endDate: java.sql.Date = _endDate
  def endDate_=(theEndDate: java.sql.Date) { _endDate = theEndDate }
  
  override def toString: String = {
    "Checkout: Copy %s to %s from %s to %s".format(copy, perspective.displayName, startDate, endDate)
  }
}

object Checkout {
  //TODO - Write the jdoPreStore method
}

trait QCheckout extends PersistableExpression[Checkout] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _perspective: ObjectExpression[Perspective] = new ObjectExpressionImpl[Perspective](this, "_perspective")
  def perspective: ObjectExpression[Perspective] = _perspective

  private[this] lazy val _copy: ObjectExpression[Copy] = new ObjectExpressionImpl[Copy](this, "_copy")
  def copy: ObjectExpression[Copy] = _copy

  private[this] lazy val _startDate: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_startDate")
  def startDate: ObjectExpression[java.sql.Date] = _startDate

  private[this] lazy val _endDate: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_endDate")
  def endDate: ObjectExpression[java.sql.Date] = _endDate
}

object QCheckout {
  def apply(parent: PersistableExpression[Checkout], name: String, depth: Int): QCheckout = {
    new PersistableExpressionImpl[Checkout](parent, name) with QCheckout
  }

  def apply(cls: Class[Checkout], name: String, exprType: ExpressionType): QCheckout = {
    new PersistableExpressionImpl[Checkout](cls, name, exprType) with QCheckout
  }

  private[this] lazy val jdoCandidate: QCheckout = candidate("this")

  def candidate(name: String): QCheckout = QCheckout(null, name, 5)

  def candidate(): QCheckout = jdoCandidate

  def parameter(name: String): QCheckout = QCheckout(classOf[Checkout], name, ExpressionType.PARAMETER)

  def variable(name: String): QCheckout = QCheckout(classOf[Checkout], name, ExpressionType.VARIABLE)
}
