package templates

import play.api.mvc.Request
import scalatags._
import _root_.forms.Binding

package object forms {
  object Tester {
    def apply(form: Binding)(implicit req: Request[_]) = {
      html(
        head(
          title("Form Tester")),
	    body(
          h1("Form Tester"),
          p("All of these fields are optional and should be used to test how the different types of fields work."),
          p(form.render(legend=Some("This Is A Legend"))))).toXML.toString
    }
  }
  
  object Results {
    def apply(list: List[(String, String)])(implicit req: Request[_]) = {
      html(
        head(
          title("Results")),
        body(list.map(kv => p(kv._1 + "--->" + kv._2)))).toXML.toString
    }
  }
}
