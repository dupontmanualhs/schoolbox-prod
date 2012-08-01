package util

import scala.xml.{Elem, NodeSeq}
import models.users.Perspective

class MenuItem(val name: String, val link: Option[String], val subItems: List[MenuItem]) {
  def asHtml: Elem = <li><a href={ link.getOrElse("#") }>{ name }</a>{
                       if (subItems.isEmpty) NodeSeq.Empty
                       else <ul>{ subItems.flatMap(_.asHtml) }</ul>
                     }</li>
}

class MenuBar(val menus: List[MenuItem]) {
  def asHtml: Elem = <ul class="sf-menu">{ menus.flatMap(_.asHtml) }</ul>
}

object Menu {
  val login = new MenuItem("Log in", Some(controllers.routes.Users.login().toString), Nil)
  val logout = new MenuItem("Log out", Some(controllers.routes.Users.logout().toString), Nil)
  val changePassword = new MenuItem("Change Password", Some(controllers.routes.Users.changePassword().toString), Nil)
  
  def buildMenu(persp: Option[Perspective]): Elem = {
    val acctItems = if (persp.isDefined) List(logout, changePassword) else List(login)
    val acct: MenuItem = new MenuItem("Account", None, acctItems)
    val courses = new MenuItem("Courses", None, Nil)
    val bar = new MenuBar(List(acct, courses))
    bar.asHtml
  }
}