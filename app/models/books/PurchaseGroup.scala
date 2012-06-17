package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

@PersistenceCapable(detachable="true")
class Course {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _title: Title = _
  private[this] var _purchaseDate: java.sql.Date = _
  private[this] var _price: Double = _

  def this(title: Title, purchaseDate: java.sql.Date, price: Double) = {
    this()
    _title = title
    _purchaseDate = purchaseDate
    _price = price
  }

  def id: Long = _id

  def title: Title = _title
  def title_=(theTitle: Title) { _title = theTitle }

  def purchaseDate: java.sql.Date = _purchaseDate
  def purchaseDate_=(thePurchaseDate: java.sql.Date) { _purchaseDate = thePurchaseDate }

  def price: Double = _price
  def price_=(thePrice: Double) { _price = thePrice }
}
