package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

@PersistenceCapable(detachable="true")
class Copy {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _purchaseGroup: PurchaseGroup = _
  private[this] var _number: Long = _
  private[this] var _isLost: Boolean = _ // TODO: Make this false by default

  def this(purchaseGroup: PurchaseGroup, number: Long, isLost: Boolean) = {
    this()
    _purchaseGroup = purchaseGroup
    _number = number
    _isLost = isLost
  }

  def id: Long = _id

  def purchaseGroup: PurchaseGroup = _purchaseGroup
  def purchaseGroup_=(thePurchaseGroup: PurchaseGroup) { _purchaseGroup = thePurchaseGroup }

  def number: Long = _number
  def number_=(theNumber: Long) { _number = theNumber }

  def isLost: Boolean = _isLost
  def isLost_=(theIsLost: Boolean) { _isLost = theIsLost }
}

object Copy {
  //def unicode()
  //TODO - Write this method. Should return the barcode

  //def save
  //TODO - Write the implmentation

  def isCheckedOut(): Boolean = {
    true
    //TODO - Write the implementation
  }

  //def getBarcode
  //TODO - Write the implementation

  //def getByBarcode
  //TODO - Write the implementation

  //def makeUniqueCopies
  //TODO - Write the implementation
}

trait QCopy extends PersistableExpression[Copy] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _purchaseGroup: ObjectExpression[PurchaseGroup] = new ObjectExpressionImpl[PurchaseGroup](this, "_purchaseGroup")
  def purchaseGroup: ObjectExpression[PurchaseGroup] = _purchaseGroup

  private[this] lazy val _number: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_number")
  def number: NumericExpression[Long] = _number

  private[this] lazy val _isLost: BooleanExpression = new BooleanExpressionImpl(this, "_isLost")
  def isLost: BooleanExpression = _isLost
}

object QCopy {
  def apply(parent: PersistableExpression[Copy], name: String, depth: Int): QCopy = {
    new PersistableExpressionImpl[Copy](parent, name) with QCopy
  }

  def apply(cls: Class[Copy], name: String, exprType: ExpressionType): QCopy = {
    new PersistableExpressionImpl[Copy](cls, name, exprType) with QCopy
  }

  private[this] lazy val jdoCandidate: QCopy = candidate("this")

  def candidate(name: String): QCopy = QCopy(null, name, 5)

  def candidate(): QCopy = jdoCandidate

  def parameter(name: String): QCopy = QCopy(classOf[Copy], name, ExpressionType.PARAMETER)

  def variable(name: String): QCopy = QCopy(classOf[Copy], name, ExpressionType.VARIABLE)
}
