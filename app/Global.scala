import play.api.GlobalSettings

import com.google.inject.Guice

import config.ConfigInjector

object Global extends GlobalSettings {
  private lazy val injector = Guice.createInjector(new ConfigInjector())
 
  override def getControllerInstance[A](klass: Class[A]) = {
    injector.getInstance(klass)
  }
}