package config.users

import controllers.users.{ MenuItem, MenuBar }
import models.users.Role

object Menu {
  val login = new MenuItem("Log in", "menu_login", Some(controllers.users.routes.App.login().toString), Nil)
  val logout = new MenuItem("Log out", "menu_logout", Some(controllers.users.routes.App.logout().toString), Nil)
  val settings = new MenuItem("Change My Password", "menu_changePassword", Some(controllers.users.routes.App.changePassword().toString), Nil)
  val changeRole = new MenuItem("Change Role", "menu_changeRole", Some(controllers.users.routes.App.chooseRole().toString), Nil)
  val changeOtherPassword = new MenuItem("Change a User's Password", "menu_changeUserPassword", Some(controllers.users.routes.App.changeOtherPassword().toString), Nil)
  
  
  def forRole(maybeRole: Option[Role]): MenuBar = maybeRole match {
    case None => new MenuBar(List(login))
    case Some(role) => {
      val items = if (role.user.roles.size > 1) List(changeRole, settings, logout)
         else List(settings, logout)
      new MenuBar(List(new MenuItem(role.displayNameWithRole, "menu_name", None, items)))    
    }
  } 
}