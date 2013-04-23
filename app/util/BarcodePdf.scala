package util

import play.api._
import play.api.mvc._
import models.books._
import models.users._
import com.itextpdf.text.pdf._
import com.itextpdf.text._

class BarcodePdf(res: String) {
}

object BarcodePdf {
  val document: Document = new Document(PageSize.LETTER)
}
