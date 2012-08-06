import play.api._
import util.DataStore

object Global extends GlobalSettings {
  override def onStop(app: Application) {
    DataStore.close()
  }
}