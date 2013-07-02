package config.users

import play.api.templates.Html
import scalatags.STag
import controllers.users.VisitRequest

trait MainTemplate {
  def apply(pageTitle: String, scripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]): Html
  def apply(pageTitle: String, scripts: STag*)(content: STag*)(implicit req: VisitRequest[_]): Html = {
    apply(pageTitle, Html(scripts.foldLeft("")(_ + _.toString())))(Html(content.foldLeft("")(_ + _.toString)))(req)
  }
}
