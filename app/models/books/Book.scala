package models.books

import config.users.UsesDataStore
import models.users.Permission

class Book {

}

object Book extends UsesDataStore {
  object Permissions {
    val Manage = Permission(classOf[Book], 0, "Manage", "can manage textbook system")
    val LookUp = Permission(classOf[Book], 1, "LookUp", "can look up info about books and users")
  }
}