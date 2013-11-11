package controllers

import scala.xml.{ Elem, NodeSeq }
import models.users.Role
import controllers.courses.{ Menu => CoursesMenu }
import config.users.{ Menu => UserMenu }
import config.{ BookMenu, ConferencesMenu }
import controllers.users.{ MenuItem, MenuBar }

object Menu {
  def buildMenu(maybeRole: Option[Role]): NodeSeq = {
    val books: Option[MenuItem] = BookMenu.forRole(maybeRole)
    val courses: Option[MenuItem] = CoursesMenu.forRole(maybeRole)
    val confs: Option[MenuItem] = ConferencesMenu.forRole(maybeRole)
    val bar = new MenuBar(List(courses, books, confs).flatten)
    
    bar.asHtml ++ <div class="pull-right">{ UserMenu.forRole(maybeRole).asHtml }</div>
  }
}
