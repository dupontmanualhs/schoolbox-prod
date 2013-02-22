package util

import scala.xml.{Elem, NodeSeq}
import models.users.Perspective

class MenuItem(val name: String, val id: String, val link: Option[String], val subItems: List[MenuItem]) {
  def asHtml: Elem = if (subItems.isEmpty) {
    					<li><a href={ link.getOrElse("#") } id={ id }>{ name }</a>
    					</li>
    					}
                         else{
                         <li class="dropdown" >
                    	   <a class="dropdown-toggle" data-toggle="dropdown" href="#" >
  								{ name }
  								<b class="caret"></b>
  							</a>
  							<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
  								<li>
  									{ subItems.flatMap(_.asHtml) }
                       		    </li>
                       	   </ul>
                       	</li>
                         }
}


class MenuBar(val menus: List[MenuItem]) {
  def asHtml: Elem = <ul class="nav">{ menus.flatMap(_.asHtml) }</ul>
}

object Menu {
  val login = new MenuItem("Log in", "menu_login", Some(controllers.routes.Users.login().toString), Nil)
  val logout = new MenuItem("Log out", "menu_logout", Some(controllers.routes.Users.logout().toString), Nil)
  val settings = new MenuItem("Settings", "menu_settings", Some(controllers.routes.Users.settings().toString), Nil)
  val currloc = new MenuItem("My Locker", "menu_lockerStatus", Some(controllers.routes.Lockers.getMyLocker().toString), Nil)
  val findlocnum = new MenuItem("Find Locker by Number", "menu_lockerNum", Some(controllers.routes.Lockers.lockerByNumber().toString), Nil)
  val locsearch = new MenuItem("Find Locker", "menu_lockerSearch", Some(controllers.routes.Lockers.lockerSearch().toString), Nil)
  val locsched = new MenuItem("Find Locker by Class", "menu_lockerClass", Some(controllers.routes.Lockers.schedule().toString), Nil)
  val addTitle = new MenuItem("Add a Title", "menu_addTitle" , Some(controllers.routes.Books.addTitle().toString), Nil)
  val chkHistory = new MenuItem("Checkout History", "menu_chkHistory", Some(controllers.routes.Books.findCheckoutHistory.toString), Nil)
  val copyHistory = new MenuItem("Copy History", "menu_copyHistory", Some(controllers.routes.Books.findCopyHistory.toString), Nil)
  val currentBks = new MenuItem("Current Checkouts", "menu_currentBks", Some(controllers.routes.Books.findCurrentCheckouts.toString), Nil)     
  
  def buildMenu(persp: Option[Perspective]): Elem = {
    val acctItems = if (persp.isDefined) List(logout, settings) else List(login)
    val locItems = List(currloc, locsearch, locsched, findlocnum)
    val bookItems = List(addTitle, chkHistory, copyHistory, currentBks)
    val acct: MenuItem = new MenuItem("Account", "menu_account", None, acctItems)
    val courses = new MenuItem("Courses", "menu_courses", Some(controllers.routes.Courses.getMySchedule().toString), Nil)
    val lockers = new MenuItem("Lockers", "menu_lockers", None, locItems)
    val confr = new MenuItem("Conferences", "menu_conferences", Some(controllers.routes.Conferences.index().toString), Nil)
    val books = new MenuItem("Books", "menu_books", None, bookItems)
    val bar = new MenuBar(List(courses, lockers, confr, books))
    bar.asHtml
  }
}