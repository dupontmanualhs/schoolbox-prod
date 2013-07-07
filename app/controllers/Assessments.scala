package controllers

import play.api.mvc.Controller
import scala.util.Random
import play.api.data._
import play.api.data.Forms._

import com.google.inject.{ Inject, Singleton }

import scalajdo.DataStore

import controllers.users.VisitAction
import config.Config

@Singleton
class Assessments @Inject()(implicit config: Config) extends Controller {

  var rand = new Random
  var first = rand.nextInt(9) + 1
  var second = rand.nextInt(9) + 1
  var otherChoice = rand.nextInt(100)
  var ans = first + second

  def menu() = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      rand = new Random
      first = rand.nextInt(9) + 1
      second = rand.nextInt(9) + 1
      otherChoice = rand.nextInt(100)
      ans = first + second
      Ok(views.html.assessments.AssessmentsMenu("solve", ansForm, -1, None, first, second, ans, otherChoice))
    }
  }

  val ansForm = Form {
    "answer" -> number
  }

  def checkAnswer(temp: Int) = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      ansForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.assessments.AssessmentsMenu("solve", errors, -1, None, first, second, ans, otherChoice))
        },
        value => {
          if (value == ans) {
            Ok(views.html.assessments.AssessmentsMenu("solve", ansForm, -1, Some(0), first, second, ans, otherChoice))
          } else {
            Ok(views.html.assessments.AssessmentsMenu("solve", ansForm, -1, Some(1), first, second, ans, otherChoice))
          }
        })
    }
  }

  def newQuestion() = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      rand = new Random
      first = rand.nextInt(9) + 1
      second = rand.nextInt(9) + 1
      otherChoice = rand.nextInt(100)
      ans = first + second
      Ok(views.html.assessments.AssessmentsMenu("solve", ansForm, -1, None, first, second, ans, otherChoice))
    }
  }
}