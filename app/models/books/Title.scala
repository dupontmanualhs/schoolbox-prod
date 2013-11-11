package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.PersistableFile
import org.joda.time.LocalDateTime
import config.users.UsesDataStore

@PersistenceCapable(detachable = "true")
class Title extends UsesDataStore {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  @Column(allowsNull = "false")
  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }

  @Column(allowsNull = "true")
  private[this] var _author: String = _
  def author: Option[String] = if (_author == null) None else Some(_author)
  def author_=(theAuthor: Option[String]) { _author = theAuthor.getOrElse(null) }
  def author_=(theAuthor: String) { _author = theAuthor }

  @Column(allowsNull = "true")
  private[this] var _publisher: String = _
  def publisher: Option[String] = if (_publisher == null) None else Some(_publisher)
  def publisher_=(thePublisher: Option[String]) { _publisher = thePublisher.getOrElse(null) }
  def publisher_=(thePublisher: String) { _publisher = thePublisher }

  @Unique
  @Column(allowsNull = "false")
  private[this] var _isbn: String = _
  def isbn: String = _isbn
  def isbn_=(theIsbn: String) { _isbn = theIsbn }

  @Column(allowsNull = "true")
  private[this] var _numPages: java.lang.Integer = _
  def numPages: Option[Int] = if (_numPages == null) None else Some(_numPages)
  def numPages_=(theNumPages: Option[Int]) {
    theNumPages match {
      case None => _numPages = null
      case Some(i) => _numPages = i
    }
  }
  def numPages_=(theNumPages: Int) { _numPages = theNumPages }

  @Column(allowsNull = "true")
  private[this] var _dimensions: String = _
  def dimensions: Option[String] = if (_dimensions == null) None else Some(_dimensions)
  def dimensions_=(theDimensions: Option[String]) { _dimensions = theDimensions.getOrElse(null) }
  def dimensions_=(theDimensions: String) { _dimensions = theDimensions }

  @Column(allowsNull = "true")
  private[this] var _weight: java.lang.Double = _
  def weight: Option[Double] = if (_weight == null) None else Some(_weight)
  def weight_=(theWeight: Option[Double]) {
    theWeight match {
      case Some(w) => _weight = w
      case _ => _weight = null
    }
  }
  def weight_=(theWeight: Double) { _weight = theWeight }

  // @Persistent(defaultFetchGroup="true")
  // @Embedded  
  // private[this] var _image: PersistableFile = _

  @Column(allowsNull = "false")
  private[this] var _verified: Boolean = _
  def verified: Boolean = _verified
  def verified_=(theVerified: Boolean) { _verified = theVerified }

  @Persistent
  @Column(allowsNull = "true")
  private[this] var _lastModified: java.sql.Timestamp = _
  def lastModified: Option[LocalDateTime] = Option(_lastModified).map(LocalDateTime.fromDateFields(_))
  def lastModified_=(theLastModified: Option[LocalDateTime]) { 
    if (theLastModified.isDefined) lastModified_=(theLastModified.get)
    else _lastModified = null
  }
  def lastModified_=(theLastModified: LocalDateTime) { _lastModified = new java.sql.Timestamp(theLastModified.toDate.getTime) }

  @Column(allowsNull = "true")
  private[this] var _image: String = _
  def image: Option[String] = if (_image == null) None else Some(_image)
  def image_=(theImage: Option[String]) { _image = theImage.getOrElse(null) }
  def image_=(theImage: String) { _image = theImage }

  def this(name: String, author: Option[String], publisher: Option[String], isbn: String, numPages: Option[Int],
    dimensions: Option[String], weight: Option[Double], verified: Boolean, lastModified: Option[LocalDateTime], image: Option[String] = None) = {
    this()
    name_=(name)
    author_=(author)
    publisher_=(publisher)
    isbn_=(isbn)
    numPages_=(numPages)
    dimensions_=(dimensions)
    weight_=(weight)
    verified_=(verified)
    lastModified_=(lastModified)
    image_=(image)
  }

  def howManyCopies(): Int = {
    val pgVar = QPurchaseGroup.variable("pg")
    val copyCand = QCopy.candidate
    dataStore.pm.query[Copy].filter(copyCand.isLost.eq(false).and(
      copyCand.purchaseGroup.eq(pgVar)).and(
        pgVar.title.eq(this)).and(copyCand.deleted.eq(false))).executeList().length
  }

  def howManyCheckedOut(): Int = {
    val pgVar = QPurchaseGroup.variable("pg")
    val copyCand = QCopy.candidate
    val coVar = QCheckout.variable("co")
    dataStore.pm.query[Copy].filter(copyCand.isLost.eq(false).and(
      copyCand.deleted.eq(false)).and(
        copyCand.purchaseGroup.eq(pgVar)).and(
          pgVar.title.eq(this)).and(
            coVar.copy.eq(copyCand)).and(
              coVar.endDate.eq(null.asInstanceOf[java.sql.Date]))).executeList().length
  }

  def howManyDeleted(): Int = {
    val pgVar = QPurchaseGroup.variable("pg")
    val copyCand = QCopy.candidate
    dataStore.pm.query[Copy].filter(copyCand.deleted.eq(true).and(
      copyCand.purchaseGroup.eq(pgVar)).and(
        pgVar.title.eq(this))).executeList().length
  }

  def howManyLost(): Int = {
    val pgVar = QPurchaseGroup.variable("pg")
    val copyCand = QCopy.candidate
    dataStore.pm.query[Copy].filter(copyCand.deleted.eq(false).and(
      copyCand.purchaseGroup.eq(pgVar)).and(
        pgVar.title.eq(this)).and(copyCand.isLost.eq(true))).executeList().length
  }

  def hasSameValues(other: Title): Boolean = {
    this.name == other.name && this.publisher == other.publisher &&
      this.author == other.author && this.isbn == other.isbn &&
      this.numPages == other.numPages && this.dimensions == other.dimensions &&
      this.weight == other.weight && this.verified == other.verified &&
      this.lastModified == other.lastModified
  }
}

object Title extends UsesDataStore {
  def getById(id: Long): Option[Title] = {
    val cand = QTitle.candidate
    dataStore.pm.query[Title].filter(cand.id.eq(id)).executeOption()
  }

  def getByIsbn(isbn: String): Option[Title] = {
    val cand = QTitle.candidate
    dataStore.pm.query[Title].filter(cand.isbn.eq(isbn)).executeOption()
  }

  def convertStrToDecimal(str: String): Double = {
    str.toDouble
  }

  def makeDimensionStrings(dim: Tuple3[Int, Int, Int]): String = {
    // Return the dimension as a String in the format l x w x h
    dim._1 + " x " + dim._2 + " x " + dim._3
  }

  def count(): Long = {
    val cand = QTitle.candidate
    dataStore.pm.query[Title].query.executeResultUnique(classOf[java.lang.Long], true, cand.count())
  }
  /**
   * Helper Method
   *
   * Given a list of the first 9 digits from a ten-digit ISBN,
   * returns the expected check digit (which could also be an X in
   * addition to the digits 0 through 9). The algorithm can be found here:
   * http://en.wikipedia.org/wiki/International_Standard_Book_Number#Check_digits
   */
  def tenDigitCheckDigit(digits: List[Int]): String = {
    val checkSum = digits.zipWithIndex.map(digitWithIndex => {
      val digit = digitWithIndex._1
      val index = digitWithIndex._2
      (10 - index) * digit
    }).sum
    val checkDigit = (11 - (checkSum % 11)) % 11
    if (checkDigit == 10) "X" else checkDigit.toString
  }

  /**
   * Helper Method
   *
   * Given a list of the first 12 digits from a 13-digit ISBN,
   * returns the expected check digit. The algorithm can be found
   * here:
   * http://en.wikipedia.org/wiki/International_Standard_Book_Number#Check_digits
   */
  def thirteenDigitCheckDigit(digits: List[Int]): String = {
    val checkSum = digits.zipWithIndex.map(digitWithIndex => {
      val digit = digitWithIndex._1
      val index = digitWithIndex._2
      digit * (if ((index % 2) == 0) 1 else 3)
    }).sum
    ((10 - (checkSum % 10)) % 10).toString
  }

  /**
   * Helper Method
   *
   * Given a possible ISBN (either 10- or 13-digit) with the check
   * digit removed, calculates the check digit, if possible. If the
   * given String is not the right length or has illegal characters,
   * returns None.
   */
  def checkDigit(isbn: String): Option[String] = {
    // if (isbn.matches("^\\d+$")) {
    try {
      val digits = isbn.toList.map(_.toString.toInt)
      digits.length match {
        case 9 => Some(tenDigitCheckDigit(digits))
        case 12 => Some(thirteenDigitCheckDigit(digits))
        case _ => None
      }
    } catch {
      case _: NumberFormatException => None
    }
    // } else None
  }

  /**
   * Helper Method
   *
   * Converts a valid 10-digit ISBN into the equivalent 13-digit one.
   * If the original String is not valid, may cause an exception.
   */
  def makeIsbn13(isbn10: String): String = {
    val isbn9 = isbn10.substring(0, 9)
    val isbn12 = "978" + isbn9
    isbn12 + checkDigit(isbn12).get
  }

  /**
   * Helper Method
   *
   * Given a possible ISBN, verifies that it's valid and
   * returns the 13-digit equivalent. If the original ISBN
   * is not valid, returns None. Any dashes that the user may
   * have entered are removed.
   */
  def asValidIsbn13(text: String): Option[String] = {
    def verify(possIsbn: String): Option[String] = {
      val noCheck = possIsbn.substring(0, possIsbn.length - 1)
      val check = checkDigit(noCheck)
      check match {
        case Some(cd) => if (possIsbn == noCheck + cd) Some(possIsbn) else None
        case _ => None
      }
    }
    val isbn = "-".r.replaceAllIn(text, "")
    isbn.length match {
      case 10 => verify(isbn).map(makeIsbn13(_))
      case 13 => verify(isbn)
      case _ => None
    }
  }
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

  private[this] lazy val _verified: BooleanExpression = new BooleanExpressionImpl(this, "_verified")
  def verified: BooleanExpression = _verified

  private[this] lazy val _lastModified: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_lastModified")
  def lastModified: ObjectExpression[java.sql.Date] = _lastModified

  private[this] lazy val _image: StringExpression = new StringExpressionImpl(this, "_image")
  def image: StringExpression = _image
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
