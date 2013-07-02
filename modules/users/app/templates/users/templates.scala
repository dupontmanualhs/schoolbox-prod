package templates.users

import scala.language.implicitConversions
import scalatags._
import play.api.templates.Html

import forms.{ Binding, FormCall }

import config.users.MainTemplate
import controllers.users.VisitRequest
import models.users.User

object Login {
  def apply(main: MainTemplate, loginForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Log in")(loginForm.render())
  }
}

object ChooseRole {
  def apply(main: MainTemplate, roleForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Choose Role")(roleForm.render())
  }
}

object ListUsers {
  def apply(main: MainTemplate, users: List[User])(implicit req: VisitRequest[_]) = {
    main("List of All Users")(
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
  def apply(main: MainTemplate, pwForm: Binding, themeForm: Binding)(implicit req: VisitRequest[_]) = {
    main("Change Your Settings")(
      pwForm.render(overrideSubmit = Some(FormCall(controllers.users.routes.App.changePassword())),
          legend = Some("Change your Password")),
      themeForm.render(overrideSubmit = Some(FormCall(controllers.users.routes.App.changeTheme())),
          legend = Some("Set your Theme"))
    )
  }
}
