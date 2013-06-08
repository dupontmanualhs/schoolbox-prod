package controllers

import scala.xml.NodeSeq
import play.api.mvc.{ Action, Controller }
import play.api.templates.Html
import play.api.data._
import play.api.data.Forms._
import models.assignments.{ DbQuestion, QDbQuestion }
import models.assignments.Task
import models.assignments.QTask

import scalajdo.DataStore

object Assignments extends Controller {
  def doTask(taskId: Long) = Action { implicit req =>
    DataStore.execute { implicit pm =>
      pm.query[Task].filter(QTask.candidate.id.eq(taskId)).executeOption() match {
        case None => NotFound("no task with that id")
        case Some(task) => Ok(views.html.assignments.questions(task))
      }
    }
  }

  def check(taskId: Long) = TODO

}