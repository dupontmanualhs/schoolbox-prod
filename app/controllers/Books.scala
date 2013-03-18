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
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

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
    }
    catch {
      case _: NumberFormatException => None
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
    val isbn = new TextField("ISBN") {
      override val minLength = Some(10)
      override val maxLength = Some(13)
      override def validators = super.validators ++ List(Validator((str: String) => asValidIsbn13(str) match {
        case None => ValidationError("This value must be a valid 10 or 13-digit ISBN.")
	    case Some(isbn) => ValidationError(Nil)
    }), Validator((str: String) => Title.getByIsbn(str) match {
        case Some(isbn) => ValidationError("ISBN already exists in database.")
        case None => ValidationError(Nil)}))
    }
    val name = new TextField("Name") { override val maxLength = Some(80) }
    val author = new TextFieldOptional("Author(s)") { override val maxLength = Some(80) }
    val publisher = new TextFieldOptional("Publisher") { override val maxLength = Some(80) }
    val numPages = new NumericFieldOptional[Int]("Number Of Pages")
    val dimensions = new TextFieldOptional("Dimensions (in)")
    val weight = new NumericFieldOptional[Double]("Weight (lbs)")
    val imageUrl = new UrlFieldOptional("Image URL")
    
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
  
  object AddPurchaseGroupForm extends Form {
    val isbn = new TextField("isbn") {
      override val minLength = Some(10)
      override val maxLength = Some(13)
      override def validators = super.validators ++ List(Validator((str: String) => asValidIsbn13(str) match {
          case None => ValidationError("This value must be a valid 10 or 13-digit ISBN.")
          case Some(isbn) => ValidationError(Nil)
        }))
      }
    val purchaseDate = new DateField("Purchase Date")
    val price = new NumericField[Double]("Price")
    val numCopies = new NumericField[Int]("Number of Copies")

    val fields = List(isbn, purchaseDate, price, numCopies)
    }

  def addPurchaseGroup = DbAction { implicit request =>
    if (request.method == "GET") Ok(views.html.books.addPurchaseGroup(Binding(AddPurchaseGroupForm)))
      else {
      implicit val pm = request.pm
      Binding(AddPurchaseGroupForm, request) match {
        case ib: InvalidBinding => Ok(views.html.books.addPurchaseGroup(ib))
        case vb: ValidBinding => {
        Title.getByIsbn(vb.valueOf(AddPurchaseGroupForm.isbn)) match {
          case None => Redirect(routes.Books.addPurchaseGroup()).flashing("message" -> "Title with the given ISBN not found")
          // TODO - Ask if the user would like to add the title if it is not found
          case Some(t) => {
            val p = new PurchaseGroup(t, vb.valueOf(AddPurchaseGroupForm.purchaseDate), vb.valueOf(AddPurchaseGroupForm.price))
            request.pm.makePersistent(p)

            // Next Copy Number
            val cand = QCopy.candidate
            val pCand = QPurchaseGroup.variable("pCand")
            val currentCopies = pm.query[Copy].filter(cand.purchaseGroup.eq(pCand).and(pCand.title.eq(t))).executeList()
            val newStart = currentCopies.length match {
              case 0 => 1
              case _ => {
                val maxCopy = currentCopies.sortWith((c1, c2) => c1.number < c2.number).last.number
                maxCopy + 1
              }
            }

            def addCopies(copyNumber: Int, copyNumberEnd: Int, purchaseGroup: PurchaseGroup): Unit = {
              if (copyNumber == copyNumberEnd) {
                val cpy = new Copy(purchaseGroup, copyNumber, false)
                request.pm.makePersistent(cpy)
              } else {
                val cpy = new Copy(purchaseGroup, copyNumber, false)
                request.pm.makePersistent(cpy)
                addCopies(copyNumber + 1, copyNumberEnd, purchaseGroup)
              }
            }

            // Add New Copies
            val copyNumberEnd = newStart + vb.valueOf(AddPurchaseGroupForm.numCopies) - 1
            addCopies(newStart, copyNumberEnd, p)
            val addedCopiesString = "copies " + newStart + " through " + copyNumberEnd + " added."

            val msg = "Purchase Group successfully added for: " + t.name + ". With " + addedCopiesString
            Redirect(routes.Books.addPurchaseGroup()).flashing("message" -> msg)
          }
        }
      }
    }
  }
}

  def addLabelsToQueue() = TODO
  
  def printCenter() = TODO
  
  def bulkCheckoutHelper() = TODO
  
  def bulkCheckout() = TODO

  object CheckoutForm extends Form {
    val barcode = new TextField("Barcode") {
      override val minLength = Some(21)
      override val maxLength = Some(23)
    }
    val student = new TextField("Student")
    
    val fields = List(barcode, student)
  }

  def checkout = DbAction { implicit request =>
    if (request.method == "GET") Ok(views.html.books.checkout(Binding(CheckoutForm)))
      else {
      implicit val pm = request.pm
      Binding(CheckoutForm, request) match {
        case ib: InvalidBinding => Ok(views.html.books.checkout(ib))
        case vb: ValidBinding => {
          val student = Student.getByStateId(vb.valueOf(CheckoutForm.student))
          val copy = Copy.getByBarcode(vb.valueOf(CheckoutForm.barcode))
          student match {
            case None => Redirect(routes.Books.checkout()).flashing("message" -> "No such student.")
            case Some(stu) => {
              copy match {
                case None => Redirect(routes.Books.checkout()).flashing("message" -> "No copy with that barcode.")
                case Some(cpy) => {
                  if (cpy.isCheckedOut) {
                    Redirect(routes.Books.checkout()).flashing("message" -> "Copy already checked out")
                  } else {
                    val c = new Checkout(stu, cpy, new java.sql.Date(new java.util.Date().getTime()), null)
                    request.pm.makePersistent(c)
                    Redirect(routes.Books.checkout()).flashing("message" -> "Copy successfully checked out.")
                  }
                }
              }
            }
          }
      }
    }
  }
}

  object CheckoutBulkForm extends Form {
    val student = new TextField("Student")

    val fields = List(student)
  }

  def checkoutBulk() = DbAction { implicit request =>
    if (request.method == "GET") {
      Ok(html.books.checkoutBulk(Binding(CheckoutBulkForm)))
    } else {
      implicit val pm = request.pm
      Binding(CheckoutBulkForm, request) match {
        case ib: InvalidBinding => Ok(html.books.checkoutBulk(ib))
        case vb: ValidBinding => {
          val checkoutStu: String = vb.valueOf(CheckoutBulkForm.student)
          Student.getByStateId(checkoutStu) match {
            case None => Redirect(routes.Books.checkoutBulk).flashing("message" -> "Student not found.")
            case Some(s) => Redirect(routes.Books.checkoutBulkHelper(checkoutStu))
          }
        }
      }
    }
  }

  object CheckoutBulkHelperForm extends Form {
    val barcode = new TextField("Barcode") {
      override val minLength = Some(21)
      override val maxLength = Some(23)
    }

    val fields = List(barcode)
  }

  def checkoutBulkHelper(stu: String) = DbAction { implicit request =>
    implicit val pm = request.pm
    if (request.method == "GET") {
      val dName = Student.getByStateId(stu) match {
        case None => "Unknown"
        case Some(s) => s.displayName
      }
      val copies = request.visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]())
      val ct = copies.map(c => (c, Copy.getByBarcode(c).get.purchaseGroup.title.isbn))
      val zipped = ct.zipWithIndex
      Ok(html.books.checkoutBulkHelper(Binding(CheckoutBulkHelperForm), dName, zipped))
    } else {
      val copies = request.visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]())
      val ct = copies.map(c => (c, Copy.getByBarcode(c).get.purchaseGroup.title.isbn))
      val zipped = ct.zipWithIndex
      Binding(CheckoutBulkHelperForm, request) match {
        case ib: InvalidBinding => Ok(html.books.checkoutBulkHelper(ib, stu, zipped))
        case vb: ValidBinding => {
          Copy.getByBarcode(vb.valueOf(CheckoutBulkHelperForm.barcode)) match {
            case None => Redirect(routes.Books.checkoutBulkHelper(stu)).flashing("message" -> "Copy not found.")
            case Some(cpy) => {
              if (cpy.isCheckedOut) {
                Redirect(routes.Books.checkoutBulkHelper(stu)).flashing("message" -> "Copy already checked out.")
              } else {
                request.visit.set("checkoutList", Vector[String](cpy.getBarcode()) ++ request.visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]()))
                Redirect(routes.Books.checkoutBulkHelper(stu))
              }
            }
          }
        }
      }
    }
  }

  object CheckInForm extends Form {
    val barcode = new TextField("Barcode") {
      override val minLength = Some(21)
      override val maxLength = Some(23)
    }

    val fields = List(barcode)
  }

  def checkIn = DbAction { implicit request =>
    if (request.method == "GET") Ok(views.html.books.checkIn(Binding(CheckInForm)))
      else {
      implicit val pm = request.pm
      Binding(CheckInForm, request) match {
        case ib: InvalidBinding => Ok(views.html.books.checkIn(ib))
        case vb: ValidBinding => {
          val cand = QCheckout.candidate
          Copy.getByBarcode(vb.valueOf(CheckInForm.barcode)) match {
            case None => Redirect(routes.Books.checkIn()).flashing("message" -> "No copy with the given barcode")
            case Some(cpy) => {
              pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.copy.eq(cpy))).executeOption() match {
                case None => Redirect(routes.Books.checkIn()).flashing("message" -> "Copy not checked out")
                case Some(currentCheckout) => {
                  currentCheckout.endDate = new java.sql.Date(new java.util.Date().getTime())
                  request.pm.makePersistent(currentCheckout)
                  Redirect(routes.Books.checkIn()).flashing("message" -> "Copy successfully checked in.")
                }
              }
          }
        }
        }
      }
    }
  }
  
  def lookup() = TODO
  
  def inspect() = TODO
  
  def findCopyHistory() = DbAction { implicit req =>
    object ChooseCopyForm extends Form {
      val barcode = new TextField("Barcode") {
        override val minLength = Some(21)
        override val maxLength = Some(23)
        override def validators = super.validators ++ List(Validator((str: String) => Copy.getByBarcode(str) match {
          case None => ValidationError("Copy not found.")
          case Some(barcode) => ValidationError(Nil)
        }))
      }

      def fields = List(barcode)
    }
    if (req.method == "GET") {
      Ok(html.books.findCopyHistory(Binding(ChooseCopyForm)))
    } else {
      Binding(ChooseCopyForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findCopyHistory(ib))
        case vb: ValidBinding => {
          val lookupCopyBarcode: String = vb.valueOf(ChooseCopyForm.barcode)
          Redirect(routes.Books.copyHistory(lookupCopyBarcode))
        }
      }
    }
  }
  
  def copyHistory(barcode: String) = DbAction { implicit req =>
    implicit val pm = req.pm
    val df = new java.text.SimpleDateFormat("MM/dd/yyyy")
    Copy.getByBarcode(barcode) match {
      case None => NotFound("No copy with the given barcode.")
      case Some(copy) => {
        val header = "Copy #%d of %s".format(copy.number, copy.purchaseGroup.title.name)
        val coCand = QCheckout.candidate
        val rows: List[(String, String, String)] = pm.query[Checkout].filter(coCand.copy.eq(copy)).executeList().map(co => {
          (co.student.formalName, df.format(co.startDate), if (co.endDate == null) "" else df.format(co.endDate))
        })
        Ok(views.html.books.copyHistory(header, rows))
      }
    }
  }
  
  def confirmCopyLost(copyId: Long) = TODO
  
  def checkInLostCopy() = TODO
  
  def delete(id: Long) = TODO
  
  def confirmDelete() = TODO
  
  def checkoutHistory(stateId: String) = DbAction { implicit req =>
  implicit val pm = req.pm
  val df = new java.text.SimpleDateFormat("MM/dd/yyyy")

  Student.getByStateId(stateId) match {
    case None => NotFound("Student not found.")
    case Some(currentStudent) => {
      val checkoutCand = QCheckout.candidate
      val currentBooks = pm.query[Checkout].filter(checkoutCand.student.eq(currentStudent)).executeList()
      val studentName = currentStudent.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String, String)] = currentBooks.map(co => { (co.copy.purchaseGroup.title.name, df.format(co.startDate),
        if (co.endDate == null) "" else df.format(co.endDate))})
      Ok(views.html.books.checkoutHistory(header,rows))
    }
  }
}

  def findCheckoutHistory() = DbAction { implicit req =>
    implicit val pm = req.pm
    object ChooseStudentForm extends Form {
      val student = new TextField("Student") {
          override def validators = super.validators ++ List(Validator((str: String) => Student.getByStateId(str) match {
            case None => ValidationError("Student not found.")
            case Some(student) => ValidationError(Nil)
          }))
      }

    def fields = List(student)
  }
  if (req.method == "GET") {
      Ok(html.books.findCheckoutHistory(Binding(ChooseStudentForm)))
    } else {
      Binding(ChooseStudentForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findCheckoutHistory(ib))
        case vb: ValidBinding => {
          val lookupStudentId: String = vb.valueOf(ChooseStudentForm.student)
          Redirect(routes.Books.checkoutHistory(lookupStudentId))
        }
      }
    }
  }

  def findCurrentCheckouts() = DbAction { implicit req =>
    implicit val pm = req.pm
    object ChooseStudentForm extends Form {
      val stateId = new TextField("Student") {
        override def validators = super.validators ++ List(Validator((str: String) => Student.getByStateId(str) match {
          case None => ValidationError("Student not found.")
          case Some(student) => ValidationError(Nil)
        }))
      }

      def fields = List(stateId)
    }
    if (req.method == "GET") {
      Ok(html.books.findPerspectiveHistory(Binding(ChooseStudentForm)))
    } else {
      Binding(ChooseStudentForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findPerspectiveHistory(ib))
        case vb: ValidBinding => {
          val lookupStudentId: String = vb.valueOf(ChooseStudentForm.stateId)
          Redirect(routes.Books.currentCheckouts(lookupStudentId))
        }
      }
    }
  }

  def currentCheckouts(stateId: String) = DbAction { implicit req =>
  implicit val pm = req.pm
  val df = new java.text.SimpleDateFormat("MM/dd/yyyy")

  Student.getByStateId(stateId) match {
    case None => NotFound("Student not found.")
    case Some(currentStudent) => {
      val checkoutCand = QCheckout.candidate
      val currentBooks = pm.query[Checkout].filter(checkoutCand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(checkoutCand.student.eq(currentStudent))).executeList()
      val studentName = currentStudent.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String)] = currentBooks.map(co => { (co.copy.purchaseGroup.title.name, df.format(co.startDate))})
      Ok(views.html.books.currentCheckouts(header,rows))
    }
  }
}

  def checkoutsByTeacherStudents() = TODO
  
  def statistics() = TODO
  
  def copyStatusByTitle(isbn: String) = DbAction { implicit req =>
    // TODO - figure out how this should handle lost copies
    implicit val pm = req.pm

    Title.getByIsbn(isbn) match {
      case None => NotFound("Title not found.")
      case Some(t) => {
        val cand = QCopy.candidate
        val pCand = QPurchaseGroup.variable("pCand")
        val currentCopies = pm.query[Copy].filter(cand.purchaseGroup.eq(pCand).and(pCand.title.eq(t))).executeList().sortWith((c1, c2) => c1.number < c2.number)

        val header = "Copy Status for " + t.name
        val rows: List[(String, String)] = currentCopies.map(cp => { (cp.number.toString, cp.isCheckedOut.toString)})
        Ok(views.html.books.copyStatusByTitle(header, rows))
      }
    }
  }
  
  def findCopyStatusByTitle() = DbAction { implicit req =>
    object ChooseTitleForm extends Form {
      val isbn = new TextField("ISBN") {
        override val minLength = Some(10)
        override val maxLength = Some(13)
        override def validators = super.validators ++ List(Validator((str: String) => Title.getByIsbn(str) match {
          case None => ValidationError("Title not found.")
          case Some(isbn) => ValidationError(Nil)
        }))
      }

      def fields = List(isbn)
    }
    if (req.method == "GET") {
      Ok(html.books.findCopyStatusByTitle(Binding(ChooseTitleForm)))
    } else {
      Binding(ChooseTitleForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findCopyStatusByTitle(ib))
        case vb: ValidBinding => {
          val lookupTitleIsbn: String = vb.valueOf(ChooseTitleForm.isbn)
          Redirect(routes.Books.copyStatusByTitle(lookupTitleIsbn))
        }
      }
    }
  }

  def allBooksOut(grade: Int) = DbAction { implicit req =>
    implicit val pm = req.pm
    val df = new java.text.SimpleDateFormat("MM/dd/yyyy")
    val stu = QStudent.variable("stu")
    val cand = QCheckout.candidate
    val currentBooksOut = pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.student.eq(stu)).and(stu.grade.eq(grade))).executeList()
    val header = "Current books out for grade " + grade
    val rows: List[(String, String, String)] = currentBooksOut.map(co => { (co.copy.purchaseGroup.title.name, df.format(co.startDate), co.student.formalName)})
    Ok(views.html.books.allBooksOut(header, rows))
  }

  def findAllBooksOut() = DbAction { implicit req =>
    object ChooseGradeForm extends Form {
      val grade = new ChoiceField[Int]("Grade", List("Freshman" -> 9, "Sophomore" -> 10, "Junior" -> 11, "Senior" -> 12))

      def fields = List(grade)
    }
    if (req.method == "GET") {
      Ok(html.books.findAllBooksOut(Binding(ChooseGradeForm)))
    } else {
      Binding(ChooseGradeForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findAllBooksOut(ib))
        case vb: ValidBinding => {
          val lookupGrade: Int = vb.valueOf(ChooseGradeForm.grade)
          Redirect(routes.Books.allBooksOut(lookupGrade))
        }
      }
    }
  }

  def copyInfo(barcode: String) = DbAction { implicit req =>
    implicit val pm = req.pm

    Copy.getByBarcode(barcode) match {
      case None => NotFound("Copy not found.")
      case Some(cpy) => {
        val df = new java.text.SimpleDateFormat("MM/dd/yyyy")

        val lost = cpy.isLost
        val num = cpy.number

        val pGroup = cpy.purchaseGroup
        val pDate = pGroup.purchaseDate
        val price = pGroup.price

        val title = pGroup.title
        val name = title.name
        val author = title.author
        val publisher = title.publisher
        val isbn = title.isbn
        val pages = title.numPages
        val dim = title.dimensions
        val weight = title.weight

        val checkedOut = cpy.isCheckedOut

        val rows: List[(String, String)] = List(("Name:", name), ("Author:", author.getOrElse("Unknown")), ("Publisher:", publisher.getOrElse("Unknown")), ("ISBN:", isbn), ("Pages:", pages.getOrElse("Unknown").toString), 
          ("Dimensions (in):", dim.getOrElse("Unknown")), ("Weight (lbs):", weight.getOrElse("Unknown").toString), ("Purchase Date:", df.format(pDate)), ("Price:", price.toString), ("Lost:", lost.toString), 
          ("Copy Number:", num.toString), ("Checked Out:", checkedOut.toString))
        val header = "Copy info for " + barcode

        Ok(views.html.books.copyInfo(header, rows))
      }
    }
  }

  def findCopyInfo() = DbAction { implicit req =>
    object ChooseCopyForm extends Form {
      val barcode = new TextField("Barcode") {
        override val minLength = Some(21)
        override val maxLength = Some(23)
        override def validators = super.validators ++ List(Validator((str: String) => Copy.getByBarcode(str) match {
          case None => ValidationError("Copy not found.")
          case Some(barcode) => ValidationError(Nil)
        }))
      }

      def fields = List(barcode)
    }
    if (req.method == "GET") {
      Ok(html.books.findCopyInfo(Binding(ChooseCopyForm)))
    } else {
      Binding(ChooseCopyForm, req) match {
        case ib: InvalidBinding => Ok(html.books.findCopyInfo(ib))
        case vb: ValidBinding => {
          val lookupBarcode: String = vb.valueOf(ChooseCopyForm.barcode)
          Redirect(routes.Books.copyInfo(lookupBarcode))
        }
      }
    }
  }

  def inventory() = DbAction { implicit req =>
    implicit val pm = req.pm

    val titles = pm.query[Title].executeList.sortWith((c1, c2) => c1.name < c2.name)

    val rows: List[(String, String, String, String)] = titles.map(ti => { (ti.name, ti.howManyCopies().toString, ti.howManyCheckedOut().toString, (ti.howManyCopies() - ti.howManyCheckedOut()).toString)})
    Ok(views.html.books.inventory(rows))
  }

}
