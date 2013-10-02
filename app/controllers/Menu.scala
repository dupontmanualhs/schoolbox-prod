package controllers

import scala.xml.{ Elem, NodeSeq }
import models.users.{ Role, User }
import controllers.courses.{ Menu => CoursesMenu }
import config.users.{ Menu => UserMenu }
import config.{ BookMenu, ConferencesMenu }
import controllers.users.{ MenuItem, MenuBar }

object Menu {
  def buildMenu(maybeRole: Option[Role]): NodeSeq = {
    val books: Option[MenuItem] = BookMenu.forRole(maybeRole)
    val courses: Option[MenuItem] = CoursesMenu.forRole(maybeRole)
    val confs: Option[MenuItem] = ConferencesMenu.forRole(maybeRole)
    val admin: Option[MenuItem] = AdminMenu.forRole(maybeRole)
    val bar = new MenuBar(List(courses, books, confs, admin).flatten)
    
    bar.asHtml ++ <div class="pull-right">{ UserMenu.forRole(maybeRole).asHtml }</div>
  }
}

object AdminMenu {
  val updateUser = new MenuItem("Update User", "menu_updateUser", Some(controllers.users.routes.App.chooseUserToEdit().toString), Nil)  
  val sendTeacherActivation = new MenuItem("Send Teacher Activation",
      "menu_teacherActivation", Some(controllers.routes.MoveMe.sendTeacherActivation().toString), Nil)
  val sendGuardianActivation = new MenuItem("Send Guardian Activation",
      "menu_guardianActivation", Some(controllers.routes.MoveMe.sendGuardianActivation().toString), Nil)

  def forRole(maybeRole: Option[Role]) = maybeRole match {
    case Some(role) if (role.permissions().contains(User.Permissions.Manage)) => {
      val items = List(
        updateUser,
        sendTeacherActivation,
        sendGuardianActivation
      )
      Some(new MenuItem("Admin", "menu_admin", None, items))
    }
    case _ => None
  }
}