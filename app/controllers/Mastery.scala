package controllers

import play.api._
import models.users.Visit
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
        var LQ = List[Question]()
        if (request.method == "GET") {
          for (s <- sections) {
            SAndQ += (s -> s.randomQuestions) //SAndQ id a map of Section -> List[Question] 
          }
          SAndQ.keys.foreach { k =>
            for (q <- SAndQ(k)) {
              LQ = q :: LQ
            }
          }
          request.visit.updateListOfQuestions(LQ)
        } else {
          LQ = request.visit.SAndQ
        }

        //MasteryForm uses SAndQ
        if (request.method == "GET") {
          object MasteryForm extends Form {

            var sectionInstructionList: List[String] = {
              var tempList = List[String]()
              for (sq <- SAndQ) {
                tempList = sq._1.toString :: tempList
              }
              tempList
            }
            def getsectionInstructions = { sectionInstructionList }
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
          Ok(html.tatro.mastery.displayMastery(quiz, Binding(MasteryForm)))
        } else {
          object MasteryForm extends Form {
            val fields: List[forms.fields.Field[_]] = {
              var tempList = List[forms.fields.Field[_]]()
              for (q <- LQ) {
                tempList = new TextField(q.toString()) :: tempList
              }
              tempList
            }
          }
          Binding(MasteryForm, request) match {
            case ib: InvalidBinding => Ok(html.tatro.mastery.displayMastery(quiz, ib)) // there were errors
            case vb: ValidBinding => {
              var listAnswers = List[String]()
              for (f <- MasteryForm.fields) {
                listAnswers = vb.valueOf(f).toString() :: listAnswers
              }
              //save these in Visit
              request.visit.updateQuiz(quiz)
              request.visit.updateLQ(LQ)
              request.visit.updateLA(listAnswers)
              Redirect(routes.Mastery.checkAnswers())
            }
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

  def checkAnswers() = DbAction { implicit request =>
    val quiz: Quiz = request.visit.getQuiz
    val questionList: List[Question] = request.visit.getLQ
    val answerList: List[String] = request.visit.getLA
    var ScoreInTF = List[Boolean]()
    var questionList2 = questionList
    for (a <- answerList) {
      val c = getRidOfExtraMultiplication(getRidOfSpaces(a))
      val AllComboAnswers: List[String] = getAllCombinationsOfEquivalentAnswers(c)
      var alreadyCorrect = false
      for (ls <- AllComboAnswers) {
        if (ls.equalsIgnoreCase(questionList2.apply(0).getAnswer) && !alreadyCorrect) {
          ScoreInTF = true :: ScoreInTF
          alreadyCorrect = true
        } else {
          ScoreInTF = false :: ScoreInTF
        }
        questionList2.drop(1)
      }
    }
    var numberWrong = 0
    for (correct <- ScoreInTF) {
      if (!correct) numberWrong = numberWrong + 1
    }
    Ok(html.tatro.mastery.displayScore(quiz, questionList, answerList, ScoreInTF, numberWrong))
  }

  def getRidOfSpaces(s: String) = {
    """ """.r.replaceAllIn(s, "")
  }
  def getRidOfExtraMultiplication(s: String) = {
    var rString1 = ""
    for (n <- 1 to s.length - 1) {
      val c = s.charAt(n)
      val pc = s.charAt(n - 1)
      if (pc == '*') {
        if (c != '(') {
          if (!(n != 1 && ((s.charAt(n - 2).isDigit && c.isLetter) || (s.charAt(n - 2).isLetter && c.isDigit)))) {
            rString1 = rString1 + pc
          }
        }
      } else {
        rString1 = rString1 + pc
      }
      if(n == s.length()-1 && (c.isDigit || c.isLetter || c == ')')){
        rString1 = rString1 + c
      }
    }
    System.out.println(rString1)
    rString1
  }
  
  def changeRadToR(s: String) = """rad(""".r.replaceAllIn(s, "r(")
  
  def encloseExponents(s: String) = {
    var rs = ""
    for(n <- 1 to s.length - 1){
      val c = s.charAt(n)
      var inExponent = false
      if(n != s.length - 1 && c == '^' && s.charAt(n+1) != '('){
        rs = rs + "^("
        inExponent = true
      } else if(true){}
    }
    rs
  }
  
  def changeToInterpreterSyntax(s: String) = {
    var rs = getRidOfSpaces(s)
    rs = getRidOfExtraMultiplication(rs)
    rs = changeRadToR(rs)
    rs = encloseExponents(rs)
  }
  def getAllCombinationsOfEquivalentAnswers(s: String) = {
    List(s)
  }
}




