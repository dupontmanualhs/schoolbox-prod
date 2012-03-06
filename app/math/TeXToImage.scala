package eschool.math

import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio._

import net.liftweb.http._
import S._

import net.liftweb.util._

import java.io.ByteArrayOutputStream
import org.scilab.forge.jlatexmath._
import net.liftweb.common.{Full, Box}

object TeXToImage {
  def getImage(tex: String, size: Float): Box[LiftResponse] = {
      val formula = new TeXFormula(tex)
      val image: Image = formula.createBufferedImage(TeXConstants.STYLE_DISPLAY, size, Color.BLACK, Color.WHITE)
      var buffer: ByteArrayOutputStream = new ByteArrayOutputStream()
      if(ImageIO.write(image.asInstanceOf[BufferedImage], "PNG", buffer)){
         var bytes: Array[Byte] = buffer.toByteArray
         return Full(InMemoryResponse(bytes, ("Content-Type" -> "image/png") :: Nil, Nil, 201))
      } else {
         return Full(InternalServerErrorResponse())
      }
  }
}