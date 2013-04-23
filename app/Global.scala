import play.api._
import util.DataStore
import java.text.SimpleDateFormat

object Global extends GlobalSettings {
  override def onStop(app: Application) {
    DataStore.close()
  }
  
  
}