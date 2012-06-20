package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction

object Application extends Controller {

  def index() = DbAction { implicit req =>
    Ok(views.html.index())
  }

  def stub() = DbAction { implicit req => 
    Ok(views.html.stub())
  }
}
