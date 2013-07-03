package config

import com.tzavellas.sse.guice.ScalaModule

class ConfigInjector extends ScalaModule {
  def configure() {
    //bind[CommonConfig].to(new ConfigImpl())
  }
}