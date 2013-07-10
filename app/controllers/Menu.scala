package controllers

import scala.xml.{ Elem, NodeSeq }
import models.users.Role

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
  val login = new MenuItem("Log in", "menu_login", Some(controllers.users.routes.App.login().toString), Nil)
  val logout = new MenuItem("Log out", "menu_logout", Some(controllers.users.routes.App.logout().toString), Nil)
  val settings = new MenuItem("Settings", "menu_settings", Some(controllers.users.routes.App.settings().toString), Nil)
  val changeRole = new MenuItem("Change Role", "menu_changeRole", Some(controllers.users.routes.App.chooseRole().toString), Nil)
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
  val delCpy = new MenuItem("Delete Copy", "menu_delCpy", Some(controllers.routes.Books.deleteCopyHelper.toString), Nil)
  val delTitle = new MenuItem("Delete Title", "menu_delTitle", Some(controllers.routes.Books.deleteTitleHelper.toString), Nil)
  val del = new MenuItem("Delete", "menu_delTitle", None, Nil, List(delCpy, delTitle))
  val evenMoar = new MenuItem("Delete Level 1", "menu_evenMoar", None, Nil, List(del))
  
  def roleMenu(maybeRole: Option[Role]): MenuBar = maybeRole match {
    case None => new MenuBar(List(login))
    case Some(role) => new MenuBar(List(new MenuItem(role.displayNameWithRole, "menu_name", None, List(changeRole, settings, logout))))
  }

  def buildMenu(maybeRole: Option[Role]): NodeSeq = {
    val locItems = List(currloc, locsearch, locsched, findlocnum)
    val bookItems = List(addTitle, chkHistory, copyHistory, currentBks, addPurchaseGroup, inventory, checkout, checkIn, copyInfo, allBksOut, copyStatusByTitle,
      blkCheckout, editTitle, addToPrintQueue, viewQueue, evenMoar)
    val acct = new MenuItem("Account", "menu_account", None, acctItems)
    val courses = new MenuItem("Courses", "menu_courses", Some(controllers.routes.Courses.getMySchedule().toString), Nil)
    val lockers = new MenuItem("Lockers", "menu_lockers", None, locItems)
    val confr = new MenuItem("Conferences", "menu_conferences", Some(controllers.routes.Conferences.index().toString), Nil)
    val masteries = new MenuItem("Masteries", "menu_masteries", Some(controllers.routes.Mastery.menuOfTests().toString), Nil)
    val books = new MenuItem("Books", "menu_books", None, bookItems)
    val bar = new MenuBar(List(courses, lockers, confr, books, masteries))
    bar.asHtml ++ <div class="pull-right">{ roleMenu(maybeRole).asHtml }</div>
  }
}
