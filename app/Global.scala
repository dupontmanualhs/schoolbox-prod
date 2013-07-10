import play.api.GlobalSettings
import com.google.inject.{ Guice, Injector }
import config.ConfigInjector
import config.users.ProvidesInjector

object Global extends GlobalSettings with ProvidesInjector {
  def provideInjector(): Injector = Guice.createInjector(new ConfigInjector())
  lazy val injector = Guice.createInjector(new ConfigInjector())
 
  override def getControllerInstance[A](klass: Class[A]) = {
    injector.getInstance(klass)
  }
}