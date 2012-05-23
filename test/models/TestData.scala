package models

import java.io.File
import util.DataStore

import models.users.UserData
import models.assignments.AssignmentData
import models.mastery.QuizData

object TestData {
  def load(debug: Boolean = false) {
    val dbFile = new File("data.h2.db")
    dbFile.delete()
    DataStore.withManager { implicit pm =>
      UserData.load(debug)
      AssignmentData.load(debug)
      QuizData.load(debug)
      pm.close()
    }
  }

}