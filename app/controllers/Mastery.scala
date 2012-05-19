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

object Mastery extends Controller {
  
  
  val ansForm = Form {
    mapping(
    		"answers" -> seq(
    				mapping(
    		        "answer" -> text
    		        )(Answer.apply)(Answer.unapply)
    		    )
    		)(Answers.apply)(Answers.unapply)
  }
  
  def menuOfTests() = DbAction { implicit req =>
	  Ok(views.html.tatro.mastery.MasteryMenu())
  }
  
  def displayQuiz(quizName: String) = DbAction { implicit req =>
      Ok(views.html.tatro.mastery.MasteryQuiz(quizName, ansForm))
  }
  
  def checkAnswers(quizName: String) = DbAction { implicit req =>
      ansForm.bindFromRequest.fold(
    		  errors => BadRequest(views.html.tatro.mastery.MasteryQuiz(quizName, errors)),
    		  answers => Ok(views.html.tatro.mastery.QuizSummary(answers))
    )
  }

}