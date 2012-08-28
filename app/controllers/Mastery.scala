package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import forms._
import forms.fields._
import forms.validators.Validator
import forms.validators.ValidationError
import play.api.mvc.PlainResult
import util.DbRequest
import math._
import play.api.templates.Html
import scala.collection.immutable.HashMap
import play.api.data._
import play.api.data.Forms._
import views._
import models._
import play.api.data.validation.Constraints._
import models.mastery._
import forms.Form
import forms.fields._
import models.assignments.questions.FillBlanks
import models.mastery._

object Mastery extends Controller {
  
  def menuOfTests() = DbAction { implicit req =>
  	//TODO get list of masteries
    implicit val pm = req.pm
    val cand=QQuiz
    val ListOfMasteries = List[Quiz]_
    OK(html.tatro.mastery.MasteryQuizMenu(ListOfMasteries))
  }
  
  //def displayQuiz(quizId: Long) = DbAction { implicit req =>
  //}
  
  //def checkAnswers(quizName: String) = DbAction { implicit req =>
  //}

}