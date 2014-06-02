package controllers

import java.io.{ File, FileInputStream, FileOutputStream }
import javax.imageio.ImageIO
import play.api.mvc.Controller
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import com.itextpdf.text.pdf.{ Barcode128, Barcode, PdfContentByte, PdfWriter, BaseFont }
import com.itextpdf.text.{ BaseColor, Document, DocumentException, PageSize, Paragraph, Utilities }
import models.books._
import models.courses.{ Student, QStudent }
import models.courses.Student.{ StudentField, StudentIdField }
import models.users._
import org.dupontmanual.forms.{ Binding, InvalidBinding, ValidBinding, Call, Method, Form }
import org.dupontmanual.forms.fields._
import org.dupontmanual.forms.widgets._
import org.dupontmanual.forms.validators._
import controllers.users.{ VisitAction, VisitRequest }
import config.Config
import com.google.inject.{ Inject, Singleton }
import org.joda.time.LocalDateTime
import org.joda.time.LocalDate
import models.courses._
import config.users.UsesDataStore
import controllers.users.{ Authenticated, PermissionRequired }
import models.books.Book.Permissions
import play.api.libs.Files.TemporaryFile
import java.util.UUID

@Singleton
class Books @Inject()(implicit config: Config) extends Controller with UsesDataStore {
  import Books._
  /**
   * Regex: /books/addTitle
   *
   * A form that allows users to add information for a new book to the database.
   */
  def addTitle() = PermissionRequired(Permissions.Manage) { implicit request =>
    Ok(templates.books.addTitle(Binding(TitleForm)))
  }
  
  def addTitleP() = PermissionRequired(Permissions.Manage) { implicit request =>
    Binding(TitleForm, request) match {
      case ib: InvalidBinding => Ok(templates.books.addTitle(ib))
      case vb: ValidBinding => dataStore.execute { implicit pm =>
        val t = new Title(vb.valueOf(TitleForm.name), vb.valueOf(TitleForm.author),
          vb.valueOf(TitleForm.publisher), vb.valueOf(TitleForm.isbn), vb.valueOf(TitleForm.numPages),
          vb.valueOf(TitleForm.dimensions), vb.valueOf(TitleForm.weight), true,
          Some(LocalDateTime.now()), Some("public/images/books/" + vb.valueOf(TitleForm.isbn) + ".jpg"))
        pm.makePersistent(t)

        vb.valueOf(TitleForm.imageUrl) match {
          case Some(url) => try {
            downloadImage(url, vb.valueOf(TitleForm.isbn))
            Redirect(routes.Books.addTitle()).flashing("message" -> "Title added successfully")
          } catch {
            case e: Exception => Redirect(routes.Books.addTitle()).flashing("warn" -> "Image not downloaded. Update the title's image to try downloading again")
          }
          case None => Redirect(routes.Books.addTitle()).flashing("message" -> "Title added without an image")
        }
      }
    }
  }

  /**
   * Regex: /books/addPurchaseGroup
   *
   * A form that allows users to add a purchase of a certain title, and update
   * information about the number of copies of the book.
   */
  def addPurchaseGroup() = PermissionRequired(Permissions.Manage) { implicit request =>
    Ok(templates.books.addPurchaseGroup(Binding(AddPurchaseGroupForm)))
  }

  def addPurchaseGroupP() = PermissionRequired(Permissions.Manage) { implicit request =>
    dataStore.execute { pm =>
      Binding(AddPurchaseGroupForm, request) match {
        case ib: InvalidBinding => Ok(templates.books.addPurchaseGroup(ib))
        case vb: ValidBinding => {
          val t = vb.valueOf(AddPurchaseGroupForm.title)
          val p = new PurchaseGroup(t, vb.valueOf(AddPurchaseGroupForm.purchaseDate), vb.valueOf(AddPurchaseGroupForm.price))
          pm.makePersistent(p)

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
              pm.makePersistent(cpy)
            } else {
              val cpy = new Copy(purchaseGroup, copyNumber, false)
              pm.makePersistent(cpy)
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

  /**
   * Regex: /books/checkout
   *
   * A form page that allows administrators to checkout a copy of a book to a student.
   */
  def checkout = PermissionRequired(Permissions.Manage) { implicit request =>
    Ok(templates.books.checkout(Binding(CheckoutForm)))
  }

  def checkoutP() = PermissionRequired(Permissions.Manage) { implicit request =>
    Binding(CheckoutForm, request) match {
      case ib: InvalidBinding => Ok(templates.books.checkout(ib))
      case vb: ValidBinding => dataStore.execute { implicit pm =>
        val stu = vb.valueOf(CheckoutForm.student)
        val cpy = vb.valueOf(CheckoutForm.cpy)
        val cand = QCheckout.candidate
        pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.copy.eq(cpy))).executeOption() match {
          case Some(currentCheckout) => {
            currentCheckout.endDate = Some(LocalDate.now())
            pm.makePersistent(currentCheckout)
            val c = new Checkout(stu, cpy, Some(LocalDate.now()), None)
            pm.makePersistent(c)
            Redirect(routes.Books.checkout()).flashing("message" -> "Copy successfully checked out")
          }
          case None => {
            val c = new Checkout(stu, cpy, Some(LocalDate.now()), None)
            pm.makePersistent(c)
            Redirect(routes.Books.checkout()).flashing("message" -> "Copy successfully checked out")
          }
        }
      }
    }
  }

  /**
   * Regex: /books/checkoutBulk
   *
   * Another form that allows books to be checked out in bulk to a single student
   * Redirects to another page.
   */
  def checkoutBulk() = PermissionRequired(Permissions.Manage) { implicit request =>
    Ok(templates.books.checkoutBulk(Binding(CheckoutBulkForm)))
  }

  def checkoutBulkP() = PermissionRequired(Permissions.Manage) { implicit request =>
    Binding(CheckoutBulkForm, request) match {
      case ib: InvalidBinding => Ok(templates.books.checkoutBulk(ib))
      case vb: ValidBinding => {
        val checkoutStu: String = vb.valueOf(CheckoutBulkForm.student)
        Student.getByStateId(checkoutStu) match {
          case None => Redirect(routes.Books.checkoutBulk).flashing("error" -> "Student not found.")
          case Some(s) => Redirect(routes.Books.checkoutBulkHelper(checkoutStu))
        }
      }
    }
  }

  /**
   * Regex: /books/checkoutBulkHelper/:stu
   *
   * A form that helps /books/checkoutBulk.
   * A form that checkouts multiple copies that are parameters of the request
   * to a student with given id.
   */
  def checkoutBulkHelper(stu: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    val dName = Student.getByStateId(stu) match {
      case None => "Unknown"
      case Some(s) => s.displayName
    }
    val visit = request.visit
    val copies = visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]())
    val ct = copies.map(c => (c, Copy.getByBarcode(c).get.purchaseGroup.title.isbn))
    val zipped = ct.zipWithIndex
    Ok(templates.books.checkoutBulkHelper(Binding(CheckoutBulkHelperForm), dName, zipped, stu))
  }

  def checkoutBulkHelperP(stu: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    val dName = Student.getByStateId(stu) match {
      case None => "Unknown"
      case Some(s) => s.displayName
    }
    val visit = request.visit
    val copies = visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]())
    val ct = copies.map(c => (c, Copy.getByBarcode(c).get.purchaseGroup.title.isbn))
    val zipped = ct.zipWithIndex
    Binding(CheckoutBulkHelperForm, request) match {
      case ib: InvalidBinding => Ok(templates.books.checkoutBulkHelper(ib, dName, zipped, stu))
      case vb: ValidBinding => {
        val cpy = vb.valueOf(CheckoutBulkHelperForm.copy)
        if (visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]()).exists(c => c == cpy.getBarcode)) {
          Redirect(routes.Books.checkoutBulkHelper(stu)).flashing("error" -> "Copy already in queue.")
        } else {
          visit.set("checkoutList", Vector[String](cpy.getBarcode()) ++ visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]()))
          Redirect(routes.Books.checkoutBulkHelper(stu))
        }
      }
    }
  }

  /**
   * Regex: /books/removeCopyFromList/:stu/:bc
   *
   * Removes a copy with given barcode (bc) from the student's with given id (stu)
   * checkout list. Redirects back to the checkout helper.
   */
  def removeCopyFromList(stu: String, barcode: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    val copies = request.visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]())
    val newCopies = copies.filter(_ != barcode)
    request.visit.set("checkoutList", newCopies)
    Redirect(routes.Books.checkoutBulkHelper(stu))
  }

  /**
   * Regex: /books/removeCopyFromList/:stu/:bc
   *
   * Removes all copies from the student's with given id (stu)
   * checkout list. Redirects back to the checkout helper.
   */
  def removeAllCopiesFromList(stu: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    request.visit.set("checkoutList", Vector[String]())
    Redirect(routes.Books.checkoutBulkHelper(stu))
  }

  /**
   * Regex: /books/cancelBulkCheckout
   *
   * Sets the parameter of the request to empty and redirects
   * to the initial bulk checkout form.
   */
  def cancelBulkCheckout() = PermissionRequired(Permissions.Manage) { implicit request =>
    request.visit.set("checkoutList", Vector[String]())
    Redirect(routes.Books.checkoutBulk())
  }

  /**
   * Regex: /books/checkoutBulkSubmit/:stu
   *
   * Checks out all the books in the checkoutList request parameter to
   * the student with given id (stu)
   */
  def checkoutBulkSubmit(stu: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    val copies: Vector[String] = request.visit.getAs[Vector[String]]("checkoutList").getOrElse(Vector[String]())

    dataStore.execute { implicit pm =>
      for (cpy <- copies) {
        val cand = QCheckout.candidate
        pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.copy.eq(Copy.getByBarcode(cpy).get))).executeOption() match {
          case Some(currentCheckout) => {
            currentCheckout.endDate = Some(LocalDate.now())
            pm.makePersistent(currentCheckout)
            val c = new Checkout(Student.getByStateId(stu).get, Copy.getByBarcode(cpy).get, Some(LocalDate.now()), None)
            pm.makePersistent(c)
          }
          case None => {
            val c = new Checkout(Student.getByStateId(stu).get, Copy.getByBarcode(cpy).get, Some(LocalDate.now()), None)
            pm.makePersistent(c)
          }
        }
      }

      val mes = copies.length + " copie(s) successfully checked out to " + Student.getByStateId(stu).get.displayName
      request.visit.set("checkoutList", Vector[String]())
      Redirect(routes.Books.checkoutBulk()).flashing("message" -> mes)
    }
  }

  /**
   * Regex: /books/checkIn
   *
   * A form that allows for a book to be checked back in.
   */
  def checkIn() = PermissionRequired(Permissions.Manage) { implicit request =>
    Ok(templates.books.checkIn(Binding(CheckInForm)))
  }

  def checkInP() = PermissionRequired(Permissions.Manage) { implicit request =>
    Binding(CheckInForm, request) match {
      case ib: InvalidBinding => Ok(templates.books.checkIn(ib))
      case vb: ValidBinding => dataStore.execute { pm =>
        val cand = QCheckout.candidate
        val cpy = vb.valueOf(CheckInForm.copy)
        pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.copy.eq(cpy))).executeOption() match {
          case None => Redirect(routes.Books.checkIn()).flashing("error" -> "Copy not checked out")
          case Some(currentCheckout) => {
            currentCheckout.endDate = Some(LocalDate.now())
            val copyNum = currentCheckout.copy.number
            val title = currentCheckout.copy.purchaseGroup.title
            val student = currentCheckout.student
            pm.makePersistent(currentCheckout)
            Redirect(routes.Books.checkIn()).flashing("message" -> s"${student.displayName} returned copy #${copyNum} of ${title.name}.")
          }
        }
      }
    }
  }

  /**
   * Regex: /books/checkInBulk
   *
   * A form that allows multiple books to be checked in simultaneously
   */
  def checkInBulk() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.checkInBulk(Binding(BulkCheckInForm)))
  }

  def checkInBulkP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(BulkCheckInForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.checkInBulk(ib))
      case vb: ValidBinding => dataStore.execute { pm =>
        val bcs = vb.valueOf(BulkCheckInForm.barcodes).split("\\r?\\n").toList.distinct
        val cps = checkInBulkPHelper(bcs, "", List[Copy]())
        val chIns = checkInList(cps._1, 0, "", pm)
        val copiesNotFound = if (cps._2.isEmpty) "" else "could not find copies with barcodes:" + cps._2.substring(1)
        val booksNotCheckedOut = if (chIns._2.isEmpty) "" else "books with the following barcodes were already checked in:" + chIns._2.substring(1)
        val errors = if (!copiesNotFound.isEmpty && !booksNotCheckedOut.isEmpty) copiesNotFound + " and " + booksNotCheckedOut else copiesNotFound + booksNotCheckedOut
        val successes = chIns._1
        if (chIns._2.isEmpty) {
          val mes = successes + " copie(s) successfully checked in"
          Redirect(routes.Books.checkInBulk()).flashing("message" -> mes)
        } else {
          val warning = successes + " copie(s) successfully checked in and " + errors
          Redirect(routes.Books.checkInBulk()).flashing("warn" -> warning)
        }
      }
    }
  }

  def checkInBulkPHelper(bcs: List[String], errs: String, cps: List[Copy]): (List[Copy], String) = {
    if (bcs.size == 0) {
        (cps, errs)
    } else if (bcs.head.isEmpty) {
      checkInBulkPHelper(bcs.tail, errs, cps)
    } else {
      Copy.getByBarcode(bcs.head) match {
        case None => checkInBulkPHelper(bcs.tail, errs + ", " + bcs.head, cps)
        case Some(c) => checkInBulkPHelper(bcs.tail, errs, cps :+ c)
      }
    }
  }

  def checkInList(cps: List[Copy], counter: Int, errs: String, pm: scalajdo.ScalaPersistenceManager): (Int, String) = {
    if (cps.size == 0) {
      (counter, errs)
    } else {
      val cand = QCheckout.candidate
      pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.copy.eq(cps.head))).executeOption() match {
        case None => checkInList(cps.tail, counter, errs + ", " + cps.head.getBarcode, pm)
        case Some(c) => {
          c.endDate = Some(LocalDate.now())
          pm.makePersistent(c)
          checkInList(cps.tail, counter + 1, errs, pm)
        }
      }
    }
  }

  /**
   * Regex: /books/findCopyHistory
   *
   * A form that allows the user to find the information on a copy with a given barcode.
   * The post request redirects to /books/copyHistory/:barcode
   */
  def findCopyHistory() = PermissionRequired(Permissions.LookUp) { implicit req =>
    Ok(templates.books.findCopyHistory(Binding(ChooseCopyForm)))
  }

  def findCopyHistoryP() = PermissionRequired(Permissions.LookUp) { implicit req =>
    Binding(ChooseCopyForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.findCopyHistory(ib))
      case vb: ValidBinding => singleCopyHistory(vb.valueOf(ChooseCopyForm.copy))
    }
  }

  /**
   * Regex: /books/copyHistory/:barcode
   *
   * A page that displays information about the history of the copy
   * with the given barcode.
   */
  def copyHistory(barcode: String) = PermissionRequired(Permissions.LookUp) { implicit req =>
    Copy.getByBarcode(barcode) match {
      case None => NotFound("No copy with the given barcode.")
      case Some(copy) => singleCopyHistory(copy)
    }
  }

  def singleCopyHistory(copy: Copy)(implicit req: VisitRequest[_]) = {
    dataStore.execute { pm =>
      val header = "Copy #%d of %s".format(copy.number, copy.purchaseGroup.title.name)
      val coCand = QCheckout.candidate
      val rows: List[(String, String, String)] = pm.query[Checkout].filter(coCand.copy.eq(copy)).executeList().map(co => {
        (co.student.formalName, co.startDate.map(df.print(_)).getOrElse(""), co.endDate.map(df.print(_)).getOrElse(""))
      })
      Ok(templates.books.copyHistory(header, rows))
    }
  }

  /**
   * Regex: /books/checkoutHistory/:studentId
   *
   * Displays information about the books checked out to a student
   * with the given id (studentId).
   */
  def checkoutHistory(stateId: String) = PermissionRequired(Permissions.LookUp) { implicit req =>
    Student.getByStateId(stateId) match {
      case None => NotFound("No student with the given id.")
      case Some(currentStudent) => allStudentCheckouts(currentStudent)
    }
  }

  def allStudentCheckouts(student: Student)(implicit req: VisitRequest[_]) = {
    dataStore.execute { pm =>
      val checkoutCand = QCheckout.candidate
      val currentBooks = pm.query[Checkout].filter(checkoutCand.student.eq(student)).executeList()
      val studentName = student.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String, String, String)] = currentBooks.map(co => {
        (co.copy.purchaseGroup.title.name, co.copy.number.toString, co.startDate.map(df.print(_)).getOrElse(""),
          co.endDate.map(df.print(_)).getOrElse(""))
      })
      Ok(templates.books.checkoutHistory(header, rows))
    }
  }

  /**
   * Regex: /books/findCheckoutHistory
   *
   * A form page that allows the user to find the checkout history
   * for a desired student.
   */
  def findCheckoutHistory() = PermissionRequired(Permissions.LookUp) { implicit req =>
    Ok(templates.books.findCheckoutHistory(Binding(ChooseStudentForm)))
  }

  def findCheckoutHistoryP() = PermissionRequired(Permissions.LookUp) { implicit req =>
    Binding(ChooseStudentForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.findCheckoutHistory(ib))
      case vb: ValidBinding => dataStore.execute { implicit pm =>
        allStudentCheckouts(vb.valueOf(ChooseStudentForm.student))
      }
    }
  }

  /**
   * Regex: /books/findCurrentCheckouts
   *
   * A form page that allows users to find books currently
   * checked out to a student they provide.
   */
  def findCurrentCheckouts() = PermissionRequired(Permissions.LookUp) { implicit req =>
    Ok(templates.books.findRoleHistory(Binding(ChooseStudentForm)))
  }

  def findCurrentCheckoutsP() = PermissionRequired(Permissions.LookUp) { implicit req =>
    Binding(ChooseStudentForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.findRoleHistory(ib))
      case vb: ValidBinding => {
        checkoutsForStudent(vb.valueOf(ChooseStudentForm.student))
      }
    }
  }

  /**
   * Regex: /books/currentCheckouts/:studentId
   *
   * Displays information about the books currently check out
   * to a student with the given id (studentId).
   */
  def currentCheckouts(stateId: String) = PermissionRequired(Permissions.LookUp) { implicit req =>
    Student.getByStateId(stateId) match {
      case None => NotFound("No student with the given id")
      case Some(currentStudent) => checkoutsForStudent(currentStudent)
    }
  }

  def checkoutsForStudent(student: Student)(implicit req: VisitRequest[_]) = {
    dataStore.execute { pm => 
      req.visit.redirectUrl = routes.Books.currentCheckouts(student.stateId)
      pm.makePersistent(req.visit)
      val checkoutCand = QCheckout.candidate
      val currentBooks = pm.query[Checkout].filter(checkoutCand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(checkoutCand.student.eq(student))).executeList()
      val studentName = student.displayName
      val header = "Student: %s".format(studentName)
      val rows: List[(String, String, String, String)] = currentBooks.map(co => { (co.copy.purchaseGroup.title.name, co.copy.number.toString, co.startDate.map(df.print(_)).getOrElse(""), co.copy.getBarcode) })
      Ok(templates.books.currentCheckouts(header, rows))
    }
  }

  /**
   * Regex: /books/copyStatusByTitle/:isbn
   *
   * Displays information about the status of a copy with given isbn.
   */
  def copyStatusByTitle(isbn: String) = PermissionRequired(Permissions.Manage) { implicit req =>
    Title.getByIsbn(isbn) match {
      case None => NotFound("Title not found.")
      case Some(t) => singleTitleStatus(t)
    }
  }

  def singleTitleStatus(t: Title)(implicit req: VisitRequest[_]) = {
    dataStore.execute { pm =>
      val cand = QCopy.candidate
      val pCand = QPurchaseGroup.variable("pCand")
      val currentCopies = dataStore.pm.query[Copy].filter(cand.purchaseGroup.eq(pCand).and(pCand.title.eq(t))).executeList().sortWith((c1, c2) => c1.number < c2.number)

      val header = "Copy Status for " + t.name
      val rows: List[(String, String, String, String)] = currentCopies.map(cp => { (cp.number.toString, cp.isCheckedOut.toString, cp.isLost.toString, cp.deleted.toString) })
      Ok(templates.books.copyStatusByTitle(header, rows))
    }
  }

  /**
   * Regex: /books/findCopyStatusByTitle
   *
   * A form page that allows a user to find a copy by its isbn.
   */
  def findCopyStatusByTitle() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.findCopyStatusByTitle(Binding(ChooseTitleForm)))
  }

  def findCopyStatusByTitleP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(ChooseTitleForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.findCopyStatusByTitle(ib))
      case vb: ValidBinding => singleTitleStatus(vb.valueOf(ChooseTitleForm.title))
    }
  }

  /**
   * Regex: /books/allBooksOut/:grade
   *
   * Displays all of the books checked out for a given grade.
   */
  def allBooksOut(grade: Int) = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { implicit pm =>
      val stu = QStudent.variable("stu")
      val cand = QCheckout.candidate
      val currentBooksOut = pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.student.eq(stu)).and(stu.grade.eq(grade))).executeList()
      val booksOut = currentBooksOut.filter(_.student.user.isActive).sortBy(_.student.user.formalName)
      val header = "Current books out for grade " + grade
      val rows: List[(String, String, String, String)] = booksOut.map(co => { (co.copy.purchaseGroup.title.name, co.copy.number.toString, co.startDate.map(df.print(_)).getOrElse(""), co.student.formalName) })
      Ok(templates.books.allBooksOut(header, rows))
    }
  }

  /**
   * Regex: /books/findAllBooksOut
   *
   * A form page that allows a user to find al the books out for a page.
   */
  def findAllBooksOut() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.findAllBooksOut(Binding(ChooseGradeForm)))
  }

  def findAllBooksOutP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(ChooseGradeForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.findAllBooksOut(ib))
      case vb: ValidBinding => dataStore.execute { implicit pm =>
        val lookupGrade: Int = vb.valueOf(ChooseGradeForm.grade)
        Redirect(routes.Books.allBooksOut(lookupGrade))
      }
    }
  }

  /**
   * Regex: /books/copyInfo/:barcode
   *
   * Displays information on a copy with given barcode.
   */
  def copyInfo(barcode: String) = PermissionRequired(Permissions.Manage) { implicit req =>
    Copy.getByBarcode(barcode) match {
      case None => NotFound("Copy not found.")
      case Some(cpy) => singleCopyInfo(cpy)
    }
  }

  def singleCopyInfo(cpy: Copy)(implicit req: VisitRequest[_]) = {
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
    ("Dimensions (in):", dim.getOrElse("Unknown")), ("Weight (lbs):", weight.getOrElse("Unknown").toString), ("Purchase Date:", df.print(pDate)), ("Price:", price.toString), ("Lost:", lost.toString),
    ("Copy Number:", num.toString), ("Checked Out:", checkedOut.toString))
    val header = "Copy info for " + cpy.getBarcode

    Ok(templates.books.copyInfo(header, rows, isbn))
  }

  /**
   * Regex: /books/findCopyInfo
   *
   * A form page that allows users to find info on a copy with a certain barcode.
   */
  def findCopyInfo() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.findCopyInfo(Binding(ChooseCopyForm)))
  }

  def findCopyInfoP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(ChooseCopyForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.findCopyInfo(ib))
      case vb: ValidBinding => singleCopyInfo(vb.valueOf(ChooseCopyForm.copy))
    }
  }

  /**
   * Regex: /books/inventory
   *
   * Displays all of the titles in stock as well as certain information about each title.
   */
  def inventory() = PermissionRequired(Permissions.Manage) { implicit req =>
    val titles = dataStore.pm.query[Title].executeList.sortWith((c1, c2) => c1.name < c2.name)

    val rows: List[(String, String, String, String, String, String)] = titles.map(ti => {
      (ti.name, ti.isbn, (ti.howManyCopies() - ti.howManyDeleted()).toString,
        ti.howManyCheckedOut().toString, ti.howManyLost().toString, (ti.howManyCopies() - (ti.howManyCheckedOut() + ti.howManyDeleted() + ti.howManyLost())).toString)
    })
    Ok(templates.books.inventory(rows))
  }

  /**
   * Regex: /books/editTitleHelper/:isbn
   *
   * A form that allows the user to alter information about a title with a certain isbn.
   */
  def editTitleHelper(isbn: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    Title.getByIsbn(isbn) match {
      case Some(title) => Ok(templates.books.editTitleHelper(Binding(new EditTitleForm(title.name, title.author, title.publisher, title.numPages, title.dimensions, title.weight))))
      case None => Redirect(routes.Books.editTitle()).flashing("error" -> "Title not found")
    }
  }

  def editTitleHelperP(isbn: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    Title.getByIsbn(isbn) match {
      case None => Redirect(routes.Books.editTitle()).flashing("error" -> "Title not found")
      case Some(title) => {
        val f = new EditTitleForm(title.name, title.author, title.publisher, title.numPages, title.dimensions, title.weight)
        Binding(f, request) match {
          case ib: InvalidBinding => Ok(templates.books.editTitleHelper(ib))
          case vb: ValidBinding => {
            title.name = vb.valueOf(f.name)
            title.author = vb.valueOf(f.author)
            title.publisher = vb.valueOf(f.publisher)
            title.numPages = vb.valueOf(f.numPages)
            title.dimensions = vb.valueOf(f.dimensions)
            title.weight = vb.valueOf(f.weight)
            title.lastModified = LocalDateTime.now
            dataStore.pm.makePersistent(title)

            vb.valueOf(f.imageUrl) match {
              case Some(url) => try {
                downloadImage(url, isbn)
                Redirect(routes.App.index()).flashing("message" -> "Title updated successfully")
              } catch {
                case e: Exception => Redirect(routes.App.index()).flashing("error" -> "Image not downloaded. Edit the tite to try downloading again")
              }
              case None => Redirect(routes.App.index()).flashing("message" -> "Title updated successfully")
            }
          }
        }
      }
    }
  }

  /**
   * Regex: /books/editTitle
   *
   * A form that redirects a user to /books/editTitleHelper/:isbn based on the isbn they enter here.
   */
  def editTitle() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.editTitle(Binding(EditTitleHelperForm)))
  }

  def editTitleP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(EditTitleHelperForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.editTitle(ib))
      case vb: ValidBinding => {
        val lookupIsbn: String = vb.valueOf(EditTitleHelperForm.isbn)
        Redirect(routes.Books.editTitleHelper(lookupIsbn))
      }
    }
  }

  /**
   * Regex: /books/addTitleToPrintQueue/:isbn/:cR
   *
   * Adds a title to the print queue and redirects the user to a print queue helper
   */
  def addTitleToPrintQueue(isbn: String, copyRange: String) = PermissionRequired(Permissions.Manage) { implicit request =>
    Title.getByIsbn(isbn) match {
      case None => Redirect(routes.Books.addTitleToPrintQueueHelper()).flashing("error" -> "Title not found")
      case Some(t) => {
        try {
          Books.sanitizeCopyRange(copyRange)
          val l = new LabelQueueSet(request.visit.role.getOrElse(null), t, copyRange)
          dataStore.pm.makePersistent(l)
          Redirect(routes.Books.addTitleToPrintQueueHelper()).flashing("message" -> "Labels added to print queue")
        } catch {
          case e: Exception => {
            Redirect(routes.Books.addTitleToPrintQueueHelper()).flashing("error" -> "Invalid copy range")
          }
        }
      }
    }
  }

  /**
   * Regex: /books/addTitleToPrintQueueHelper
   *
   * A form page that allows the user to add certain titles and page ranges to a printer setup.
   */
  def addTitleToPrintQueueHelper() = PermissionRequired(Permissions.Manage) { implicit request =>
    Ok(templates.books.addTitleToPrintQueueHelper(Binding(AddTitleToPrintQueueForm)))
  }

  def addTitleToPrintQueueHelperP() = PermissionRequired(Permissions.Manage) { implicit request =>
    Binding(AddTitleToPrintQueueForm, request) match {
      case ib: InvalidBinding => Ok(templates.books.addTitleToPrintQueueHelper(ib))
      case vb: ValidBinding => {
        val lookupIsbn: String = vb.valueOf(AddTitleToPrintQueueForm.isbn)
        val copyRange: String = vb.valueOf(AddTitleToPrintQueueForm.copyRange)
        Redirect(routes.Books.addTitleToPrintQueue(lookupIsbn, copyRange.replaceAll("\\s", "")))
      }
    }
  }

  /**
   * Regex: /books/viewPrintQueue
   *
   * Displays the items in the current print queue.
   */
  def viewPrintQueue() = PermissionRequired(Permissions.Manage) { implicit request =>
    val labelSets = dataStore.pm.query[LabelQueueSet].executeList
    Ok(templates.books.viewPrintQueue(Binding(new ViewPrintQueueForm(labelSets))))
  }

  def viewPrintQueueP() = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      val labelSets = pm.query[LabelQueueSet].executeList()
      val f = new ViewPrintQueueForm(labelSets)
      Binding(f, req) match {
        case ib: InvalidBinding => Ok(templates.books.viewPrintQueue(ib))
        case vb: ValidBinding => {
          val setsToPrint: List[LabelQueueSet] = vb.valueOf(f.cboxes)
          val tempFile = createPdf(setsToPrint, UUID.randomUUID().toString())
          pm.deletePersistentAll(setsToPrint)
          Ok.sendFile(content = tempFile.file, fileName = _ => "labels.pdf")
        }
      }
    }
  }

  /**
   * Regex: /books/deleteCopy/:barcode
   *
   * Removes the copy with given barcode from the database and redirects the user
   * to the deleteCopyHelper
   */
  def deleteCopyP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(ChooseCopyForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.deleteCopy(ib))
      case vb: ValidBinding => {
        val c = vb.valueOf(ChooseCopyForm.copy)
        dataStore.execute { pm =>
          val cand = QCheckout.candidate
          pm.query[Checkout].filter(cand.copy.eq(c).and(cand.endDate.eq(null.asInstanceOf[java.sql.Date]))).executeOption() match {
            case None => {
              c.deleted = true
              pm.makePersistent(c)
            }
            case Some(ch) => {
              ch.endDate = Some(LocalDate.now())
              pm.makePersistent(ch)
              c.deleted = true
              pm.makePersistent(c)
            }
          }
        }
        Redirect(routes.Books.deleteCopy()).flashing("message" -> "Copy deleted")
      }
    }
  }

  /**
   * Regex: /books/deleteCopyHelper
   *
   * A form page that allows the user to delete the current copy.
   */
  def deleteCopy() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.deleteCopy(Binding(ChooseCopyForm)))
  }

  /**
   * Regex: /books/deleteTitle/:isbn
   *
   * Deletes the title with given isbn from the database and redirects the user
   * to the deleteTitleHelper controller
   */
  def deleteTitleP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(ChooseTitleForm, req) match {
      case ib: InvalidBinding => Ok(templates.books.deleteTitle(ib))
      case vb: ValidBinding => dataStore.execute { pm =>
        val t = vb.valueOf(ChooseTitleForm.title)
        val cand = QPurchaseGroup.candidate
        val pg = pm.query[PurchaseGroup].filter(cand.title.eq(t)).executeList()
        if (pg.isEmpty) {
          pm.deletePersistent(t)
          Redirect(routes.Books.deleteTitle()).flashing("message" -> "Title successfully deleted.")
        } else {
          Redirect(routes.Books.deleteTitle()).flashing("error" -> "Books of this title purchased. Contact your system administrator to remove.")
        }
      }
    }
  }

  def deleteTitle() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.deleteTitle(Binding(ChooseTitleForm)))
  }

  /**
  * Regex: /books/printSingleSection
  *
  * A form page that allows a user to pick a section and then print all of the student labels for that section
  */
  def printSingleSection() = PermissionRequired(Permissions.Manage) { implicit req =>
    val cand = QSection.candidate()
    val sections = dataStore.pm.query[Section].filter(cand.terms.contains(Term.current)).executeList()
    val m = sections.map(s => (s.displayName, s.sectionId)).toMap
    val secs = sections.map(s => s.displayName)
    val f = new ChooseSectionForm(m, secs)
    Ok(templates.books.printSingleSection(Binding(f)))
  }

  def printSingleSectionP() = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      val cand = QSection.candidate()
      val sections = pm.query[Section].filter(cand.terms.contains(Term.current)).executeList()
      val m = sections.map(s => (s.displayName, s.sectionId)).toMap
      val secs = sections.map(s => s.displayName)
      val f = new ChooseSectionForm(m, secs)
      Binding(f, req) match {
        case ib: InvalidBinding => Ok(templates.books.printSingleSection(ib))
        case vb: ValidBinding => {
          val sectionId = vb.valueOf(f.section)
          Section.getBySectionId(sectionId) match {
            case None => Redirect(routes.Books.printSingleSection)
            case Some(s) => {
              makeSectionBarcodes(List(s))
              Redirect(routes.Books.displaySectionPdf)
            }
          }
        }
      }
    }
  }

  def displaySectionPdf() = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      Ok.sendFile(content = new java.io.File("public/sectionBarcodes.pdf"), inline = true)
    }
  }

  /**
  * Regex: /books/printSectionsByDept
  *
  * Lets the user select a department and then print the student labels for each section in that department
  */
  def printSectionsByDept() = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      val f = new ChooseDeptForm()
      Ok(templates.books.printSectionsByDept(Binding(f)))
    }
  }

  def printSectionsByDeptP() = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      val f = new ChooseDeptForm()
      Binding(f, req) match {
        case ib: InvalidBinding => Ok(templates.books.printSectionsByDept(ib))
        case vb: ValidBinding => {
          val cand = QDepartment.candidate
          val dept = vb.valueOf(f.dept)
          pm.query[Department].filter(cand.eq(dept)).executeOption() match {
            case None => Redirect(routes.Books.printSectionsByDept).flashing("error" -> "Department not found")
            case Some(d) => {
              val secCand = QSection.candidate
              val courseVar = QCourse.variable("courseVar")
              val roomVar = QRoom.variable("roomVar")
              val sections = pm.query[Section].filter(secCand.terms.contains(Term.current).and(
                  secCand.course.eq(courseVar)).and(courseVar.department.eq(d)).and(
                  secCand.room.eq(roomVar))).orderBy(roomVar.name.asc()).executeList()
              makeSectionBarcodes(sections)
              Redirect(routes.Books.displaySectionPdf)
            }
          }
        }
      }
    }
  }

  /*
  * Regex: /books/printAllSections
  *
  * Prints student barcodes for all sections
  */
  def printAllSections = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      val secCand = QSection.candidate
      val roomVar = QRoom.variable("roomVar")
      val sections = pm.query[Section].filter(secCand.terms.contains(Term.current).and(
          secCand.room.eq(roomVar))).orderBy(roomVar.name.asc()).executeList()
      makeSectionBarcodes(sections)
      Redirect(routes.Books.displaySectionPdf)
    }
  }

  /*
  * Regex: /books/reportCopyLost
  *
  * Reports a copy lost
  */
  def reportCopyLost(barcode: String) = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      if (req.visit.redirectUrl == None) {
        req.visit.redirectUrl = routes.App.index
        pm.makePersistent(req.visit)
      }
      val redirectLoc: play.api.mvc.Call = req.visit.redirectUrl.get
      Copy.getByBarcode(barcode) match {
        case Some(c) => {
          if (!c.isCheckedOut) {
            c.isLost = true
            pm.makePersistent(c)
            Redirect(redirectLoc).flashing("message" -> "Copy successfully marked lost")
          } else {
            val cand = QCheckout.candidate
            pm.query[Checkout].filter(cand.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand.copy.eq(c))).executeOption() match {
              case None => Redirect(redirectLoc).flashing("error" -> "Error: please contact your system administrator")
              case Some(ch) => {
                ch.endDate = Some(LocalDate.now)
                pm.makePersistent(ch)
                c.isLost = true
                pm.makePersistent(c)
                Redirect(redirectLoc).flashing("message" -> "Copy successfully marked lost")
              }
            }
          }
        }
        case None => Redirect(redirectLoc).flashing("error" -> "Copy not found")
      }
    }
  }

  /*
  * Regex: /books/quickCheckoutHelper/:stuId/:barcode
  *
  * Checks a copy out to a student and is the helper method for quickCheckout
  */
  def quickCheckoutHelper(stuId: String, barcode: String) = PermissionRequired(Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      val cand = QStudent.candidate
      pm.query[Student].filter(cand.stateId.eq(stuId).or(cand.studentNumber.eq(stuId))).executeOption() match {
        case None => Ok(<li class="skip">Could not find student \u2718</li>.toString)
        case Some(s) => {
          Copy.getByBarcode(barcode) match {
            case None => Ok(<li class="skip">Copy with the given barcode not found \u2718</li>.toString)
            case Some(c) => {
              val cand2 = QCheckout.candidate
              pm.query[Checkout].filter(cand2.endDate.eq(null.asInstanceOf[java.sql.Date]).and(cand2.copy.eq(c))).executeOption() match {
                case Some(currentCheckout) => {
                  currentCheckout.endDate = Some(LocalDate.now())
                  pm.makePersistent(currentCheckout)
                  val ch = new Checkout(s, c, Some(LocalDate.now()), None)
                  pm.makePersistent(ch)
                  Ok(<li>{ s.formalName + " was assigned Copy " + c.number + " of " + c.purchaseGroup.title.name + " \u2714" }</li>.toString)
                }
                case None => {
                  val ch = new Checkout(s, c, Some(LocalDate.now()), None)
                  pm.makePersistent(ch)
                  Ok(<li>{ s.formalName + " was assigned Copy " + c.number + " of " + c.purchaseGroup.title.name + " \u2714" }</li>.toString)
                }
              }
            }
          }
        }
      }
    }
  }

  /*
  * Regex: /books/quickCheckout
  *
  * Displays the quickCheckout page
  */
  def quickCheckout() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(templates.books.quickCheckout())
  }

}

object Books extends UsesDataStore {
  import Student.StudentList
  // This is the companion object to the Books class

  final val df = org.joda.time.format.DateTimeFormat.forPattern("MM/dd/yyyy")

  class IsbnField(name: String) extends TextField(name) {
    override val minLength = Some(10)
    override val maxLength = Some(14)
    override def asValue(strs: Seq[String]): Either[ValidationError, String] = {
      strs match {
        case Seq(s) => Title.asValidIsbn13(s) match {
          case Some(t) => Right(t.filterNot(c => c == '-'))
          case _ => Left(ValidationError("This value must be a valid 10 or 13-digit ISBN"))
        }
        case _ => Left(ValidationError("This value must be a valid 10 or 13-digit ISBN"))
      }
    }
  }

  class TitleField(name: String) extends BaseTextField[Title](name) {
    def asValue(strs: Seq[String]): Either[ValidationError, Title] = {
      strs match {
        case Seq(s) => {
          Title.getByIsbn(s.filterNot(c => c == '-')) match {
            case Some(t) => Right(t)
            case _ => Left(ValidationError("Title not found"))
          }
        }
        case _ => Left(ValidationError("Title not found"))
      }
    }
  }

  class CopyField(name: String) extends BaseTextField[Copy](name) {
    def asValue(strs: Seq[String]): Either[ValidationError, Copy] = {
      strs match {
        case Seq(s) => {
          Copy.getByBarcode(s) match {
            case Some(c) => Right(c)
            case _ => Left(ValidationError("Copy not found"))
          }
        }
        case _ => Left(ValidationError("Copy not found"))
      }
    }
  }

  object CheckoutForm extends Form {
    val student = new StudentField("Student", StudentList.studentsIds)
    val cpy = new CopyField("Barcode")

    val fields = List(student, cpy)
  }

  object CheckInForm extends Form {
    val copy = new CopyField("Barcode")

    val fields = List(copy)
  }

  object TitleForm extends Form {
    val isbn = new IsbnField("ISBN") {
      override def validators = super.validators ++ List(Validator((str: String) => Title.getByIsbn(str) match {
        case Some(isbn) => ValidationError("ISBN already exists in database.")
        case None => ValidationError(Nil)
      }))
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

  object CheckoutBulkForm extends Form {
    val student = new StudentIdField("Student", StudentList.studentsIds)

    val fields = List(student)
  }

  object CheckoutBulkHelperForm extends Form {
    val copy = new CopyField("Barcode")

    override def submitText = "Add"

    val fields = List(copy)
  }

  object AddPurchaseGroupForm extends Form {
    val title = new TitleField("ISBN")
    val purchaseDate = new DateField("Purchase Date")
    val price = new NumericField[Double]("Price")
    val numCopies = new NumericField[Int]("Number of Copies")

    val fields = List(title, purchaseDate, price, numCopies)
  }

  object ChooseGradeForm extends Form {
    val grade = new ChoiceField[Int]("Grade", List("Freshman" -> 9, "Sophomore" -> 10, "Junior" -> 11, "Senior" -> 12))

    def fields = List(grade)
  }

  object ChooseStudentForm extends Form {
    val student = new StudentField("Student", StudentList.studentsIds)

    def fields = List(student)
  }

  object ChooseCopyForm extends Form {
    val copy = new CopyField("Barcode")

    def fields = List(copy)
  }

  object EditTitleHelperForm extends Form {
    val isbn = new IsbnField("ISBN")

    def fields = List(isbn)
  }

  class EditTitleForm(iName: String, iAuthor: Option[String], iPublisher: Option[String], iNumPages: Option[Int], iDimensions: Option[String], iWeight: Option[Double]) extends Form {
    val name = new TextField("Name") {
      override def initialVal = Some(iName)
      override val maxLength = Some(80)
    }
    val author = new TextFieldOptional("Author(s)") {
      override def initialVal = Some(iAuthor)
      override val maxLength = Some(80)
    }
    val publisher = new TextFieldOptional("Publisher") {
      override def initialVal = Some(iPublisher)
      override val maxLength = Some(80)
    }
    val numPages = new NumericFieldOptional[Int]("Number Of Pages") {
      override def initialVal = Some(iNumPages)
    }
    val dimensions = new TextFieldOptional("Dimensions (in)") {
      override def initialVal = Some(iDimensions)
    }
    val weight = new NumericFieldOptional[Double]("Weight (lbs)") {
      override def initialVal = Some(iWeight)
    }
    val imageUrl = new UrlFieldOptional("New Image URL")

    // TODO: this should probably be a Call, not a String
    override def cancelTo = Some(Call(Method.GET, "/books/editTitle"))

    def fields = List(name, author, publisher, numPages, dimensions, weight, imageUrl)
  }

  object ChooseTitleForm extends Form {
    val title = new TitleField("ISBN")

    val fields = List(title)
  }

  object AddTitleToPrintQueueForm extends Form {
    val isbn = new IsbnField("ISBN")
    val copyRange = new TextField("Copy Range")

    val fields = List(isbn, copyRange)
  }

  object BulkCheckInForm extends Form {
    val barcodes = new TextField("Barcodes") {
      override def widget = new Textarea(required)
    }

    val fields = List(barcodes)
  }

  class ChooseSectionForm(m: Map[String, String], secs: List[String]) extends Form {
    val section = new AutocompleteField("Section", secs) {
      override def asValue(s: Seq[String]): Either[ValidationError, String] = {
        if (s.isEmpty) {
          Left(ValidationError("This field is required."))
        } else {
          Right(m.get(s(0)).getOrElse(""))
        }
      }
    }
    val fields = List(section)
  }

  class ChooseDeptForm extends Form {
    val cand = QDepartment.candidate()
    val depts = dataStore.pm.query[Department].orderBy(cand.name.asc()).executeList().map(d => (d.name, d))
    val dept = new ChoiceField[Department]("Department", depts)

    val fields = List(dept)
  }

  class ViewPrintQueueForm(l: List[LabelQueueSet]) extends Form {
    val cboxes = new CheckboxField("Labels to Print", l.map(s => (s.toString, s)))

    val fields = List(cboxes)
  }

  // Helper Method
  def sanitizeCopyRange(s: String): List[Int] = {
    val newS = s.trim.split(",")
    var res: List[Int] = List[Int]()
    for (x <- newS) {
      if (x.contains("-")) {
        val newX = x.trim.split("-")
        val startVal = newX(0).trim.toInt
        val endVal = newX(1).trim.toInt
        val temp = startVal.until(endVal + 1).toList
        res = res ++ temp
      } else {
        res = res :+ x.trim.toInt
      }
    }
    res
  }

  // Helper Method
  def downloadImage(url: java.net.URL, isbn: String) = {
    val pic = ImageIO.read(url)
    ImageIO.write(pic, "jpg", new File("public/images/books/" + isbn + ".jpg"))
  }

  // Helper Method
  def makeBarcode(barcode: String): Barcode = {
    val b: Barcode128 = new Barcode128()
    b.setCode(barcode)
    b.setAltText(barcode)
    return b
  }

  // Helper Method
  def cropText(s: String): String = {
    // This will crop strings so that they fit on a label
    val w = Utilities.inchesToPoints(2.6f) - 12
    val font = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, false)
    if (font.getWidthPoint(s, 10f) <= w) {
      s
    } else {
      cropText(s.substring(0, s.length - 1))
    }
  }

  // Helper Method - Given a list of Sections, it makes a PDF with a label for each student in the section.
  //  Each label contains the student's name, the section, and a barcode of the student's stateId. Also, this starts a new page for each section.
  def makeSectionBarcodes(sections: List[Section]) {
    // Spacing in points
    // Bottom left: 0,0
    // Top right: 612, 792

    // Avery 5160 labels have 1/2 inch top/bottom margins and 0.18 inch left/right margins.
    // Labels are 2.6" by 1". Labels abut vertically but there is a .15" gutter horizontally.

    // A Barcode Label is an Avery 5160 label with three lines of text across the top and
    // a Code128 barcode under them. The text is cropped to an appropriate width and the
    // barcode is sized to fit within the remainder of the label.

    // inchesToPoints gives the point value for a measurement in inches

    // Spacing Increments
    // Top to bottom (inches)
    // 0.5 1.0 1.0 1.0 0.5
    // Left to right (inches)
    // 0.18 2.6 0.15 2.6 0.15 2.6 0.18

    val halfInch = Utilities.inchesToPoints(.5f)
    val inch = Utilities.inchesToPoints(1f)
    val gutter = Utilities.inchesToPoints(.15f)
    val lAndRBorder = Utilities.inchesToPoints(.18f)
    val labelWidth = Utilities.inchesToPoints(2.6f)

    val topLeftX = lAndRBorder
    val topLeftY = 792 - halfInch - 10

    val result: String = "public/sectionBarcodes.pdf"
    val document: Document = new Document(PageSize.LETTER)
    val writer = PdfWriter.getInstance(document, new FileOutputStream(result))
    document.open()
    val cb = writer.getDirectContent() //PdfContentByte
    val font = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, false)
    var labelTopLeftX = topLeftX
    var labelTopLeftY = topLeftY
    var n = 0

    for (section <- sections) {
      val students = section.students().sortWith((s1, s2) => s1.formalName < s2.formalName)
      val sec = section.labelName
      val line3 = section.teachers.map(t => t.formalName).mkString(", ") + " - " + section.room.name
      document.newPage()
      n = 0
      labelTopLeftX = topLeftX
      labelTopLeftY = topLeftY

      for (student <- students) {
        // Do this for each section but change the position so that it is a new label each time
        val id = if (student.stateId != null && student.stateId != "") student.stateId else if (student.studentNumber != null && student.studentNumber != "") student.studentNumber else  "0000000000"
        val b = makeBarcode(id)
        val studentName = student.formalName

        cb.setFontAndSize(font, 10)
        cb.beginText()
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, cropText(studentName), (labelTopLeftX + 6), labelTopLeftY, 0)
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, cropText(sec), (labelTopLeftX + 6), (labelTopLeftY - 8), 0)
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, cropText(line3), (labelTopLeftX + 6), (labelTopLeftY - 16), 0)
        cb.endText()
        b.setX(1.0f)
        val img = b.createImageWithBarcode(cb, null, null)
        val barcodeOffset = (labelWidth - img.getPlainWidth()) / 2
        cb.addImage(img, img.getPlainWidth, 0, 0, img.getPlainHeight, (labelTopLeftX + barcodeOffset), (labelTopLeftY - 52))

        n += 1

        if (n % 3 == 0) {
          labelTopLeftX = topLeftX
          labelTopLeftY = labelTopLeftY - inch
        } else {
          labelTopLeftX = labelTopLeftX + labelWidth + gutter
        }
        if (n % 30 == 0 && student != students.last) {
          // Make a new page
          document.newPage()
          cb.setFontAndSize(font, 10)
          labelTopLeftY = topLeftY
        }
      }
    }

    document.close()
  }

  // Helper Method
  def makePdf(barcodes: List[(Barcode, String, String, String)], filename: String): TemporaryFile = { //Barcode, title.name, title.author, title.publisher
    // Spacing in points
    // Bottom left: 0,0
    // Top right: 612, 792

    // Avery 5160 labels have 1/2 inch top/bottom margins and 0.18 inch left/right margins.
    // Labels are 2.6" by 1". Labels abut vertically but there is a .15" gutter horizontally.

    // A Barcode Label is an Avery 5160 label with three lines of text across the top and
    // a Code128 barcode under them. The text is cropped to an appropriate width and the
    // barcode is sized to fit within the remainder of the label.

    // inchesToPoints gives the point value for a measurement in inches

    // Spacing Increments
    // Top to bottom (inches)
    // 0.5 1.0 1.0 1.0 0.5
    // Left to right (inches)
    // 0.18 2.6 0.15 2.6 0.15 2.6 0.18

    val halfInch = Utilities.inchesToPoints(.5f)
    val inch = Utilities.inchesToPoints(1f)
    val gutter = Utilities.inchesToPoints(.15f)
    val lAndRBorder = Utilities.inchesToPoints(.18f)
    val labelWidth = Utilities.inchesToPoints(2.6f)

    val topLeftX = lAndRBorder
    val topLeftY = 792 - halfInch - 10

    val document: Document = new Document(PageSize.LETTER)
    val tempFile: TemporaryFile = TemporaryFile(new File(s"/tmp/$filename"))
    val writer = PdfWriter.getInstance(document, new FileOutputStream(tempFile.file))
    document.open()
    val cb = writer.getDirectContent() //PdfContentByte
    val font = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, false)
    cb.setFontAndSize(font, 10)
    var labelTopLeftX = topLeftX
    var labelTopLeftY = topLeftY
    var n = 0

    for (barcode <- barcodes) {
      // Do this for each barcode but change the position so that it is a new label each time

      cb.beginText()
      cb.showTextAligned(PdfContentByte.ALIGN_LEFT, cropText(barcode._2), (labelTopLeftX + 6), labelTopLeftY, 0)
      cb.showTextAligned(PdfContentByte.ALIGN_LEFT, cropText(barcode._3), (labelTopLeftX + 6), (labelTopLeftY - 8), 0)
      cb.showTextAligned(PdfContentByte.ALIGN_LEFT, cropText(barcode._4), (labelTopLeftX + 6), (labelTopLeftY - 16), 0)
      cb.endText()
      val b = barcode._1
      b.setX(0.7f)
      val img = b.createImageWithBarcode(cb, null, null)
      val barcodeOffset = (labelWidth - img.getPlainWidth()) / 2
      cb.addImage(img, img.getPlainWidth, 0, 0, img.getPlainHeight, (labelTopLeftX + barcodeOffset), (labelTopLeftY - 52))

      n += 1

      if (n % 3 == 0) {
        labelTopLeftX = topLeftX
        labelTopLeftY = labelTopLeftY - inch
      } else {
        labelTopLeftX = labelTopLeftX + labelWidth + gutter
      }
      if (n % 30 == 0) {
        // Make a new page
        document.newPage()
        cb.setFontAndSize(font, 10)
        labelTopLeftY = topLeftY
      }
    }
    document.close()
    tempFile
  }

  // Helper Method
  def createPdf(l: List[LabelQueueSet], filename: String): TemporaryFile = {
    var printList = List[(Barcode, String, String, String)]()
    for (x <- l) {
      val r = Books.sanitizeCopyRange(x.copyRange)
      for (y <- r) {
        val b = makeBarcode("%s-%s-%05d".format(x.title.isbn, "200", y))
        printList = printList :+ (b, x.title.name, x.title.author.getOrElse(""), x.title.publisher.getOrElse(""))
      }
    }
    makePdf(printList, filename)
  }
  
}
