package config

import controllers.users.{ MenuItem, MenuBar }
import models.books.Book
import models.users.Role

object BookMenu {
  def SomeIf[T](cond: Boolean)(t: => T): Option[T] = if (cond) Some(t) else None

  def forRole(maybeRole: Option[Role]): Option[MenuItem] = maybeRole match {
    case None => None
    case Some(role) => {
      val perms = role.permissions()
      val items: List[MenuItem] = List(
          SomeIf(perms.contains(Book.Permissions.Manage)) {
            new MenuItem("Manage", "menu_manage", None, Nil, List(
              new MenuItem("Quick Checkout", "menu_quickChout", Some(controllers.routes.Books.quickCheckout.toString), Nil),
              new MenuItem("Add a Title", "menu_addTitle", Some(controllers.routes.Books.addTitle().toString), Nil),
              new MenuItem("Add Purchase Group", "menu_addPurchaseGroup", Some(controllers.routes.Books.addPurchaseGroup.toString), Nil),
              new MenuItem("Checkout", "menu_checkout", Some(controllers.routes.Books.checkout.toString), Nil),
              new MenuItem("Bulk Checkout", "menu_bulkCheckout", Some(controllers.routes.Books.checkoutBulk.toString), Nil),
              new MenuItem("Check In", "menu_checkIn", Some(controllers.routes.Books.checkIn.toString), Nil),
              new MenuItem("Bulk Check In", "menu_blkCheckIn", Some(controllers.routes.Books.checkInBulk.toString), Nil),
              new MenuItem("Edit Title", "menu_editTitle", Some(controllers.routes.Books.editTitle.toString), Nil),
              new MenuItem("Delete Copy", "menu_delCpy", Some(controllers.routes.Books.deleteCopy.toString), Nil),
              new MenuItem("Delete Title", "menu_delTitle", Some(controllers.routes.Books.deleteTitle.toString), Nil)
            ))
          },
          SomeIf(perms.contains(Book.Permissions.Manage)) {
            new MenuItem("Print", "menu_print", None, Nil, List(
              new MenuItem("Add Title to Print Queue", "menu_addToPrintQueue", Some(controllers.routes.Books.addTitleToPrintQueueHelper.toString), Nil), 
              new MenuItem("View Print Queue", "menu_viewQueue", Some(controllers.routes.Books.viewPrintQueue.toString), Nil), 
              new MenuItem("Single Section", "menu_print1Sec", Some(controllers.routes.Books.printSingleSection.toString), Nil), 
              new MenuItem("Sections by Department", "menu_printByDept", Some(controllers.routes.Books.printSectionsByDept.toString), Nil), 
              new MenuItem("All Sections", "menu_printAllSec", Some(controllers.routes.Books.printAllSections.toString), Nil)
            ))
          },
          SomeIf(perms.contains(Book.Permissions.Manage)) {
            new MenuItem("View", "menu_view", None, Nil, List(
              new MenuItem("Checkout History", "menu_chkHistory", Some(controllers.routes.Books.findCheckoutHistory.toString), Nil), 
              new MenuItem("Copy History", "menu_copyHistory", Some(controllers.routes.Books.findCopyHistory.toString), Nil), 
              new MenuItem("Current Checkouts", "menu_currentBks", Some(controllers.routes.Books.findCurrentCheckouts.toString), Nil), 
              new MenuItem("Inventory", "menu_inventory", Some(controllers.routes.Books.inventory.toString), Nil), 
              new MenuItem("Copy Info", "menu_copyInfo", Some(controllers.routes.Books.findCopyInfo.toString), Nil), 
              new MenuItem("All Books Out", "menu_allBksOut", Some(controllers.routes.Books.findAllBooksOut.toString), Nil), 
              new MenuItem("Copy Status By Title", "menu_copyStatusByTitle", Some(controllers.routes.Books.findCopyStatusByTitle.toString), Nil)
            ))
          }
        ).flatten
      SomeIf(!items.isEmpty)(new MenuItem("Books", "menu_books", None, items))
    }
  }
}