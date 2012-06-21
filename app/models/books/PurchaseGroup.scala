package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

@PersistenceCapable(detachable="true")
class PurchaseGroup {
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

object PurchaseGroup {
  def numCopies(): Int = {
    123
    //TODO - Write the implementation
  }

  def numLost(): Int = {
    23
    //TODO - Write the implementation
  }

  def shortDescription(): String = {
    // Returns a description in the form: "x copies purchased on <date> at $<amount> each"
    "123 copies purchased on January 1, 2000 at $60.00 each"
    //TODO - Write the implementation
  }

  def unicode(): String = {
    // Returns the short description with the title
    // title: description
    // TODO - Write the implementation
    "title: description"
  }
}

trait QPurchaseGroup extends PersistableExpression[PurchaseGroup] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _title: ObjectExpression[Title] = new ObjectExpressionImpl[Title](this, "_title")
  def title: ObjectExpression[Title] = _title

  private[this] lazy val _purchaseDate: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_purchaseDate")
  def purchaseDate: ObjectExpression[java.sql.Date] = _purchaseDate

  private[this] lazy val _price: NumericExpression[Double] = new NumericExpression[Double](this, "_price")
  def price: NumericExpression[Double] = _price
}

object QPurchaseGroup {
  def apply(parent: PersistableExpression[PurchaseGroup], name: String, depth: Int): QPurchaseGroup = {
    new PersistableExpressionImpl[PurchaseGroup](parent, name) with QPurchaseGroup
  }

  def apply(cls: Class[PurchaseGroup], name: String, exprType: ExpressionType): QPurchaseGroup = {
    new PersistableExpressionImpl[PurchaseGroup](cls, name, exprType) with PurchaseGroup
  }

  private[this] lazy val jdoCandidate: QPurchaseGroup = candidate("this")

  def candidate(name: String): QUser = QUser(null, name, 5)

  def candidate(): QPurchaseGroup = jdoCandidate

  def parameter(name: String): QPurchaseGroup = QPurchaseGroup(classOf[PurchaseGroup], name, ExpressionType.PARAMETER)

  def variable(name: String): QPurchaseGroup = QPurchaseGroup(classOf[PurchaseGroup], name, ExpressionType.VARIABLE)
}
