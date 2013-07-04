package controllers.users

import scala.language.implicitConversions

import play.api.mvc.{ Action, Controller, Session }
import play.api.data.Forms._
import play.api.templates.Html
import scala.xml.Text
import play.api.mvc.Flash._
import scalajdo.DataStore
import com.google.inject.{ Inject, Singleton }
import config.users.Config
import forms.{ Binding, Form, InvalidBinding, ValidBinding }
import forms.fields._
import forms.validators._
import models.users.{ QUser, User, Visit }
import scala.xml.NodeSeq

import scalatags._

@Singleton
class App @Inject()(implicit config: Config) extends Controller { 
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
    Ok(templates.users.Login(config.mainTemplate, Binding(LoginForm)))
  }

  def loginP() = VisitAction { implicit request =>
    DataStore.execute { pm =>
      Binding(LoginForm, request) match {
        case ib: InvalidBinding => Ok(templates.users.Login(config.mainTemplate, ib))
        case vb: ValidBinding => {
          // set the session user
          request.visit.user = User.getByUsername(vb.valueOf(LoginForm.username))
          // set the session role
          request.visit.user.map(_.roles).getOrElse(Nil) match {
            // no roles attached to this user
            case Nil => Redirect(controllers.users.routes.App.login()).flashing("message" -> "That user has no active roles.")
            // there's at least one
            case persp :: rest => {
              // TODO: should we have a default role?
              // set the first one
              request.visit.role = Some(persp)
              request.visit.updateMenu()
              pm.makePersistent(request.visit)
              rest match {
                // there was only one
                case Nil => {
                  if (request.visit.redirectUrl.isDefined) Redirect(request.visit.redirectUrl.get)
                  else Redirect(config.defaultCall).flashing("message" -> "You have successfully logged in.")
                }
                // multiple roles
                case _ => Redirect(controllers.users.routes.App.chooseRole()).flashing("message" -> "Choose which role to use.")
              }
            }
          }
        }
      }
    }
  }

  class ChooseRoleForm(visit: Visit) extends Form {
    val role = new ChoiceField("role", visit.user.get.roles.toList.map(p => (p.displayNameWithRole, p)))

    def fields = List(role)
  }

  /**
   * Regex: /chooseRole
   *
   * helper method
   */

  def chooseRole() = Authenticated { implicit req =>
    Ok(templates.users.ChooseRole(config.mainTemplate, Binding(new ChooseRoleForm(req.visit))))
  }

  def chooseRoleP() = Authenticated { implicit req =>
    val form = new ChooseRoleForm(req.visit)
    Binding(form, req) match {
      case ib: InvalidBinding => Ok(templates.users.ChooseRole(config.mainTemplate, ib))
      case vb: ValidBinding => {
        req.visit.role = Some(vb.valueOf(form.role))
        //visit.updateMenu
        DataStore.pm.makePersistent(req.visit)
        Redirect(req.visit.redirectUrl.getOrElse(config.defaultCall)).flashing("message" -> "You have successfully logged in.")
      }
    }
  }

  /**
   * regex: logout
   *
   * Logs out user and displays home page with message "You have been logged out."
   */

  def logout = VisitAction { implicit req =>
    DataStore.execute { pm =>
      pm.deletePersistent(req.visit)
      Redirect(config.defaultCall).flashing("message" -> "You have been logged out.")
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
    val pwForm = new ChangePasswordForm(req.role.user)
    Ok(templates.users.ChangeSettings(config.mainTemplate, Binding(pwForm), Binding(ChangeTheme)))
  }

  /**
   * regex: /changePassword
   *
   * User inputs old password and new password and submits the form. The password their account then changes to their new selected password.
   */

  def changePassword = Authenticated { implicit req =>
    DataStore.execute { pm =>
      val user = req.role.user
      val form = new ChangePasswordForm(user)
      Binding(form, req) match {
        case ib: InvalidBinding => Ok(templates.users.ChangeSettings(config.mainTemplate, ib, Binding(ChangeTheme)))
        case vb: ValidBinding => {
          user.password = vb.valueOf(form.newPassword)
          pm.makePersistent(user)
          Redirect(config.defaultCall).flashing("message" -> "Settings successfully changed.")
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
      val user = req.role.user
      val pwForm = new ChangePasswordForm(user)
      Binding(ChangeTheme, req) match {
        case ib: InvalidBinding => Ok(templates.users.ChangeSettings(config.mainTemplate, Binding(pwForm), ib))
        case vb: ValidBinding => {
          user.theme = vb.valueOf(ChangeTheme.theme)
          pm.makePersistent(user)
          Redirect(config.defaultCall).flashing("message" -> "Settings successfully changed.")
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
      Ok(templates.users.ListUsers(config.mainTemplate, pm.query[User].orderBy(cand.last.asc, cand.first.asc).executeList()))
    }
  }
}