package templates

import scalatags._
import play.api.templates.Html
import controllers.users.{ Theme, VisitRequest }
import config.users.MainTemplate

object Main extends MainTemplate {
  def apply(pageTitle: String, extraScripts: STag*)(content: STag*)(implicit req: VisitRequest[_]): Html =
    Html(
      html(
        head(
          title(pageTitle),
          Theme.themePick(req.visit.user),
          (headAfterTitleBeforeScripts ++ extraScripts)),
        body(
          menu(),
          container(content))).toXML.toString)
  
  @deprecated("use the Scalatags version of templates", "2013-07-04")
  def apply(pageTitle: String, extraScripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]): Html = {
    Seq(Html(s"<html><head><title>${pageTitle}</title>${Theme.themePick(req.visit.user)}"),
        seqSTag2html(headAfterTitleBeforeScripts),
        extraScripts,
        Html("</head>"),
        Html(s"<body>${menu().toXML.toString}"),
        Html("<div class=\"container\"><div class=\"content\">"),
        seqSTag2html(messages()),
        content,
        Html("</div></div>")).reduceLeft(_ += _)
  }
  
  lazy val headAfterTitleBeforeScripts: Seq[STag] = Seq(
    link.rel("icon").ctype("image/png").href(controllers.routes.Assets.at("images/favicon.ico")),
    link.rel("stylesheet").href(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery-ui.css"))),
    <!-- HTML5 shim, for IE6-8 support of HTML5 elements. -->,
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->,
    script.src(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery.js"))),
    script.src(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery-ui.js"))),
    script.src(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("bootstrap.js"))),
    script.src(controllers.routes.Assets.at("javascripts/maskedinput.js")),
    script.src(controllers.routes.Assets.at("javascripts/jquery.timepicker.js")))

  def menu()(implicit req: VisitRequest[_]): STag = {
    div.cls("navbar", "navbar-fixed-top")(
    div.cls("navbar-inner")(
      div.cls("container")(
        a.cls("btn", "btn-navbar").attr("data-toggle" -> "collapse", "data-target" -> ".nav-collapse")(
          span.cls("icon-bar"), span.cls("icon-bar"), span.cls("icon-bar")),
        a.cls("brand").href("/")("ABCD eSchool"),
        div.cls("nav-collapse", "collapse").id("main-menu")(
          req.visit.menu))))
  }
  
  def container(content: STag*)(implicit req: VisitRequest[_]): STag = {
    div.cls("container")(
          div.cls("content")(
            (messages ++ content)))
  }
  
  def messages()(implicit req: VisitRequest[_]): Seq[STag] = Seq(
    req.flash.get("message").map(div.cls("alert", "alert-success")(_)).getOrElse(""),
    req.flash.get("error").map(div.cls("alert", "alert-error")(_)).getOrElse(""),
    req.flash.get("warn").map(div.cls("alert")(_)).getOrElse("")
  )
  
  def seqSTag2html(tags: Seq[STag]): Html = Html(tags.map(_.toXML.toString).reduceLeft(_ + _))
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
