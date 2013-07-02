package templates

import scalatags._
import play.api.templates.Html
import controllers.users.{ Theme, VisitRequest }
import config.users.MainTemplate

object Main extends MainTemplate {
  def apply(pageTitle: String, extraScripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]) =
    Html(html(
      head(
        title(pageTitle),
        Theme.themePick(req.visit.user),
        <link rel="icon" type="image/png" href={ controllers.routes.Assets.at("images/favicon.ico").toString } />,
        <link rel="stylesheet" href={ controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery-ui.css")).toString } />,
        <!-- HTML5 shim, for IE6-8 support of HTML5 elements. -->,
        <!--[if lt IE 9]>
          <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->,
        <script src={ controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery.js")).toString }></script>,
        <script src={ controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery-ui.js")).toString }></script>,
        <script src={ controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("bootstrap.js")).toString }></script>,
        <script src={ controllers.routes.Assets.at("javascripts/maskedinput.js").toString }></script>,
        <script src={ controllers.routes.Assets.at("javascripts/jquery.timepicker.js").toString }></script>,
        extraScripts.toSTag),
      body(
        div.cls("navbar", "navbar-fixed-top")(
          div.cls("navbar-inner")(
            div.cls("container")(
              a.cls("btn", "btn-navbar").attr("data-toggle" -> "collapse", "data-target" -> ".nav-collapse")(
                span.cls("icon-bar"), span.cls("icon-bar"), span.cls("icon-bar")),
              a.cls("brand").href("/")("ABCD eSchool"),
              div.cls("nav-collapse", "collapse").id("main-menu")(
                req.visit.menu)))),
        div.cls("container")(
          div.cls("content")(
            req.flash.get("message").map(div.cls("alert", "alert-success")(_)).getOrElse(""),
            req.flash.get("error").map(div.cls("alert", "alert-error")(_)).getOrElse(""),
            req.flash.get("warn").map(div.cls("alert")(_)).getOrElse(""),
            content.toSTag)))).toString)
}

object Index {
  def apply(main: MainTemplate)(implicit req: VisitRequest[_]) =
    main("ABCD eSchool")(
      div.cls("hero-unit")(
        h1("ABCD eSchool"),
        p("OH GOD IM TRAPPED IN HERE SOMEONE HELP PLEASE!")
      ))
}

object NotFound {
  def apply(main: MainTemplate, reason: String)(implicit req: VisitRequest[_]) =
    main("Error")(
      div.cls("alert", "alert-error")(reason),
      img.src("assets/images/sosad.jpg"))
}

object Stub {
  def apply(main: MainTemplate)(implicit req: VisitRequest[_]) =
    main("Stub")(
      h1("Unfortunately, this page is a stub, and the developers have yet to implement it."))
}
