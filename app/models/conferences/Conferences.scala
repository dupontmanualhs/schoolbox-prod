package models.conferences

import config.users.UsesDataStore
import models.users.Permission

class Conferences { }

object Conferences extends UsesDataStore {
  object Permissions {
    val Manage = Permission(classOf[Conferences], 0, "Manage", "can manage conference system")
  }
}