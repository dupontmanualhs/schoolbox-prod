package controllers

import play.api._
import play.api.mvc._

import util.{DataStore, ScalaPersistenceManager}

object Application extends Controller {
  
  def index() = Action { implicit request =>
    DataStore.withTransaction { implicit pm => 
      Ok(views.html.index("Your new application is ready."))
    }
  }
}