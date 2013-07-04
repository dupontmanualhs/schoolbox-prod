package config.users

import play.api.templates.Html
import scalatags.STag
import controllers.users.VisitRequest

trait MainTemplate {
  @deprecated("use the scalatags version", "2013-07-04")
  def apply(pageTitle: String, scripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]): Html

  def apply(pageTitle: String, scripts: STag*)(content: STag*)(implicit req: VisitRequest[_]): Html
}
