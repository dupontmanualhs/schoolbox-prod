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
import javax.imageio._
import java.io._

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
    // if (isbn.matches("^\\d+$")) {
      val digits = isbn.toList.map(_.toString.toInt)
      digits.length match {
        case 9 => Some(tenDigitCheckDigit(digits))
        case 12 => Some(thirteenDigitCheckDigit(digits))
        case _ => None
      }
   // } else None
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
  
  object TitleForm extends Form {
    val isbn = new TextField("isbn") {
      override val minLength = Some(10)
      override val maxLength = Some(13)
      override def validators = super.validators ++ List(Validator((str: String) => asValidIsbn13(str) match {
        case None => ValidationError("This value must be a valid 10- or 13-digit ISBN.")
	    case Some(isbn) => ValidationError(Nil)
    }), Validator((str: String) => Title.getByIsbn(str) match {
        case Some(isbn) => ValidationError("ISBN already exists in database.")
        case None => ValidationError(Nil)}))
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
          val t = new Title (vb.valueOf(TitleForm.name), vb.valueOf(TitleForm.author), 
          vb.valueOf(TitleForm.publisher), vb.valueOf(TitleForm.isbn), vb.valueOf(TitleForm.numPages), 
          vb.valueOf(TitleForm.dimensions), vb.valueOf(TitleForm.weight), true, 
          new java.sql.Date(new java.util.Date().getTime()), Some("public/images/books/" + vb.valueOf(TitleForm.isbn)+ ".jpg"))
        request.pm.makePersistent(t)

        vb.valueOf(TitleForm.imageUrl) match {
          case Some(url) => try {
            downloadImage(url, vb.valueOf(TitleForm.isbn))
            Redirect(routes.Books.addTitle()).flashing("message" -> "Title added successfully")
          } catch {
            case e: Exception => Redirect(routes.Books.addTitle()).flashing("message" -> "Image not downloaded. Update the title's image to try downloading again")
          }
          case None => Redirect(routes.Books.addTitle()).flashing("message" -> "Title added without an image")
        }
      }
    }
  }
}

  def downloadImage(url: java.net.URL, isbn: String) = {
    val pic = ImageIO.read(url)
    ImageIO.write(pic, "jpg", new File("public/images/books/" + isbn + ".jpg"))
  }

  def confirmation() = TODO
  
  def verifyTitle(isbnNum: Long) = TODO
  
  def addCopiesToPg(pgId: Long) = TODO
  
  def addPurchaseGroup(titleId: Long) = TODO
  
  def addLabelsToQueue() = TODO
  
  def printCenter() = TODO
  
  def bulkCheckoutHelper() = TODO
  
  def bulkCheckout() = TODO

  object CheckoutForm extends Form {
    val barcode = new TextField("Barcode") {
      override val minLength = Some(21)
      override val maxLength = Some(23)
    }
    val studentNumber = new TextField("Student Number")
    
    val fields = List(barcode, studentNumber)
  }

  def checkout = DbAction { implicit request =>
    if (request.method == "GET") Ok(views.html.books.checkout(Binding(CheckoutForm)))
    else {
      Binding(CheckoutForm, request) match {
        case ib: InvalidBinding => Ok(views.html.books.checkout(ib))
        case vb: ValidBinding => {
          Redirect(routes.Books.checkout()).flashing("message" -> "No Errors")
      }
    }
  }
}
  
  def lookup() = TODO
  
  def inspect() = TODO
  
  def findBooksOut() = TODO
  
  def booksOut(perspectiveId: Long) = TODO
  
  def findCopyHistory() = DbAction { implicit req =>
    object ChooseCopyForm extends Form {
      val copyId = new NumericField[Int]("Copy ID")

      def fields = List(copyId)
    }
    if (req.method == "GET") {
      Ok(html.books.findCopyHistory(Binding(ChooseCopyForm)))
    } else {
      Binding(ChooseCopyForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findCopyHistory(ib))
        case vb: ValidBinding => {
          val lookupCopyId: Long = vb.valueOf(ChooseCopyForm.copyId)
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
  
  def checkInLostCopy() = TODO
  
  def delete(id: Long) = TODO
  
  def confirmDelete() = TODO
  
  def checkoutHistory(perspectiveId: Long) = DbAction { implicit req =>
  implicit val pm = req.pm
  val df = new java.text.SimpleDateFormat("MM/dd/yyyy")

  val perspectiveCand = QPerspective.candidate
  pm.query[Perspective].filter(perspectiveCand.id.eq(perspectiveId)).executeOption() match {
    case None => NotFound("No student with the given id")
    case Some(currentPerspective) => {
      val checkoutCand = QCheckout.candidate
      val currentBooks = pm.query[Checkout].filter(checkoutCand.perspective.eq(currentPerspective)).executeList()
      val studentName = currentPerspective.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String, String)] = currentBooks.map(co => { (co.copy.purchaseGroup.title.name, df.format(co.startDate),
        if (co.endDate == null) "" else df.format(co.endDate))})
      Ok(views.html.books.checkoutHistory(header,rows))
    }
  }
}

def findCheckoutHistory() = DbAction { implicit req =>
    object ChoosePerspectiveForm extends Form {
      val perspectiveId = new NumericField[Int]("Perspective ID")

      def fields = List(perspectiveId)
    }
    if (req.method == "GET") {
      Ok(html.books.findCheckoutHistory(Binding(ChoosePerspectiveForm)))
    } else {
      Binding(ChoosePerspectiveForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findCheckoutHistory(ib))
        case vb: ValidBinding => {
          val lookupPerspectiveId: Long = vb.valueOf(ChoosePerspectiveForm.perspectiveId)
          Redirect(routes.Books.checkoutHistory(lookupPerspectiveId))
        }
      }
    }
  }

  def findCurrentCheckouts() = DbAction { implicit req =>
    object ChoosePerspectiveForm extends Form {
      val perspectiveId = new NumericField[Int]("Perspective ID")

      def fields = List(perspectiveId)
    }
    if (req.method == "GET") {
      Ok(html.books.findPerspectiveHistory(Binding(ChoosePerspectiveForm)))
    } else {
      Binding(ChoosePerspectiveForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findPerspectiveHistory(ib))
        case vb: ValidBinding => {
          val lookupPerspectiveId: Long = vb.valueOf(ChoosePerspectiveForm.perspectiveId)
          Redirect(routes.Books.currentCheckouts(lookupPerspectiveId))
        }
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
      val studentName = currentPerspective.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String)] = currentBooks.map(co => { (co.copy.purchaseGroup.title.name, df.format(co.startDate))})
      Ok(views.html.books.currentCheckouts(header,rows))
    }
  }
}

  def checkoutsByTeacherStudents() = TODO
  
  def statistics() = TODO
  
  def copyStatusByTitle() = TODO /* DbAction { implicit req =>
    implicit val pm = req.pm
    val form = Form(
      "titleId" -> longNumber
    )
    Ok(views.html.books.copyStatusByTitleForm())
    
  }*/
  
  def allBooksOut(grade: Int = 13) = TODO
}
