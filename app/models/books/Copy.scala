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
