package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import math._
import play.api.templates.Html
import scala.collection.immutable.HashMap

object Math extends Controller {
  def randomProblem = DbAction { implicit req =>
    val rand = math.MathSum(math.MathRandom.getRandomInteger, math.MathRandom.getRandomInteger)
    val ans = rand.evaluate(new HashMap[MathExpression, MathValue])
    Ok(views.html.math.randomProblem(rand.toLaTeX, ans.toLaTeX))
  }
}