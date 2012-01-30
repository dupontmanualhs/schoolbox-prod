package controllers

import play.api.mvc.{Action, Controller, Session}
import play.api.templates.Html
import play.api.data.Form
import play.api.data.Forms.{text, tuple}
import views.html
import models.users.User


object Users extends Controller {
  val loginForm = Form(
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
    Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      usernameAndPassword => Redirect(routes.Application.index).withSession("username" -> usernameAndPassword._1)
    )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Users.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
  
  def userInfo(implicit session: Session): Html = {
    val maybeUser: Option[User] = session.get("username") match {
      case Some(username) => User.getByUsername(username)
      case None => None
    }
    html.userInfo(maybeUser)
  }
}