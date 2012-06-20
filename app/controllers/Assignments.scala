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


object Assignments extends Controller {
  def allQuestions() = DbAction { implicit req =>
    implicit val pm = req.pm
    val qs: List[DbQuestion] = pm.query[DbQuestion].executeList()
    Ok(views.html.assignments.questions(qs))
  }
}