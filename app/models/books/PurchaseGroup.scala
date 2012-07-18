package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile
import util.DataStore

@PersistenceCapable(detachable="true")
class PurchaseGroup {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _title: Title = _
  @Persistent
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

  override def toString = DataStore.withTransaction { implicit pm =>
    val str = "Purchased %s: %d copies of %s at $%.2f each".format(purchaseDate, this.numCopies, title.name, price)

    var verb = this.numLost match {
      case 1 => "has"
      case _ => "have"
    }

    if (this.numLost > 0) {
      str + " (%d %s been lost)".format(this.numLost, verb)
    } else {
      str
    }
  }

  def numCopies(implicit pm: ScalaPersistenceManager): Int = {
    val copyCand = QCopy.candidate
    pm.query[Copy].filter(copyCand.isLost.eq(false).and(
      copyCand.purchaseGroup.eq(this))).executeList().length
  }

  def numLost(implicit pm: ScalaPersistenceManager): Int = {
    val copyCand = QCopy.candidate
    pm.query[Copy].filter(copyCand.isLost.eq(true).and(
      copyCand.purchaseGroup.eq(this))).executeList().length
  }

}

object PurchaseGroup {
  def getById(id: Long)(implicit pm: ScalaPersistenceManager): Option[PurchaseGroup] = {
    val cand = QPurchaseGroup.candidate
    pm.query[PurchaseGroup].filter(cand.id.eq(id)).executeOption()
  }

}

trait QPurchaseGroup extends PersistableExpression[PurchaseGroup] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _title: ObjectExpression[Title] = new ObjectExpressionImpl[Title](this, "_title")
  def title: ObjectExpression[Title] = _title

  private[this] lazy val _purchaseDate: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_purchaseDate")
  def purchaseDate: ObjectExpression[java.sql.Date] = _purchaseDate

  private[this] lazy val _price: NumericExpression[Double] = new NumericExpressionImpl[Double](this, "_price")
  def price: NumericExpression[Double] = _price
}

object QPurchaseGroup {
  def apply(parent: PersistableExpression[PurchaseGroup], name: String, depth: Int): QPurchaseGroup = {
    new PersistableExpressionImpl[PurchaseGroup](parent, name) with QPurchaseGroup
  }

  def apply(cls: Class[PurchaseGroup], name: String, exprType: ExpressionType): QPurchaseGroup = {
    new PersistableExpressionImpl[PurchaseGroup](cls, name, exprType) with QPurchaseGroup
  }

  private[this] lazy val jdoCandidate: QPurchaseGroup = candidate("this")

  def candidate(name: String): QPurchaseGroup = QPurchaseGroup(null, name, 5)

  def candidate(): QPurchaseGroup = jdoCandidate

  def parameter(name: String): QPurchaseGroup = QPurchaseGroup(classOf[PurchaseGroup], name, ExpressionType.PARAMETER)

  def variable(name: String): QPurchaseGroup = QPurchaseGroup(classOf[PurchaseGroup], name, ExpressionType.VARIABLE)
}
