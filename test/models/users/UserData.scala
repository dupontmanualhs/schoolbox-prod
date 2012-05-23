package models.users

import util.ScalaPersistenceManager

object UserData {  
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    val kermit = new User("frog", "Kermit", Some("The"), "Frog", None, Gender.MALE, "kermit@muppets.org", "piggy")
    val piggy = new User("moi", "Miss", None, "Piggy", None, Gender.FEMALE, "mp@muppets.org", "moi")
    pm.makePersistent(kermit)
    pm.makePersistent(piggy)
  }
}