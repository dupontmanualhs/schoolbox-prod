package config.users

import scala.xml.NodeSeq
import com.google.inject.Injector
import scalatags.STag
import org.dupontmanual.forms.Call
import play.api.templates.Html
import models.users.Role
import controllers.users.VisitRequest
import com.google.inject.Inject
import scalajdo.DataStore
import javax.jdo.JDOHelper
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory


trait Config {
  def defaultCall: Call
  def main: MainTemplate
  def notFound: NotFoundTemplate
  def menuBuilder: (Option[Role] => NodeSeq)
  def webjars: (String => Call)
}

object UsesDataStore {
  val dataStore: DataStore = new DataStore(() => JDOHelper.getPersistenceManagerFactory("play-eschool").asInstanceOf[JDOPersistenceManagerFactory])
}

trait UsesDataStore {
  def dataStore = UsesDataStore.dataStore
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
