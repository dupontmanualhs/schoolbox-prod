package config

import controllers.users.{ MenuItem, MenuBar }

object LockerMenu {
  val currloc = new MenuItem("My Locker", "menu_lockerStatus", Some(controllers.routes.Lockers.getMyLocker().toString), Nil)
  val findlocnum = new MenuItem("Find Locker by Number", "menu_lockerNum", Some(controllers.routes.Lockers.lockerByNumber().toString), Nil)
  val locsearch = new MenuItem("Find Locker", "menu_lockerSearch", Some(controllers.routes.Lockers.lockerSearch().toString), Nil)
  val locsched = new MenuItem("Find Locker by Class", "menu_lockerClass", Some(controllers.routes.Lockers.schedule().toString), Nil)
}