package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import math._
import play.api.templates.Html
import scala.collection.immutable.HashMap
import play.api.data._
import play.api.data.Forms._
import views._
import models._
import play.api.data.validation.Constraints._
import models.mastery.{QuizTemplate, QQuizTemplate}

object Mastery extends Controller {
  
  
  val ansForm = Form {
    	"answer" -> list(text)
  }
  
  def menuOfTests() = DbAction { implicit req =>
    implicit val pm = req.pm
    val cand = QQuizTemplate.candidate
    val quizzes = pm.query[QuizTemplate].executeList()
	  Ok(views.html.tatro.mastery.MasteryMenu(quizzes))
  }
  
  def displayQuiz(quizId: Long) = TODO /*DbAction { implicit req =>
    implicit val pm = req.pm
    val tempCand = QQuizTemplate.candidate
    val template = pm.query[QuizTemplate].filter(tempCand.id.eq(quizId)).executeOption.get
    
  }*/
  
  def checkAnswers(quizName: String) = DbAction { implicit req =>
      ansForm.bindFromRequest.fold(
    		  errors => BadRequest(views.html.tatro.mastery.MasteryQuiz(quizName, errors)),
    		  value => Ok(views.html.tatro.mastery.QuizSummary(value))
    )
  }

}