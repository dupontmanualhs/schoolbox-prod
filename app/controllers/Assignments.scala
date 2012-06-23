package controllers

import scala.xml.NodeSeq
import play.api.mvc.Controller
import util.DbAction
import util.ScalaPersistenceManager
import util.DbRequest
import play.api.templates.Html
import play.api.data._
import play.api.data.Forms._
import models.assignments.{DbQuestion, QDbQuestion}
import models.assignments.Task
import models.assignments.QTask


object Assignments extends Controller {
  def doTask(taskId: Long) = DbAction { implicit req =>
    implicit val pm = req.pm
    pm.query[Task].filter(QTask.candidate.id.eq(taskId)).executeOption() match {
      case None => NotFound("no task with that id")
      case Some(task) => Ok(views.html.assignments.questions(task))
    }
  }
  
  def check(taskId: Long) = DbAction { implicit req => 
    implicit val pm = req.pm
    Task.getById(taskId) match {
      case None => NotFound("no task with that id")
      case Some(task) => {
        taskForm.bindFromRequest.fold(
          formWithErrors => NotFound("really, errors?"),
          answers => Ok(views.html.assignments.check(answers))
        )        
      }
    }
  }
}