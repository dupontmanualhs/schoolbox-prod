package eschool.math

import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap.{ConvertableToMenu, Menu}
import net.liftweb.sitemap.Loc.Unless._
import net.liftweb.util.Helpers._
import net.liftweb.http.{S, Req, LiftRules}

package object Math {
    def rules = {
      LiftRules.dispatch.append {
        case Req("math" :: "totex" :: Nil, _, _) =>
          () => TeXToImage.getImage(S.param("tex").openTheBox, S.param("size").openTheBox.toFloat)
      }
    }
}