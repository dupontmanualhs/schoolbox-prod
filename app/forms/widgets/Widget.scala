package forms.widgets
import scala.xml.MetaData
import scala.xml.NodeSeq
import scala.xml.Null
import scala.xml.UnprefixedAttribute
import play.api.mvc.MultipartFormData.FilePart

abstract class Widget(
    val attrs: MetaData = Null,
    val isRequired: Boolean = false) {

  def isHidden: Boolean = false

  def needsMultipartForm: Boolean = false

  def render(name: String, value: Seq[String], attrList: MetaData = Null): NodeSeq
  
  //TODO: need something different for files
  def valueFromDatadict(data: Map[String, Seq[String]], name: String): Seq[String] = {
    data.getOrElse(name, Nil)
  } 
}

object Widget {
  implicit def map2MetaData(attrs: Map[String, String]): MetaData = {
    attrs.foldRight[MetaData](Null)(
	  (keyValue: (String, String), rest: MetaData) => 
	    new UnprefixedAttribute(keyValue._1, keyValue._2, rest))
  }
}

abstract class WidgetCompanion {
  
}
