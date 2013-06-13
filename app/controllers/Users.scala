package controllers

import play.api.mvc.{ Action, Controller, Session }
import play.api.data.Forms._
import play.api.templates.Html
import views.html
import models.users.User
import models.users.QUser
import forms.Form
import forms.fields._
import forms.widgets._
import forms.{ Binding, InvalidBinding, ValidBinding }
import forms.validators.ValidationError
import forms.validators.Validator
import scala.xml.Text
import play.api.mvc.Flash._
import util.VisitAction
import scalajdo.DataStore
import models.users.Visit
import util.Authenticated

object Users extends Controller {
  object LoginForm extends Form {
    val username = new TextField("username")
    val password = new PasswordField("password")

    def fields = List(username, password)

    override def validate(vb: ValidBinding): ValidationError = {
      DataStore.execute { pm =>
        User.authenticate(vb.valueOf(username), vb.valueOf(password)) match {
          case None => ValidationError("Incorrect username or password.")
          case Some(user) => ValidationError(Nil)
        }
      }
    }
  }

    /**
   * Regex: /login
   * 
   * Displays login form in which users can enter username and password in order to login.
   */
  
  def login() = VisitAction { implicit request =>
    DataStore.execute { pm =>
      val visit = Visit.getFromRequest(request)
      if (request.method == "GET") {
        Ok(html.users.login(Binding(LoginForm)))
      } else {
        Binding(LoginForm, request) match {
          case ib: InvalidBinding => Ok(html.users.login(ib))
          case vb: ValidBinding => {
            // set the session user
            visit.user = User.getByUsername(vb.valueOf(LoginForm.username))
            // set the session perspective
            visit.user.map(_.perspectives).getOrElse(Nil) match {
              // no perspectives attached to this user
              case Nil => Redirect(controllers.routes.Application.index()).flashing("message" -> "That user has no active perspectives.")
              // there's only one
              case List(persp) => {
                // set it in the session
                visit.perspective = Some(persp)
                visit.updateMenu
                pm.makePersistent(visit)
                Redirect(visit.redirectUrl.getOrElse(controllers.routes.Application.index())).flashing("message" -> "You have successfully logged in.")
              }
              // multiple perspectives
              case _ => Redirect(controllers.routes.Users.choosePerspective()).flashing("message" -> "Choose which perspective to use.")
            }
          }
        }
      }
    }
  }

  /**
   * Regex: /choosePerspective
   * 
   * helper method
   */
  
  def choosePerspective = Authenticated { implicit req =>
    DataStore.execute { pm =>
      val visit = Visit.getFromRequest(req)
      object ChoosePerspectiveForm extends Form {
        val perspective = new ChoiceField("perspective", visit.user.get.perspectives.map(p => (p.displayNameWithRole, p)))

        def fields = List(perspective)
      }
      if (req.method == "GET") {
        Ok(html.users.choosePerspective(Binding(ChoosePerspectiveForm)))
      } else {
        Binding(ChoosePerspectiveForm, req) match {
          case ib: InvalidBinding => Ok(html.users.choosePerspective(ib))
          case vb: ValidBinding => {
            visit.perspective = Some(vb.valueOf(ChoosePerspectiveForm.perspective))
            visit.updateMenu
            pm.makePersistent(visit)
            Redirect(routes.Application.index()).flashing("message" -> "You have successfully logged in.")
          }
        }
      }
    }
  }

  /**
   * regex: logout
   * 
   * Logs out user and displays home page with message "You have been logged out."
   */
  
  def logout = VisitAction { implicit request =>
    DataStore.execute { pm =>
      val visit = Visit.getFromRequest(request)
      pm.deletePersistent(visit)
      Redirect(controllers.routes.Application.index()).flashing("message" -> "You have been logged out.")
    }
  }

  class ChangePasswordForm(user: User) extends Form {
    val currentPassword = new PasswordField("currentPassword") {
      override def validators: List[Validator[String]] = {
        List(Validator((str: String) => {
          ValidationError(
            if (User.authenticate(user, str).isDefined) Nil
            else List(Text("Current password is incorrect.")))
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

  object ChangeTheme extends Form {
    val theme = new ChoiceField("theme", List(("Default", "default"), ("Night", "night"), ("Cyborg", "cyborg")))

    def fields = List(theme)
  }

  /**
   * regex: /settings
   * 
   * Displays page with the form to change the user's password and the form to change the user's theme.
   */
  
  def settings = Authenticated { implicit req =>
    val visit = Visit.getFromRequest(req)
    val user = visit.user.get
    val pwForm = new ChangePasswordForm(user)
    Ok(html.users.settings(Binding(pwForm), Binding(ChangeTheme)))
  }

  /**
   * regex: /changePassword
   * 
   * User inputs old password and new password and submits the form. The password their account then changes to their new selected password.
   */
  
  def changePassword = Authenticated { implicit req =>
    DataStore.execute { pm =>
      val user = Visit.getFromRequest(req).user.get // TODO: this scares me -- we shouldn't get here if the user is None, but...
      val form = new ChangePasswordForm(user)
      Binding(form, req) match {
        case ib: InvalidBinding => Ok(html.users.settings(ib, Binding(ChangeTheme)))
        case vb: ValidBinding => {
          user.password = vb.valueOf(form.newPassword)
          pm.makePersistent(user)
          Redirect(routes.Application.index()).flashing("message" -> "Settings successfully changed.")
        }
      }
    }
  }
  
  /**
   * regex: /changeTheme
   * 
   * User chooses from drop-down menu and this changes the theme to that selection.
   */

  def changeTheme = Authenticated { implicit req =>
    DataStore.execute { pm =>
      val user = Visit.getFromRequest(req).user.get
      val pwForm = new ChangePasswordForm(user)
      Binding(ChangeTheme, req) match {
        case ib: InvalidBinding => Ok(html.users.settings(Binding(pwForm), ib))
        case vb: ValidBinding => {
          user.theme = vb.valueOf(ChangeTheme.theme)
          pm.makePersistent(user)
          Redirect(routes.Application.index()).flashing("message" -> "Settings successfully changed.")
        }
      }
    }
  }

  //def settingsPage = 

  /**
   * regex: /listUsers
   * 
   * Helper Method
   */
  
  def list = VisitAction { implicit request =>
    DataStore.execute { pm =>
      val cand = QUser.candidate
      Ok(html.users.list(pm.query[User].orderBy(cand.last.asc, cand.first.asc).executeList()))
    }
  }
}