package controllers

import play.api._
import util.Helpers.mkNodeSeq
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
    val pm=req.pm
    val cand = QQuiz.candidate()
    val listOfMasteries=pm.query[Quiz].orderBy(cand.name.asc).executeList()
    val hasQuizzes = listOfMasteries.size != 0
    val table: List[NodeSeq] = listOfMasteries.map { q =>
      <tr>
      	<td>{ linkToQuiz(q) }</td>
      </tr>
    }
    
    Ok(html.tatro.mastery.MasteryQuizMenu(table, hasQuizzes)) // this is a fake error -.-
  }
  
  def linkToQuiz(quiz: Quiz): NodeSeq = {
    val link = controllers.routes.Mastery.getDisplayQuiz(quiz.id)
    <a href={link.url}>{quiz.toString}</a>
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
      val sections: List[QuizSection] = quiz.sections
      if(sections==null || sections.isEmpty){
        NotFound(views.html.notFound("There are no sections :("))
      } else {
      var SAndQ: Map[QuizSection, List[Question]] = Map()
      for(s <- sections){
        SAndQ += (s -> s.randomQuestions)
      }
      Ok(html.tatro.mastery.displayMastery(quiz, SAndQ))
    }
    }
  }
  def testDataBase() = DbAction { implicit req =>
    val pm=req.pm
    val quizCand = QQuiz.candidate()
    val listOfMasteries=pm.query[Quiz].orderBy(quizCand.name.asc).executeList()
    val listOfSections=pm.query[models.mastery.QuizSection].executeList()
    val listOfQSets=pm.query[QuestionSet].executeList()
    val listOfQuestions=pm.query[Question].executeList()
    Ok(html.tatro.mastery.testData(listOfMasteries, listOfSections, listOfQSets, listOfQuestions))  
  }
  
  
  //def checkAnswers(quizName: String) = DbAction { implicit req =>
  //}

}




