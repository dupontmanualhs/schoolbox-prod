package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import javax.jdo.listener.StoreCallback

import scalajdo.DataStore

@PersistenceCapable(detachable="true")
class Copy /*extends StoreCallback*/ {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  @Persistent
  private[this] var _purchaseGroup: PurchaseGroup = _
  def purchaseGroup: PurchaseGroup = _purchaseGroup
  def purchaseGroup_=(thePurchaseGroup: PurchaseGroup) { _purchaseGroup = thePurchaseGroup }

  private[this] var _number: Int = _
  def number: Int = _number
  def number_=(theNumber: Int) { _number = theNumber }

  private[this] var _isLost: Boolean = _
  def isLost: Boolean = _isLost
  def isLost_=(theIsLost: Boolean) { _isLost = theIsLost }

  private[this] var _deleted: Boolean = _
  def deleted: Boolean = _deleted
  def deleted_=(theDeleted: Boolean) { _deleted = theDeleted }

  def this(purchaseGroup: PurchaseGroup, number: Int, isLost: Boolean = false, deleted: Boolean = false) = {
    this()
    _purchaseGroup = purchaseGroup
    _number = number
    _isLost = isLost
    _deleted = deleted
  }

  val maxCopyNumber: Int = 99999

  override def toString: String = {
    this.getBarcode
  }

  def getBarcode(): String = {
    // Schoolcode is currently hardcoded - change this to use a variable
    "%s-%s-%05d".format(purchaseGroup.title.isbn, "200", number)
  }

  def isCheckedOut: Boolean = {
    val cand = QCheckout.candidate
    DataStore.pm.query[Checkout].filter(cand.copy.eq(this).and(cand.endDate.eq(null.asInstanceOf[java.sql.Date]))).executeList().nonEmpty
  }

  //TODO Fix this so that it works
  /* Causes a problem loading the data
  def jdoPreStore(): Unit = {
    // TODO - We need real exceptions
    if (number > maxCopyNumber) {
      throw new Exception("Copy number greater than 5 digits")
    }
    // Make this check to make sure that the number doesn't already exist
    DataStore.execute { tpm =>
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
  def getById(id: Long): Option[Copy] = {
    val cand = QCopy.candidate
    DataStore.pm.query[Copy].filter(cand.id.eq(id)).executeOption()
  }

  def getByBarcode(barcode: String): Option[Copy] = {
    val isbn = barcode.substring(0, 13)
    val copyNumber = barcode.substring(18).toInt
    val cand = QCopy.candidate
    val titleVar = QTitle.variable("titleVar")
    val pgVar = QPurchaseGroup.variable("pgVar")
    DataStore.pm.query[Copy].filter(cand.number.eq(copyNumber).and(cand.purchaseGroup.eq(pgVar)).and(
        pgVar.title.eq(titleVar)).and(titleVar.isbn.eq(isbn))).executeOption()
  }

  def makeUniqueCopies(pGroup: PurchaseGroup, quantity: Int): (Int, Int) = {
    val pm = DataStore.pm
    val cand = QCopy.candidate
    val nTitle = pGroup.title
    val pgVar = QPurchaseGroup.variable("pgVar")
    val allCopies: List[Copy] = pm.query[Copy].filter(pgVar.title.eq(nTitle)).orderBy(cand.number.desc).executeList()

    val firstNum = if (!allCopies.isEmpty) {
      allCopies.apply(0).number + 1
    } else {
      1
    }

    var lastNum = 0

    def makeMoreCopies(quantity: Int, pGroup: PurchaseGroup, copyNum: Int) {
      if (quantity == 1) {
        val c = new Copy(pGroup, copyNum)
        pm.makePersistent(c)
        lastNum = copyNum
      } else {
        val c = new Copy(pGroup, copyNum)
        pm.makePersistent(c)
        makeMoreCopies(quantity - 1, pGroup, copyNum + 1)
      }
    }

    makeMoreCopies(quantity, pGroup, firstNum)
    return (firstNum, lastNum)
  }
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

  private[this] lazy val _deleted: BooleanExpression = new BooleanExpressionImpl(this, "_deleted")
  def deleted: BooleanExpression = _deleted

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
