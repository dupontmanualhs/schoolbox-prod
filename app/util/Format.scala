package util

import scala.xml.{NodeSeq, Text}

sealed abstract class Format {
  def toInt: Int
  def toXml: NodeSeq
}
object Format {
  case object PlainText extends Format {
    def toInt = 0
    def toXml = Text("plain-text")
  }
  case object Html extends Format {
    def toInt = 1
    def toXml = Text("html")
  }
  case object Markdown extends Format {
    def toInt = 2
    def toXml = Text("markdown")
  }
  case object Wiki extends Format {
    def toInt = 3
    def toXml = Text("wiki")
  }
  case object TeX extends Format {
    def toInt = 4
    def toXml = Text("tex")
  }
  
  def fromInt(i: Int): Format = i match {
    case 1 => Html
    case 2 => Markdown
    case 3 => Wiki
    case 4 => TeX
    case _ => PlainText
  }
  
  def fromXml(x: NodeSeq) = x match {
    case Text("html") => Html
    case Text("markdown") => Markdown
    case Text("wiki") => Wiki
    case Text("tex") => TeX
    case _ => PlainText
  }
}
