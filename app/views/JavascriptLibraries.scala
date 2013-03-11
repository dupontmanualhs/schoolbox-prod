package views

import play.api.templates.Html
import scala.xml.Node

object JavascriptLibraries {
  val TINY_MCE: Html = Html(
    <scripts>
      <script src={ controllers.routes.Assets.at("javascripts/tinymce/jscripts/tiny_mce/tiny_mce.js").url } type="text/javascript"></script>,
      <script src={ controllers.routes.Assets.at("javascripts/tinymce/jscripts/tiny_mce/jquery.tinymce.js").url } type="text/javascript"></script>
    </scripts>.\("script").toString
  )
}

