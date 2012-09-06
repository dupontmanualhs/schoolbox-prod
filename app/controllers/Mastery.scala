package controllers

import play.api._
import util.Helpers.mkNodeSeq
import scala.util.Random
import play.api.mvc._
import util.{ DataStore, ScalaPersistenceManager }
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
import play.api.mvc.{ Action, Controller, Session }
import play.api.data.Forms._
import play.api.templates.Html
import views.html
import util.DataStore
import util.ScalaPersistenceManager
import util.{ DbAction, DbRequest, Menu }
import forms.Form
import forms.fields._
import forms.widgets._
import forms.{ Binding, InvalidBinding, ValidBinding }
import forms.validators.ValidationError
import forms.validators.Validator
import util.Authenticated
import scala.xml._
import scala.xml
import util.Helpers.camel2TitleCase

object Mastery extends Controller {

  def menuOfTests() = DbAction { implicit req =>
    //TODO get list of masteries
    val pm = req.pm
    val cand = QQuiz.candidate()
    val listOfMasteries = pm.query[Quiz].orderBy(cand.name.asc).executeList()
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
    <a href={ link.url }>{ quiz.toString }</a>
  }

  def getDisplayQuiz(quizId: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    displayQuiz(Quiz.getById(quizId))
  }

  def displayQuiz(maybeQuiz: Option[Quiz])(implicit request: DbRequest[_]): PlainResult = {
    if (!maybeQuiz.isDefined) {
      NotFound(views.html.notFound("The quiz of which you are seeking no longer exists."))
    } else {
      val quiz = maybeQuiz.get
      val sections: List[QuizSection] = quiz.sections
      if (sections == null || sections.isEmpty) {
        NotFound(views.html.notFound("There are no sections :("))
      } else {
        var SAndQ: Map[QuizSection, List[Question]] = Map()
        for (s <- sections) {
          SAndQ += (s -> s.randomQuestions) //SAndQ id a map of Section -> List[Question] 
        }
      	
        //MasteryForm uses SAndQ
        
        object MasteryForm extends Form {
        
          var sectionInstructionList: List[String] = {
            var tempList = List[String]()
            for (sq <- SAndQ) {
              tempList = sq._1.toString :: tempList
            }
            tempList
          }
          def getsectionInstructions = {sectionInstructionList}
          val fields2: List[List[forms.fields.Field[_]]] = {
            var tempList = List[List[forms.fields.Field[_]]]()
            for (sq <- SAndQ) {
              var tempList2 = List[forms.fields.Field[_]]()
              for (q <- sq._2) {
                tempList2 = (new TextField(q.toString())) :: tempList2
              }
              tempList = tempList2 :: tempList
            }
            tempList
          }
          
          def getfields: List[List[forms.fields.Field[_]]] = { fields2 }
          
          
          override def asHtml(bound: Binding): Elem = {
            <form method={ method }>
              <table>
                { if (bound.formErrors.isEmpty) NodeSeq.Empty else <tr><td></td><td>{ bound.formErrors.asHtml }</td><td></td></tr> }
                {
                  fields2.flatMap(q => {
                    //TODO: Make it so the strings in the list "sectionInstructionList" appear
                	  <tr>
                	  <td>{ sectionInstructionList.apply(0).toString }</td>
                	  </tr>
                    sectionInstructionList = sectionInstructionList.drop(1)
                    q.flatMap(f => {
                      val name = f.name
                      val label = f.label.getOrElse(camel2TitleCase(f.name))
                      val labelName = if (label == "") "" else {
                        if (":?.!".contains(label.substring(label.length - 1, label.length))) label
                        else label + labelSuffix
                      }
                      val labelPart =
                        if (labelName != "") f.labelTag(this, Some(labelName)) ++ Text(" ")
                        else NodeSeq.Empty
                      val errorList = bound.fieldErrors.get(name).map(_.asHtml)
                      <tr>
                        <td>{ labelPart }</td>
                        <td>{ f.asWidget(bound) }</td>
                        {
                          if (bound.hasErrors) <td>{ errorList.getOrElse(NodeSeq.Empty) }</td>
                          else NodeSeq.Empty
                        }
                      </tr>
                    })
                  }).toList
                }
              </table>
              <input type="submit"/>
            </form>
          }
          val fields = List[forms.fields.Field[_]]()
        }
          
        if (request.method == "GET") Ok(html.tatro.mastery.displayMastery(quiz, Binding(MasteryForm)))
        
        //TODO: Figure out a way so that even though I get the sections and the 
        //questions randomly, they don't change b/t the time they are displayed
        //and the time I go to retrieve them
        else Binding(MasteryForm, request) match {
            case ib: InvalidBinding => Ok(html.tatro.mastery.displayMastery(quiz, ib)) // there were errors
            case vb: ValidBinding => {
              var listOfValuesOfForms = List[List[String]]()
              for (q <- MasteryForm.getfields) {
                var tempList = List[String]()
                for (f <- q) {
                  val oneSecsForms = (MasteryForm.getfields.apply(MasteryForm.getfields.indexOf(q)))
                  tempList = vb.valueOf(oneSecsForms.apply(oneSecsForms.indexOf(f))).toString :: tempList
                }
                listOfValuesOfForms = tempList :: listOfValuesOfForms
              }
              // do whatever you want with the values now (notice they're typesafe!)
              Redirect(routes.Mastery.testDataBase())
            }
        }
      }
    }
  }

  def testDataBase() = DbAction { implicit req =>
    //val pm=req.pm
    val quizCand = QQuiz.candidate()
    val listOfMasteries = req.pm.query[Quiz].orderBy(quizCand.name.asc).executeList()
    val listOfSections = req.pm.query[models.mastery.QuizSection].executeList()
    val listOfQSets = req.pm.query[QuestionSet].executeList()
    val listOfQuestions = req.pm.query[Question].executeList()
    Ok(html.tatro.mastery.testData(listOfMasteries, listOfSections, listOfQSets, listOfQuestions))
  }

  //def checkAnswers(quizName: String) = DbAction { implicit req =>
  //}

}




