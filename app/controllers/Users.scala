package controllers

import play.api.mvc.{Action, Controller, Session}
import play.api.data.Forms._
import play.api.templates.Html
import views.html
import models.users.User
import util.DataStore
import util.ScalaPersistenceManager
import models.users.QUser
import util.{DbAction, DbRequest}
import forms.Form
import forms.fields._
import forms.widgets._
import forms.{Binding, InvalidBinding, ValidBinding}
import forms.validators.ValidationError
import forms.validators.Validator

object Users extends Controller {  
  /**
   * Login page.
   */
  def login = DbAction { implicit request =>
    object LoginForm extends Form {
      val username = new TextField("username")
      val password = new PasswordField("password")
      
      def fields = List(username, password)
      
      override def validate(vb: ValidBinding): ValidationError = {
        DataStore.withTransaction { implicit pm => 
          User.authenticate(vb.valueOf(username), vb.valueOf(password)) match {
            case None => ValidationError("Incorrect username or password.")
            case Some(user) => ValidationError(Nil)
          }
        }
      }
    }
    if (request.method == "GET") {
      Ok(html.users.login(Binding(LoginForm)))
    } else {
      Binding(LoginForm, request) match {
        case ib: InvalidBinding => Ok(html.users.login(ib))
        case vb: ValidBinding => {
          // set the session user
          request.visit.user = User.getByUsername(vb.valueOf(LoginForm.username))
          // set the session perspective
          request.visit.user.map(_.perspectives).getOrElse(Nil) match {
            // no perspectives attached to this user
            case Nil => Redirect(routes.Application.index()).flashing("message" -> "That user has no active perspectives.")
            // there's only one
            case persp :: Nil => {
              // set it in the session
              request.visit.perspective = Some(persp)
              Redirect(routes.Application.index()).flashing("message" -> "You have successfully logged in.")
            }
            // multiple perspectives
            case _ => Redirect(routes.Users.choosePerspective()).flashing("message" -> "Choose which perspective to use.")
          }
        }
      }
    }
  }
  
  def choosePerspective = TODO

  /**
   * Logout and clean the session.
   */
  def logout = DbAction { implicit request =>
    request.pm.deletePersistent(request.visit)
    Redirect(routes.Application.index()).flashing(
      "message" -> "You've been logged out..."
    )
  }
  
  class ChangePasswordForm(user: User) extends Form {
    val currentPassword = new PasswordField("currentPassword") {
      override def validators: List[Validator[String]] = {
        List(Validator((str: String) => {
          ValidationError(
            if (User.authenticate(user, str).isDefined) Nil
            else List("Current password is incorrect.")
          )
        }))
      }
    }
    val newPassword = new PasswordField("newPassword")
    val verifyNewPassword = new PasswordField("verifyNewPassword")
    
    def fields = List(currentPassword, newPassword, verifyNewPassword)
    
    override def validate(vb: ValidBinding): ValidationError = {
      if (vb.valueOf(newPassword) != vb.valueOf(verifyNewPassword)) {
        ValidationError("New password and verify password must match.")
      } else ValidationError(Nil)
    }
    
  }
  
  def changePassword = DbAction { implicit req => 
    if (!req.session.get("username").isDefined) {
      Redirect(routes.Users.login()).flashing("message" -> "You must log in to view that page.")
    } else {
      val user = User.getByUsername(req.session("username"))(req.pm).get
      val form = new ChangePasswordForm(user)
      if (req.method == "GET") {
        Ok(html.users.changePassword(Binding(form)))
      } else {
        Binding(form, req) match {
          case ib: InvalidBinding => Ok(html.users.changePassword(ib))
          case vb: ValidBinding => {
            user.password = vb.valueOf(form.newPassword)
            req.pm.makePersistent(user)
            Redirect(routes.Application.index()).flashing("message" -> "Password successfully changed.")
          }
        }
      }
    }
  }
  
  def list = DbAction { implicit request =>
    val cand = QUser.candidate
    Ok(html.users.list(request.pm.query[User].orderBy(cand.last.asc, cand.first.asc).executeList()))
  }
}