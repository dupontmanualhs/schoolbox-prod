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
        usernameAndPassword => {
          Redirect(routes.Application.index()).withSession(
              "username" -> usernameAndPassword._1).flashing("message" -> "You successfully logged in!")
        }
      )
    }
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action { implicit request =>
    Redirect(routes.Application.index()).withNewSession.flashing(
      "message" -> "You've been logged out..."
    )
  }
  
  def cpForm(user: User) = Form(tuple(
      "currentPassword" -> text,
      "newPassword" -> text,
      "verifyNewPassword" -> text
    ) verifying("Current password is incorrect.", _ match {
      case (cp, np, vnp) => User.authenticate(user, cp).isDefined
    }) verifying("New password and verify password must match.", _ match {
      case (cp, np, vnp) => np == vnp
    })
  )
  
  def changePassword = Action { implicit req => 
    if (!req.session.get("username").isDefined) {
      Redirect(routes.Users.login()).flashing("message" -> "You must log in to view that page.")
    } else {
      DataStore.withTransaction { implicit pm: ScalaPersistenceManager =>
        val user = User.getByUsername(req.session("username")).get
        Ok(html.users.changePassword(cpForm(user)))
      }
    }
  }
  
  def updatePassword = Action { implicit req =>
    if (!req.session.get("username").isDefined) {
      Redirect(routes.Users.login()).flashing("message" -> "You must log in to view that page.")
    } else {
      DataStore.withTransaction { implicit pm: ScalaPersistenceManager =>
        val user = User.getByUsername(req.session("username")).get
        cpForm(user).bindFromRequest.fold(
          formWithErrors => BadRequest(html.users.changePassword(formWithErrors)),
          cpNpAndVnp => {
            user.password = cpNpAndVnp._2
            pm.makePersistent(user)
            Redirect(routes.Application.index()).flashing("message" -> "Password successfully changed.")
          }
        )
      }
    }
  }
  
  def list = Action { implicit request =>
    DataStore.withTransaction { implicit pm =>
      val cand = QUser.candidate
      Ok(html.users.list(pm.query[User].orderBy(cand.last.asc, cand.first.asc).executeList()))
    }
  }
}