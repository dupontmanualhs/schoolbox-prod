package config

import scala.xml.NodeSeq

import com.tzavellas.sse.guice.ScalaModule
import config.users.MainTemplate
import templates.Main
import models.users.Role

trait Config extends config.users.Config

class ConfigImpl extends Config {
  def defaultCall = controllers.routes.App.index()
  def mainTemplate = templates.Main
  def menuBuilder: (Option[Role] => NodeSeq) = controllers.Menu.buildMenu _
}

class ConfigInjector extends ScalaModule {
  def configure() {
    bind[config.users.Config].to[ConfigImpl]
    bind[Config].to[ConfigImpl]
  }
}
