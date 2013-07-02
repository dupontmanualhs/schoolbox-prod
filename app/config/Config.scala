package config

import com.tzavellas.sse.guice.ScalaModule
import config.users.MainTemplate
import templates.Main
import config.users.Config

trait CommonConfig extends config.users.Config

class ConfigImpl extends CommonConfig {
  def defaultCall = controllers.routes.App.index()
  def mainTemplate = templates.Main
}
