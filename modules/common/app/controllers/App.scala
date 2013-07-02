package controllers

import play.api.mvc._

object App extends Controller {
  def index() = Action { implicit req =>
      Ok(views.html.index())
  }

  def stub() = Action { implicit req =>
      Ok(views.html.stub())
  }
}