package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import controllers.users.VisitAction
import com.google.inject.{ Inject, Singleton }
import config.Config

@Singleton
class App @Inject()(implicit config: Config) extends Controller {
  def index() = VisitAction { implicit req =>
      Ok(templates.Index())
  }
  
  def stub() = VisitAction { implicit req =>
      Ok(templates.Stub())
  }
}


