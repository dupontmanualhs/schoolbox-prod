import play.api.templates.Html
import controllers.users.VisitRequest

package object views {
  def main(title: String, scripts: Html = Html(""))(content: Html)(implicit req: VisitRequest[_]) = {
    _root_.templates.Main(title, scripts)(content)(req)
  }
}