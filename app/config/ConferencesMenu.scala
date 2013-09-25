package config

import controllers.users.{ MenuItem, MenuBar }
import models.conferences.Conferences
import models.users.Role

object ConferencesMenu {
  def SomeIf[T](cond: Boolean)(t: => T): Option[T] = if (cond) Some(t) else None

  def forRole(maybeRole: Option[Role]): Option[MenuItem] = maybeRole match {
    case None => None
    case Some(role) => {
      val perms = role.permissions()
      val items: List[MenuItem] = List(
        SomeIf(perms.contains(Conferences.Permissions.Manage)) {
          new MenuItem("Manage", "menu_manage", None, Nil, List(
            new MenuItem("List Events", "menu_listevents", Some(controllers.routes.Conferences.index.toString), Nil)
          ))
        }     
      ).flatten
      SomeIf(!items.isEmpty)(new MenuItem("Conferences", "menu_conferences", None, items))
    }
  }
}