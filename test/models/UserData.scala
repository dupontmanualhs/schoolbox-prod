package models

import java.io.File

import util.DataStore
import models.users.User
import models.users.Gender

object UserData {
  def recreateDb() {
    deleteDbFile()
    create()
    DataStore.close()
  }
  
  def deleteDbFile() {
    val dbFile = new File("data.h2.db")
    dbFile.delete()
  }
  
  def create() {
    DataStore.withTransaction( pm => {
      val kermit = new User("frog", "Kermit", Some("The"), "Frog", None, Gender.MALE, "kermit@muppets.org", "piggy")
      val piggy = new User("moi", "Miss", None, "Piggy", None, Gender.FEMALE, "mp@muppets.org", "moi")
      pm.makePersistent(kermit)
      pm.makePersistent(piggy)
    })
  }

}