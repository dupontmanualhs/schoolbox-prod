package controllers

import scala.xml.{ Elem, NodeSeq }
import models.users.Role
import config.users.{ Menu => UserMenu }

class MenuItem(val name: String, val id: String, val link: Option[String], val dropItems: List[MenuItem], val sideItems: List[MenuItem] = Nil) { //only include a sideItems list if it is a drop item
  def asHtml: Elem = if (dropItems.isEmpty && sideItems.isEmpty) {																				 //sideItems are the dropdown within a dropdown 
    <li>
      <a href={ link.getOrElse("#") } id={ id }>{ name }</a>
    </li>
  } else if(!dropItems.isEmpty){
    <li class="dropdown">
      <a class="dropdown-toggle" data-toggle="dropdown" href="#">
        { name }
        <b class="caret"> </b>
      </a>
      <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
          { dropItems.flatMap(_.asHtml) }
      </ul>
   </li >
  } else {
    <li class="dropdown-submenu">
	  <a tabindex="-1" href="#">{name}</a>
	  <ul class="dropdown-menu">
		{sideItems.flatMap(_.asHtml)}
   	  </ul>
    </li>
  }
}

class MenuBar(val menus: List[MenuItem]) {
  def asHtml: Elem = <ul class="nav">{ menus.flatMap(_.asHtml) }</ul>
}

object Menu {
  val currloc = new MenuItem("My Locker", "menu_lockerStatus", Some(controllers.routes.Lockers.getMyLocker().toString), Nil)
  val findlocnum = new MenuItem("Find Locker by Number", "menu_lockerNum", Some(controllers.routes.Lockers.lockerByNumber().toString), Nil)
  val locsearch = new MenuItem("Find Locker", "menu_lockerSearch", Some(controllers.routes.Lockers.lockerSearch().toString), Nil)
  val locsched = new MenuItem("Find Locker by Class", "menu_lockerClass", Some(controllers.routes.Lockers.schedule().toString), Nil)
  val addTitle = new MenuItem("Add a Title", "menu_addTitle", Some(controllers.routes.Books.addTitle().toString), Nil)
  val chkHistory = new MenuItem("Checkout History", "menu_chkHistory", Some(controllers.routes.Books.findCheckoutHistory.toString), Nil)
  val copyHistory = new MenuItem("Copy History", "menu_copyHistory", Some(controllers.routes.Books.findCopyHistory.toString), Nil)
  val currentBks = new MenuItem("Current Checkouts", "menu_currentBks", Some(controllers.routes.Books.findCurrentCheckouts.toString), Nil)
  val addPurchaseGroup = new MenuItem("Add Purchase Group", "menu_addPurchaseGroup", Some(controllers.routes.Books.addPurchaseGroup.toString), Nil)
  val inventory = new MenuItem("Inventory", "menu_inventory", Some(controllers.routes.Books.inventory.toString), Nil)
  val checkout = new MenuItem("Checkout", "menu_checkout", Some(controllers.routes.Books.checkout.toString), Nil)
  val checkIn = new MenuItem("Check In", "menu_checkIn", Some(controllers.routes.Books.checkIn.toString), Nil)
  val copyInfo = new MenuItem("Copy Info", "menu_copyInfo", Some(controllers.routes.Books.findCopyInfo.toString), Nil)
  val allBksOut = new MenuItem("All Books Out", "menu_allBksOut", Some(controllers.routes.Books.findAllBooksOut.toString), Nil)
  val copyStatusByTitle = new MenuItem("Copy Status By Title", "menu_copyStatusByTitle", Some(controllers.routes.Books.findCopyStatusByTitle.toString), Nil)
  val blkCheckout = new MenuItem("Bulk Checkout", "menu_bulkCheckout", Some(controllers.routes.Books.checkoutBulk.toString), Nil)
  val editTitle = new MenuItem("Edit Title", "menu_editTitle", Some(controllers.routes.Books.editTitle.toString), Nil)
  val addToPrintQueue = new MenuItem("Add Title to Print Queue", "menu_addToPrintQueue", Some(controllers.routes.Books.addTitleToPrintQueueHelper.toString), Nil)
  val viewQueue = new MenuItem("View Print Queue", "menu_viewQueue", Some(controllers.routes.Books.viewPrintQueue.toString), Nil)
  val delCpy = new MenuItem("Delete Copy", "menu_delCpy", Some(controllers.routes.Books.deleteCopy.toString), Nil)
  val delTitle = new MenuItem("Delete Title", "menu_delTitle", Some(controllers.routes.Books.deleteTitle.toString), Nil)
  val blkCheckIn = new MenuItem("Bulk Check In", "menu_blkCheckIn", Some(controllers.routes.Books.checkInBulk.toString), Nil)
  val print1Sec = new MenuItem("Single Section", "menu_print1Sec", Some(controllers.routes.Books.printSingleSection.toString), Nil)
  val printByDept = new MenuItem("Sections by Department", "menu_printByDept", Some(controllers.routes.Books.printSectionsByDept.toString), Nil)
  val printAllSec = new MenuItem("All Sections", "menu_printAllSec", Some(controllers.routes.Books.printAllSections.toString), Nil)
  val quickChout = new MenuItem("Quick Checkout", "menu_quickChout", Some(controllers.routes.Books.quickCheckout.toString), Nil)

  val manage = new MenuItem("Manage", "menu_manage", None, Nil, List(quickChout, addTitle, addPurchaseGroup, checkout, blkCheckout, checkIn, blkCheckIn, editTitle,
    delCpy, delTitle))
  val view = new MenuItem("View", "menu_view", None, Nil, List(chkHistory, copyHistory, currentBks, inventory, copyInfo, allBksOut, copyStatusByTitle))
  val print = new MenuItem("Print", "menu_print", None, Nil, List(addToPrintQueue, viewQueue, print1Sec, printByDept, printAllSec))

  /* Example for how to do multiple menu levels
  val del = new MenuItem("Delete", "menu_delTitle", None, Nil, List(delCpy, delTitle))
  val evenMoar = new MenuItem("Delete Level 1", "menu_evenMoar", None, Nil, List(del))
  */

  def buildMenu(maybeRole: Option[Role]): NodeSeq = {
    val locItems = List(currloc, locsearch, locsched, findlocnum)
    val bookItems = List(manage, view, print)
    val courses = new MenuItem("Courses", "menu_courses", Some(controllers.courses.routes.App.mySchedule().toString), Nil)
    val lockers = new MenuItem("Lockers", "menu_lockers", None, locItems)
    val confr = new MenuItem("Conferences", "menu_conferences", Some(controllers.routes.Conferences.index().toString), Nil)
    val masteries = new MenuItem("Masteries", "menu_masteries", Some(controllers.routes.Mastery.menuOfTests().toString), Nil)
    val books = new MenuItem("Books", "menu_books", None, bookItems)
    val bar = new MenuBar(List(courses, lockers, confr, books, masteries))
    bar.asHtml ++ <div class="pull-right">{ UserMenu.forRole(maybeRole).asHtml }</div>
  }
}
