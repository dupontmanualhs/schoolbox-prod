package config.users

import scala.xml.NodeSeq
import com.google.inject.Injector
import scalatags.STag
import org.dupontmanual.forms.Call
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

trait ProvidesInjector {
  def provideInjector(): Injector
}

trait MainTemplate {
  @deprecated("use the scalatags version", "2013-07-04")
  def apply(pageTitle: String, scripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]): Html

  def apply(pageTitle: String, scripts: STag*)(content: STag*)(implicit req: VisitRequest[_]): Html
}

trait NotFoundTemplate {
  def apply(reason: STag)(implicit req: VisitRequest[_]): Html
}
