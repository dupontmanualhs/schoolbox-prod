package controllers

import scala.xml.NodeSeq
import play.api.mvc.Controller
import play.api.templates.Html
import play.api.data._
import play.api.data.Forms._
import com.google.inject.{ Inject, Singleton }

import models.assignments.{ DbQuestion, QDbQuestion }
import models.assignments.Task
import models.assignments.QTask

import config.Config
import config.users.UsesDataStore

import controllers.users.VisitAction

@Singleton
class Assignments @Inject()(implicit config: Config) extends Controller with UsesDataStore {
  def doTask(taskId: Long) = VisitAction { implicit req =>
    dataStore.execute { implicit pm =>
      pm.query[Task].filter(QTask.candidate.id.eq(taskId)).executeOption() match {
        case None => NotFound("no task with that id")
        case Some(task) => Ok(views.html.assignments.questions(task))
      }
    }
  }

  def check(taskId: Long) = TODO

}