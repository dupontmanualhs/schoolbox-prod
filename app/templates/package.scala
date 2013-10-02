import scalatags._
import play.api.templates.Html
import com.google.inject.{ Inject, Singleton }
import controllers.users.{ Theme, VisitRequest }
import config.users.{ Config, MainTemplate, NotFoundTemplate }

package object templates {
  import scala.language.implicitConversions
  
  private[templates] class ConfigProvider @Inject()(val config: Config)
  implicit lazy val config: Config = Global.injector.getInstance(classOf[ConfigProvider]).config

  object Main extends MainTemplate {
    def apply(pageTitle: String, extraScripts: STag*)(content: STag*)(implicit req: VisitRequest[_]): Html =
      Html("<!DOCTYPE html>") += Html(html(
        head(
          title(pageTitle),
          //Theme.themePick(req.visit.user),
          (headAfterTitleBeforeScripts ++ extraScripts)),
        body(
          menu(),
          container(content))).toXML.toString)

    @deprecated("use the Scalatags version of templates", "2013-07-04")
    def apply(pageTitle: String, extraScripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]): Html = {
      Seq(Html(s"<!DOCTYPE html><html><head><title>${pageTitle}</title>"),
        seqSTag2html(headAfterTitleBeforeScripts),
        extraScripts,
        Html("</head>"),
        Html(s"<body>${menu().toXML.toString}"),
        Html("<div class=\"container\"><div class=\"content\">"),
        seqSTag2html(messages()),
        content,
        Html("</div></div>")).reduceLeft(_ += _)
    }

    def headAfterTitleBeforeScripts: Seq[STag] = {
      Seq(
        link.rel("icon").ctype("image/png").href(controllers.routes.Assets.at("images/favicon.ico")),
        link.rel("stylesheet").href(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery-ui.css"))),
        link.rel("stylesheet").href(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("bootstrap.css"))),
        link.rel("stylesheet").href(controllers.routes.Assets.at("stylesheets/ourstyle.css")),
        link.rel("stylesheet").href(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("datepicker.css"))),
        link.rel("stylesheet").href(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("bootstrap-timepicker.min.css"))),
        <!-- HTML5 shim, for IE6-8 support of HTML5 elements. -->,
          <!--[if lt IE 9]>
          <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->,
        script.src(config.webjars("jquery.js")),
        script.src(config.webjars("jquery-ui.js")),
        script.src(config.webjars("bootstrap.js")),
        script.src(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("bootstrap-datepicker.js"))),
        script.src(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("bootstrap-timepicker.min.js"))),
        script.src(controllers.routes.WebJarAssets.at(controllers.WebJarAssets.locate("jquery.maskedinput.js")))
      )
    }

    def menu()(implicit req: VisitRequest[_]): STag = {
      div.cls("navbar", "navbar-fixed-top")(
        div.cls("navbar-inner")(
          div.cls("container")(
            a.cls("btn", "btn-navbar").attr("data-toggle" -> "collapse", "data-target" -> ".nav-collapse")(
              span.cls("icon-bar"), span.cls("icon-bar"), span.cls("icon-bar")),
            a.cls("brand").href(controllers.routes.App.index)("Schoolbox (v0.1)"),
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
      req.flash.get("warn").map(div.cls("alert")(_)).getOrElse(""))

    def seqSTag2html(tags: Seq[STag]): Html = Html(tags.map(_.toXML.toString).reduceLeft(_ + _))
  }

  object Index {
    def apply()(implicit req: VisitRequest[_]) =
      config.main("Schoolbox")(
        div.cls("hero-unit")(
          h1("Schoolbox"),
          p("Everything a school needs in one easy package. (Eventually.)")))
  }

  object NotFound extends NotFoundTemplate {
    def apply(reason: STag)(implicit req: VisitRequest[_]) =
      config.main("Error")(
        div.cls("alert", "alert-error")(reason),
        img.src("/assets/images/sosad.jpg"))
  }

  object Stub {
    def apply()(implicit req: VisitRequest[_]) =
      config.main("Stub")(
        h1("Unfortunately, this page is a stub, and the developers have yet to implement it."))
  }

}