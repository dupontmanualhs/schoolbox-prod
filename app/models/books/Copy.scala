package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import javax.jdo.listener.StoreCallback
import util.DataStore

@PersistenceCapable(detachable="true")
class Copy /*extends StoreCallback*/ {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _purchaseGroup: PurchaseGroup = _
  private[this] var _number: Int = _
  private[this] var _isLost: Boolean = _ // TODO: Make this false by default

  def this(purchaseGroup: PurchaseGroup, number: Int, isLost: Boolean = false) = {
    this()
    _purchaseGroup = purchaseGroup
    _number = number
    _isLost = isLost
  }

  def id: Long = _id

  def purchaseGroup: PurchaseGroup = _purchaseGroup
  def purchaseGroup_=(thePurchaseGroup: PurchaseGroup) { _purchaseGroup = thePurchaseGroup }

  def number: Long = _number
  def number_=(theNumber: Int) { _number = theNumber }

  def isLost: Boolean = _isLost
  def isLost_=(theIsLost: Boolean) { _isLost = theIsLost }

  val maxCopyNumber: Int = 99999

  override def toString: String = {
    this.getBarcode
  }

  def getBarcode(): String = {
    // Schoolcode is currently hardcoded - change this to use a variable
    "%s-%s-%05d".format(purchaseGroup.title.isbn, "200", number)
  }

  def isCheckedOut(implicit pm: ScalaPersistenceManager = null): Boolean = {
    def query(epm: ScalaPersistenceManager): Boolean = {
      val cand = QCheckout.candidate
      epm.query[Checkout].filter(cand.copy.eq(this).and(cand.endDate.eq(null.asInstanceOf[java.sql.Date]))).executeList().isEmpty
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }

  /* Causes a problem loading the data
  def jdoPreStore(): Unit = {
    // TODO - We need real exceptions
    if (number > maxCopyNumber) {
      throw new Exception("Copy number greater than 5 digits")
    }
    // Make this check to make sure that the number doesn't already exist
    DataStore.withTransaction { tpm =>
      val cand = QCopy.candidate
      val pgVar = QPurchaseGroup.variable("pgVar")
      val others = tpm.query[Copy].filter(cand.number.eq(this.number).and(cand.purchaseGroup.eq(pgVar)).and(
          pgVar.title.eq(this.purchaseGroup.title)).and(cand.id.ne(this.id))).executeList()
      if (!others.isEmpty) throw new Exception("Copy number already used")
    }
  }
  */
}

object Copy {
  def getById(id: Long)(implicit pm: ScalaPersistenceManager = null): Option[Copy] = {
    def query(epm: ScalaPersistenceManager): Option[Copy] = {
      val cand = QCopy.candidate
      epm.query[Copy].filter(cand.id.eq(id)).executeOption()
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }

  def getByBarcode(barcode: String)(implicit pm: ScalaPersistenceManager = null): Option[Copy] = {
    def query(epm: ScalaPersistenceManager): Option[Copy] = {
      val isbn = barcode.substring(0, 13)
      val copyNumber = barcode.substring(18).toInt
      val cand = QCopy.candidate
      val titleVar = QTitle.variable("titleVar")
      val pgVar = QPurchaseGroup.variable("pgVar")
      epm.query[Copy].filter(cand.number.eq(copyNumber).and(cand.purchaseGroup.eq(pgVar)).and(
        pgVar.title.eq(titleVar)).and(titleVar.isbn.eq(isbn))).executeOption()
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }

  def makeUniqueCopies(pGroup: PurchaseGroup, quantity: Int)(implicit pm: ScalaPersistenceManager = null): Unit = {
    def query(epm: ScalaPersistenceManager): List[Copy] = {
      val cand = QCopy.candidate
      val nTitle = pGroup.title
      val pgVar = QPurchaseGroup.variable("pgVar")
      epm.query[Copy].filter(pgVar.title.eq(nTitle)).orderBy(cand.number.desc).executeList()
    }
  }
  //TODO - Write the implementation
}

trait QCopy extends PersistableExpression[Copy] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _purchaseGroup: ObjectExpression[PurchaseGroup] = new ObjectExpressionImpl[PurchaseGroup](this, "_purchaseGroup")
  def purchaseGroup: ObjectExpression[PurchaseGroup] = _purchaseGroup

  private[this] lazy val _number: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_number")
  def number: NumericExpression[Int] = _number

  private[this] lazy val _isLost: BooleanExpression = new BooleanExpressionImpl(this, "_isLost")
  def isLost: BooleanExpression = _isLost

  private[this] lazy val _checkout: ObjectExpression[Checkout] = new ObjectExpressionImpl[Checkout](this, "_checkout")
  def checkout: ObjectExpression[Checkout] = _checkout
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
