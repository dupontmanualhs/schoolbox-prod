/*package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import math._
import play.api.templates.Html
import scala.collection.immutable.HashMap
import play.api.data._
import play.api.data.Forms._

object Math extends Controller {
  
  val ansForm = Form {
    		"answer" -> text
  }
  
  //if isCorrect is 0, then there has been no attempt
  //if it is 1, then they got it right
  //if it is 2, then they got it wrong
  def randomProblem() = DbAction { implicit req =>
    val rand = math.Sum(math.RandomGen.getInteger(), math.RandomGen.getInteger())
    val ans = rand.evaluate(new HashMap[Expression, Value])
    Ok(views.html.math.randomProblem(rand.toLaTeX, ans.toLaTeX, ansForm, 0))
  }
  
  def checkAnswer(temp: String) = DbAction { implicit req =>
    val rand = math.Sum(math.RandomGen.getInteger(), math.RandomGen.getInteger())
    val ans = rand.evaluate(new HashMap[Expression, Value])
    ansForm.bindFromRequest.fold(
        errors => {
          BadRequest(views.html.math.randomProblem(rand.toLaTeX, ans.toLaTeX, errors, 2))
        },
        value => {
          if(temp == value) {
            Ok(views.html.math.randomProblem(rand.toLaTeX, ans.toLaTeX, ansForm, 1))
          } else {
            Ok(views.html.math.randomProblem(rand.toLaTeX, ans.toLaTeX, ansForm, 2))
          }
        }
    )
  }
}*/