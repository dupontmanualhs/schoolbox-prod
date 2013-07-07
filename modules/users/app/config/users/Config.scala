package config.users

import scala.xml.NodeSeq
import scalatags.STag
import forms.Call
import play.api.templates.Html

import models.users.Role
import controllers.users.VisitRequest

trait Config {
  def defaultCall: Call
  def main: MainTemplate
  def notFound: NotFoundTemplate
  def menuBuilder: (Option[Role] => NodeSeq)
  def webjars: (String => Call)
}

trait MainTemplate {
  @deprecated("use the scalatags version", "2013-07-04")
  def apply(pageTitle: String, scripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]): Html

  def apply(pageTitle: String, scripts: STag*)(content: STag*)(implicit req: VisitRequest[_]): Html
}

trait NotFoundTemplate {
  def apply(reason: STag)(implicit req: VisitRequest[_]): Html
}
