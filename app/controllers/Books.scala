package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import models.books._

object Books extends Controller {

  def addBooks() = TODO 
  
  def addBooksSubmit() = TODO
  
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
  
  def findCopyHistory() = TODO
  
  def findCopyHistorySubmit() = TODO
  
  def copyHistory(copyId: Long) = DbAction { implicit req =>
    implicit val pm = req.pm
    val df = new java.text.SimpleDateFormat("dd/mm/yyyy")
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
  
  def checkoutHistory() = TODO
  
  def checkoutHistorySubmit() = TODO
  
  def checkoutsByTeacherStudents() = TODO
  
  def checkoutsByTeacherStudentsSubmit() = TODO
 
  def statistics() = TODO
  
  def copiesOutByTitle() = TODO
  
  def copiesOutByTitleSubmit() = TODO
  
  def allBooksOut(grade: Int = 13) = TODO
}