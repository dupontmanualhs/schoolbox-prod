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

object Mastery extends Controller {
  
  def menuOfTests() = DbAction { implicit req =>
	  Ok(views.html.tatro.mastery.MasteryMenu())
  }

}