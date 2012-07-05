package util

import xml.{Elem, NodeSeq}
import java.util.Locale
import scala.xml.Node
import scala.xml.Text
import scala.xml.XML
import views.html.helper.FieldConstructor

object Helpers {
  implicit val tableFields = FieldConstructor(views.html.templates.fieldAsTableRow(_))
  
  def camel2TitleCase(camel: String): String = {
    camel match {
      case s: String if (s.length > 0) => {
        val buf: StringBuilder = new StringBuilder(s.substring(0, 1).toUpperCase)
        (1 until s.length) foreach ((index: Int) => {
          if ((index < s.length - 2) && 
              Character.isUpperCase(s.charAt(index - 1)) &&
              Character.isUpperCase(s.charAt(index)) &&
              Character.isLowerCase(s.charAt(index + 1))) {
            buf ++= (" " + s.substring(index, index + 1))
          } else if (Character.isUpperCase(s.charAt(index)) && 
              !Character.isUpperCase(s.charAt(index - 1))) {
            buf ++= (" " + s.substring(index, index + 1))
          } else if (!Character.isLetter(s.charAt(index)) &&
              Character.isLetter(s.charAt(index - 1))) {
            buf ++= (" " + s.substring(index, index + 1))
          } else {
            buf += s.charAt(index)
          }
        })
        buf.toString
      }
      case _ => camel
    }
  }
  
  def string2nodeSeq(legalNodeSeq: String): NodeSeq = {
    // TODO: this just crashes if someone passes in malformed XML
    val node = XML.loadString("<dummy>" + legalNodeSeq + "</dummy>")
    NodeSeq.fromSeq(node.child);
  }
  
  def string2elem(legalElem: String): Elem = {
    // TODO: this just crashes if someone passes in malformed XML
    XML.loadString(legalElem)
  }
  
  def pluralizeInformal(num: Int, word: String): String = {
    val listNoChange = List("moose", "fish", "deer", "sheep", "means", "offspring", "series", "species")
    if (listNoChange.contains(word)){
      return intToWordInformal(num) + " " + word
    }
    if (word.matches(".*child$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("d$", "dren") else word)
    }
    if (word.matches("^person$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("person", "people") else word)
    }
    if (word.matches(".*(?<![a])bus$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("bus$", "buses") else word)
    }
    if (word.matches("^circus$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("s$", "ses") else word)
    }
    if (word.matches("^vertebra$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("bra$", "brae") else word)
    }
    if (word.matches(".*genus$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("us$", "era") else word)
    }
    if (word.matches(".*corpus")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("us$", "ora") else word)
    }
    if (word.matches(".*[otf]ax")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("x$", "xes") else word)
    }
    if (word.matches(".*[mnf]ix")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("x$", "xes") else word)
    }
    if (word.matches(".*(?<![aeiou])y$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("y$", "ies") else word)
    }
    if (word.matches(".*[cs]h$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("h$", "hes") else word)
    }
    if (word.matches(".*fe$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("fe$", "ves") else word)
    }
    if (word.matches(".*f$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("f$", "ves") else word)
    }
    if (word.matches(".*o$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("o$", "oes") else word)
    }
    if (word.matches(".*z$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("z$", "zzes") else word)
    }
    if (word.matches(".*us$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("us$", "i") else word)
    }
    if (word.matches(".*is$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("is$", "es") else word)
    }
    if (word.matches(".*fe$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("fe$", "ves") else word)
    }
    if (word.matches(".*(?<![z])on$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("on$", "a") else word)
    }
    if (word.matches(".*um$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("um$", "a") else word)
    }
    if (word.matches("[ml]ouse$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("ouse$", "ice") else word)
    }
    if (word.matches(".*(?<![r])a$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("a$", "ae") else word)
    }
    if (word.matches(".*[tgf]oo(?![d]).*{1,2}$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("oo", "ee") else word)
    }
    if (word.matches(".*x$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("x$", "ces") else word)
    }
    if (word.matches(".*eau$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("eau$", "eaux") else word)
    }
    if (word.matches(".*man$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("man$", "men") else word)
    }
    if (word.matches(".*s$")){
      return intToWordInformal(num) + " " + (if (num != 1) word.replaceAll("s$", "ses") else word)
    }
    intToWordInformal(num) + " " + word + (if (num != 1) "s" else "")
  }

  def intToWordInformal(num: Int) = {
    if (0 <= num && num <= 10) numMap(num) else num.toString
  }

  val numMap = Map[Int, String](
    0 -> "zero",
    1 -> "one",
    2 -> "two",
    3 -> "three",
    4 -> "four",
    5 -> "five",
    6 -> "six",
    7 -> "seven",
    8 -> "eight",
    9 -> "nine",
    10 -> "ten"
  )

  def mkNodeSeq(els: List[NodeSeq], sep: Node): NodeSeq = {
    els match {
      case Nil => NodeSeq.Empty
      case one :: Nil => one
      case fst :: rst => fst ++ sep ++ mkNodeSeq(rst, sep)
    }
  }
  
  def intersperse[T](list: List[T], co: T): List[T] = {
    list.foldRight[List[T]](Nil)((elem: T, ls: List[T]) => co :: elem :: ls).tail
  }
}