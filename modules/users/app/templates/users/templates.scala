package templates.users

import scala.language.implicitConversions
import scalatags._
import play.api.templates.Html

import forms.{ Binding, FormCall }

import config.users.Config
import controllers.users.VisitRequest
import models.users.User

object Login {
  def apply(loginForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Log in")(loginForm.render())
  }
}

object ChooseRole {
  def apply(roleForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Choose Role")(roleForm.render())
  }
}

object ListUsers {
  def apply(users: List[User])(implicit req: VisitRequest[_], config: Config) = {
    config.main("List of All Users")(
      div.cls("page-header")("User List"),
      table.cls("table", "table-striped", "table-condensed")(
        <thead><th>Last</th><th>First</th><th>Middle</th><th>Preferred</th><th>Username</th></thead>,
        for (u <- users) yield 
          <tr><td>{u.last}</td><td>{u.first}</td><td>{u.middle.getOrElse("")}</td><td>{u.preferred.getOrElse("")}</td><td>{u.username}</td></tr>
      )
    )     
  }
}

object ChangeSettings {
  def apply(pwForm: Binding, themeForm: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main("Change Your Settings")(
      pwForm.render(overrideSubmit = Some(FormCall(controllers.users.routes.App.changePassword())),
          legend = Some("Change your Password")),
      themeForm.render(overrideSubmit = Some(FormCall(controllers.users.routes.App.changeTheme())),
          legend = Some("Set your Theme"))
    )
  }
}
