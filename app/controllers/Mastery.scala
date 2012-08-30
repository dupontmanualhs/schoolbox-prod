package controllers

import play.api._
import scala.util.Random
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
import play.api.mvc.{Action, Controller, Session}
import play.api.data.Forms._
import play.api.templates.Html
import views.html
import util.DataStore
import util.ScalaPersistenceManager
import util.{DbAction, DbRequest, Menu}
import forms.Form
import forms.fields._
import forms.widgets._
import forms.{Binding, InvalidBinding, ValidBinding}
import forms.validators.ValidationError
import forms.validators.Validator
import util.Authenticated
import scala.xml._
import scala.xml

object Mastery extends Controller {
  
  def menuOfTests() = DbAction { implicit req =>
  	//TODO get list of masteries
    val cand = QQuiz.candidate()
    val listOfMasteries=req.pm.query[Quiz].orderBy(cand.name.asc).executeList()
    val table: List[NodeSeq] = listOfMasteries.map { q =>
      <tr>
      	<td>
      		{<a href={controllers.routes.Mastery.getDisplayQuiz(q.id).url}>q.toString</a>}
      	</td>
      </tr>
    }
    Ok(html.tatro.mastery.MasteryQuizMenu(table)) // this is a fake error -.-
  }
  
  def getDisplayQuiz(quizId: Long) = DbAction { implicit req =>
  	implicit val pm: ScalaPersistenceManager = req.pm
  	displayQuiz(Quiz.getById(quizId))
  }
  
  def displayQuiz(maybeQuiz: Option[Quiz])(implicit req: DbRequest[_]): PlainResult = {
    if(!maybeQuiz.isDefined){
      NotFound(views.html.notFound("The quiz of which you are seeking no longer exists."))
    } else {
      val quiz=maybeQuiz.get
      val sections = quiz.sections
      var SAndQ: Map[Section, List[Question]] = Map()
      for(s <- sections){
        SAndQ += (s -> s.randomQuestions)
      }
      Ok(html.tatro.mastery.displayMastery(quiz, SAndQ))
    }
  }
  
  //def checkAnswers(quizName: String) = DbAction { implicit req =>
  //}

}




