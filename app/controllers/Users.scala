package controllers

import play.api.mvc.{Action, Controller, Session}
import play.api.templates.Html
import play.api.data.Form
import play.api.data.Forms.{text, tuple}
import views.html
import models.users.User
import util.DataStore
import util.ScalaPersistenceManager
import models.users.QUser

object Users extends Controller {
  def loginForm(implicit pm: ScalaPersistenceManager) = Form(
    tuple("username" -> text,
       "password" -> text
    ) verifying ("Incorrect username or password.", result => result match {
      case (username, password) => User.authenticate(username, password).isDefined
    })
  )
  
  /**
   * Login page.
   */
  def login = Action { implicit request =>
    DataStore.withTransaction { implicit pm: ScalaPersistenceManager => 
      Ok(html.login(loginForm))
    }
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    DataStore.withTransaction { implicit pm: ScalaPersistenceManager => 
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        usernameAndPassword => Redirect(routes.Application.index()).withSession("username" -> usernameAndPassword._1)
      )
    }
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action { implicit request =>
    Redirect(routes.Users.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
  
  def list = Action { implicit request =>
    DataStore.withTransaction { implicit pm =>
      val cand = QUser.candidate
      Ok(html.users.list(pm.query[User].orderBy(cand.last.asc, cand.first.asc).executeList()))
    }
  }
}