package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile

@PersistenceCapable(detachable="true")
class Title {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _name: String = _
  private[this] var _author: String = _
  private[this] var _publisher: String = _
  @Unique
  private[this] var _isbn: String = _
  private[this] var _numPages: Int = _
  private[this] var _dimensions: String = _
  private[this] var _weight: Double = _
  @Persistent(defaultFetchGroup="true")
  @Embedded  
  private[this] var _image: PersistableFile = _
  private[this] var _verified: Boolean = _
  private[this] var _lastModified: java.sql.Date = _

  def this(name: String, author: String, publisher: String, isbn: String, numPages: Int,
    dimensions: String, weight: Double, verified: Boolean, lastModified: java.sql.Date) = {
    this()
    _name = name
    _author = author
    _publisher = publisher
    _isbn = isbn
    _numPages = numPages
    _dimensions = dimensions
    _weight = weight
    // _image = image
    _verified = verified
    _lastModified = lastModified
  }

  def id: Long = _id

  def name: String = _name
  def name_=(theName: String) { _name = theName }

  def author: String = _author
  def author_=(theAuthor: String) { _author = theAuthor }

  def publisher: String = _publisher
  def publisher_=(thePublisher: String) { _publisher = thePublisher }

  def isbn: String = _isbn
  def isbn_=(theIsbn: String) { _isbn = theIsbn }

  def numPages: Int = _numPages
  def numPages_=(theNumPages: Int) { _numPages = theNumPages }

  def dimensions: String = _dimensions
  def dimensions_=(theDimensions: String) { _dimensions = theDimensions }

  def weight: Double = _weight
  def weight_=(theWeight: Double) { _weight = theWeight }
  
  def image: PersistableFile = _image
  def image_=(theImage: PersistableFile) { _image = theImage }

  def verified: Boolean = _verified
  def verified_(theVerified: Boolean) { _verified = theVerified }

  def lastModified: java.sql.Date = _lastModified
  def lastModified_=(theLastModified: java.sql.Date) { _lastModified = theLastModified }

  def howManyCopies(implicit pm: ScalaPersistenceManager): Int = {
    val pgVar = QPurchaseGroup.variable("pg")
    val copyCand = QCopy.candidate
    pm.query[Copy].filter(copyCand.isLost.eq(false).and(
        copyCand.purchaseGroup.eq(pgVar)).and(
        pgVar.title.eq(this))).executeList().length
  }
  
  def howManyCheckedOut(implicit pm: ScalaPersistenceManager): Int = {
    val pgVar = QPurchaseGroup.variable("pg")
    val copyCand = QCopy.candidate
    val coVar = QCheckout.variable("co")
    pm.query[Copy].filter(copyCand.isLost.eq(false).and(
        copyCand.purchaseGroup.eq(pgVar)).and(
        pgVar.title.eq(this)).and(
        coVar.copy.eq(copyCand)).and(
        coVar.endDate.eq(null.asInstanceOf[java.sql.Date]))).executeList().length
  }
}

object Title {
  def getById(id: Long)(implicit pm: ScalaPersistenceManager): Option[Title] = {
    val cand = QTitle.candidate
    pm.query[Title].filter(cand.id.eq(id)).executeOption()
  }
  
  def getByIsbn(isbn: String)(implicit pm: ScalaPersistenceManager): Option[Title] = {
    val cand = QTitle.candidate
    pm.query[Title].filter(cand.isbn.eq(isbn)).executeOption()
  }

  def hasSameValues(other: Title): Boolean = {
    true //TODO - Write the implementation
  }

  def convertStrToDecimal(str: String): Double = {
    12.0 //TODO - Write the implementation
  }

  def makeDimensionStrings(dim: Tuple3[Int, Int, Int]): String = {
    // Return the dimension as a String in the format l x w x h
    dim._1 + " x " + dim._2 + " x " + dim._3
    // TODO - Test this code
  }
  
  def count()(implicit pm: ScalaPersistenceManager): Long = {
    val cand = QTitle.candidate
    pm.query[Title].query.executeResultUnique(classOf[java.lang.Long], true, cand.count())
  }

  // def setSizeCallback
  // I have no idea what this is suposed to do
  //TODO - Figure out what this does and write the implementation
}

trait QTitle extends PersistableExpression[Title] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name

  private[this] lazy val _author: StringExpression = new StringExpressionImpl(this, "_author")
  def author: StringExpression = _author

  private[this] lazy val _publisher: StringExpression = new StringExpressionImpl(this, "_publisher")
  def publisher: StringExpression = _publisher

  private[this] lazy val _isbn: StringExpression = new StringExpressionImpl(this, "_isbn")
  def isbn: StringExpression = _isbn

  private[this] lazy val _numPages: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_numPages")
  def numPages: NumericExpression[Int] = _numPages

  private[this] lazy val _dimensions: StringExpression = new StringExpressionImpl(this, "_dimensions")
  def dimensions: StringExpression = _dimensions

  private[this] lazy val _weight: NumericExpression[Double] = new NumericExpressionImpl[Double](this, "_weight")
  def weight: NumericExpression[Double] = _weight

  // Add image field

  private[this] lazy val _verified: BooleanExpression = new BooleanExpressionImpl(this, "_verified")
  def verified: BooleanExpression = _verified

  private[this] lazy val _lastModified: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_lastModified")
  def lastModified: ObjectExpression[java.sql.Date] = _lastModified
}

object QTitle {
  def apply(parent: PersistableExpression[Title], name: String, depth: Int): QTitle = {
    new PersistableExpressionImpl[Title](parent, name) with QTitle
  }

  def apply(cls: Class[Title], name: String, exprType: ExpressionType): QTitle = {
    new PersistableExpressionImpl[Title](cls, name, exprType) with QTitle
  }

  private[this] lazy val jdoCandidate: QTitle = candidate("this")

  def candidate(name: String): QTitle = QTitle(null, name, 5)

  def candidate(): QTitle = jdoCandidate

  def parameter(name: String): QTitle = QTitle(classOf[Title], name, ExpressionType.PARAMETER)

  def variable(name: String): QTitle = QTitle(classOf[Title], name, ExpressionType.VARIABLE)
}
