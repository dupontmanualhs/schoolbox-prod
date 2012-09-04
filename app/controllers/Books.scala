package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import models.books._
import models.users._
import forms._
import forms.fields._
import views.html
import forms.validators.Validator
import forms.validators.ValidationError

object Books extends Controller {
  /**
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
   * Given a list of tho first 12 digits from a 13-digit ISBN,
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
   * Given a possible ISBN (either 10- or 13-digit) with the check
   * digit removed, calculates the check digit, if possible. If the
   * given String is not the right length or has illegal characters,
   * returns None.
   */
  def checkDigit(isbn: String): Option[String] = {
    if (isbn.matches("^\\d+$")) {
      val digits = isbn.toList.map(_.toString.toInt)
      digits.length match {
        case 9 => Some(tenDigitCheckDigit(digits))
        case 12 => Some(thirteenDigitCheckDigit(digits))
        case _ => None
      }
    } else None
  }

  /**
   * Converts a valid 10-digit ISBN into the equivalent 13-digit one.
   * If the original String is not valid, may cause an exception.
   */
  def makeIsbn13(isbn10: String): String = {
    val isbn9 = isbn10.substring(0, 9)
    val isbn12 = "978" + isbn9
    isbn12 + checkDigit(isbn12).get
  }
  
  /**
   * Given a possible ISBN, verifies that it's valid and 
   * returns the 13-digit equivalent. If the original ISBN
   * is not valid, returns None. Any dashes that the user may
   * have entered are removed.
   */
  def asValidIsbn13(text: String): Option[String] = {
    def verify(possIsbn: String): Option[String] = {
      val noCheck = possIsbn.substring(0, possIsbn.length - 1)
      val check = checkDigit(noCheck)
      if (possIsbn == noCheck + check) Some(possIsbn) else None
    }
    val isbn = "-".r.replaceAllIn(text, "")
    isbn.length match {
      case 10 => verify(isbn).map(makeIsbn13(_))
      case 13 => verify(isbn)
      case _ => None
    }
  }
  
  object TitleForm extends Form {
    val isbn = new TextField("isbn") {
      override val minLength = Some(10)
      override val maxLength = Some(13)
      override val validators = List(Validator((str: String) => asValidIsbn13(str) match {
        case None => ValidationError("This value must be a valid 10- or 13-digit ISBN.")
	    case Some(isbn) => ValidationError(Nil)
      }))
    }
    val name = new TextField("name") { override val maxLength = Some(80) }
    val author = new TextFieldOptional("author(s)") { override val maxLength = Some(80) }
    val publisher = new TextFieldOptional("publisher") { override val maxLength = Some(80) }
    val numPages = new NumericFieldOptional[Int]("numberOfPages")
    val dimensions = new TextFieldOptional("dimensions")
    val weight = new NumericFieldOptional[Double]("weight")
    val imageUrl = new UrlFieldOptional("imageUrl")
    
    val fields = List(isbn, name, author, publisher, numPages, dimensions, weight, imageUrl)
  }
  
  def addTitle = DbAction { implicit request =>
    if (request.method == "GET") Ok(views.html.books.addTitle(Binding(TitleForm)))
    else {
      Binding(TitleForm, request) match {
        case ib: InvalidBinding => Ok(views.html.books.addTitle(ib))
        case vb: ValidBinding => {
          // TODO: try to get image from url
          Redirect(routes.Application.index)
        }
      }
    }
  }
  
  def confirmation() = TODO
  
  def confirmationSubmit() = TODO
  
  def verifyTitle(isbnNum: Long) = TODO
  
  def verifyTitleSubmit(isbnNum: Long) = TODO
  
  def addCopiesToPg(pgId: Long) = TODO
  
  def addCopiesToPgSubmit(pgId: Long) = TODO
  
  def addPurchaseGroup(titleId: Long) = TODO
  
  def addPurchaseGroupSubmit(titleId: Long) = TODO
  
  def addLabelsToQueue() = TODO
  
  def addLabelsToQueueSubmit() = TODO
  
  def printCenter() = TODO
  
  def printCenterSubmit() = TODO
  
  def bulkCheckoutHelper() = TODO
  
  def bulkCheckout() = TODO
  
  def bulkCheckoutSubmit() = TODO
  
  def checkout() = TODO
  
  def checkoutSubmit() = TODO
  
  def lookup() = TODO
  
  def inspect() = TODO
  
  def findBooksOut() = TODO
  
  def findBooksOutSubmit() = TODO
  
  def booksOut(perspectiveId: Long) = TODO
  
  def findCopyHistory() = DbAction { implicit req =>
    object ChooseCopyForm extends Form {
      val copyId = new NumericField[Double]("Copy ID")
      // TODO - Write a NumericField[Long] and use that here

      def fields = List(copyId)
    }
    if (req.method == "GET") {
      Ok(html.books.findCopyHistory(Binding(ChooseCopyForm)))
    } else {
      Binding(ChooseCopyForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findCopyHistory(ib))
        case vb: ValidBinding => {
          val lookupCopyId: Long = vb.valueOf(ChooseCopyForm.copyId).toLong
          Redirect(routes.Books.copyHistory(lookupCopyId))
        }
      }
    }
  }
  
  def copyHistory(copyId: Long) = DbAction { implicit req =>
    implicit val pm = req.pm
    val df = new java.text.SimpleDateFormat("MM/dd/yyyy")
    val copyCand = QCopy.candidate
    pm.query[Copy].filter(copyCand.id.eq(copyId)).executeOption() match {
      case None => NotFound("no copy with the given id")
      case Some(copy) => {
        val header = "Copy #%d of %s".format(copy.number, copy.purchaseGroup.title.name)
        val coCand = QCheckout.candidate
        val rows: List[(String, String, String)] = pm.query[Checkout].filter(coCand.copy.eq(copy)).executeList().map(co => {
          (co.perspective.formalName, df.format(co.startDate), if (co.endDate == null) "" else df.format(co.endDate))
        })
        Ok(views.html.books.copyHistory(header, rows))
      }
    }
  }
  
  def confirmCopyLost(copyId: Long) = TODO
  
  def checkIn() = TODO
  
  def checkInSubmit() = TODO
  
  def checkInLostCopy() = TODO
  
  def delete(id: Long) = TODO
  
  def deleteSubmit(id: Long) = TODO
  
  def confirmDelete() = TODO
  
  def confirmDeleteSubmit() = TODO

  def checkoutHistory(perspectiveId: Long) = DbAction { implicit req =>
  implicit val pm = req.pm
  val df = new java.text.SimpleDateFormat("MM/dd/yyyy")

  val perspectiveCand = QPerspective.candidate
  pm.query[Perspective].filter(perspectiveCand.id.eq(perspectiveId)).executeOption() match {
    case None => NotFound("No student with the given id")
    case Some(currentPerspective) => {
      val checkoutCand = QCheckout.candidate
      val currentBooks = pm.query[Checkout].filter(checkoutCand.perspective.eq(currentPerspective)).executeList()
      val studentName = currentBooks(0).perspective.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String, String)] = currentBooks.map(co => { (co.copy.purchaseGroup.title.name, df.format(co.startDate),
        if (co.endDate == null) "" else df.format(co.endDate))})
      Ok(views.html.books.checkoutHistory(header,rows))
    }
  }
}

  def currentCheckouts(perspectiveId: Long) = DbAction { implicit req =>
  implicit val pm = req.pm
  val df = new java.text.SimpleDateFormat("MM/dd/yyyy")

  val perspectiveCand = QPerspective.candidate
  pm.query[Perspective].filter(perspectiveCand.id.eq(perspectiveId)).executeOption() match {
    case None => NotFound("No student with the given id")
    case Some(currentPerspective) => {
      val checkoutCand = QCheckout.candidate
      val currentBooks = pm.query[Checkout].filter(checkoutCand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(checkoutCand.perspective.eq(currentPerspective))).executeList()
      val studentName = currentBooks(0).perspective.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String)] = currentBooks.map(co => { (co.copy.purchaseGroup.title.name, df.format(co.startDate))})
      Ok(views.html.books.currentCheckouts(header,rows))
    }
  }
}

  def checkoutHistorySubmit() = TODO
  
  def checkoutsByTeacherStudents() = TODO
  
  def checkoutsByTeacherStudentsSubmit() = TODO
 
  def statistics() = TODO
  
 
  def copyStatusByTitle() = TODO /* DbAction { implicit req =>
    implicit val pm = req.pm
    val form = Form(
      "titleId" -> longNumber
    )
    Ok(views.html.books.copyStatusByTitleForm())
    
  }*/
  
  def copyStatusByTitleSubmit() = TODO
  
  def allBooksOut(grade: Int = 13) = TODO
}
