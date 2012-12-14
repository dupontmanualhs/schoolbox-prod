package util

import scala.xml.{Elem, NodeSeq}
import models.users.Perspective

class MenuItem(val name: String, val id: String, val link: Option[String], val subItems: List[MenuItem]) {
  def asHtml: Elem = <li><a href={ link.getOrElse("#") } id={ id }>{ name }</a>{
                        if (subItems.isEmpty) NodeSeq.Empty
                       else   
                         <li class="dropdown" role="menu" aria-labelledby="dropdownMenu">
                    	   <a class="dropdown-toggle" data-toggle="dropdown" href="#" >
  								
  								<b class="caret"></b>
  							</a>
  							<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
  								<li>
  									{ subItems.flatMap(_.asHtml) }
                       		    </li>
                       	   </ul>
                       	</li>
                     }</li>
}


class MenuBar(val menus: List[MenuItem]) {
  def asHtml: Elem = <ul class="nav">{ menus.flatMap(_.asHtml) }</ul>
}

object Menu {
  val login = new MenuItem("Log in", "menu_login", Some(controllers.routes.Users.login().toString), Nil)
  val logout = new MenuItem("Log out", "menu_logout", Some(controllers.routes.Users.logout().toString), Nil)
  val changePassword = new MenuItem("Change Password", "menu_changePassword", Some(controllers.routes.Users.changePassword().toString), Nil)
  
  def buildMenu(persp: Option[Perspective]): Elem = {
    val acctItems = if (persp.isDefined) List(logout, changePassword) else List(login)
    val acct: MenuItem = new MenuItem("Account", "menu_account", None, acctItems)
    val courses = new MenuItem("Courses", "menu_courses", Some(controllers.routes.Courses.getMySchedule().toString), Nil)
    val lockers = new MenuItem("Lockers", "menu_lockers", Some(controllers.routes.Lockers.index().toString), Nil)
    val bar = new MenuBar(List(courses, lockers))
    bar.asHtml
  }
}