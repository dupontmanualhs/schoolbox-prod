package models.assignments.questions

import scala.xml.{Elem, Node, NodeSeq}
import math.MathExactNumber
import math.MathInteger

case class Answer(text: NodeSeq, worth: MathExactNumber, feedback: NodeSeq) {
  def asXml: Elem = <answer worth={ worth.toString }><text>{ text }</text><feedback>{ feedback }</feedback></answer>
}
case class TrueAnswer(worth: MathExactNumber, feedback: NodeSeq) {
  def asXml: Elem = <answer worth={ worth.toString }><text>true</text><feedback>{ feedback }</feedback></answer>
}
case class FalseAnswer(worth: MathExactNumber, feedback: NodeSeq) {
  def asXml: Elem = <answer worth={ worth.toString }><text>false</text><feedback>{ feedback }</feedback></answer>
}

object Answer {
  def fromXml(ans: Node): Answer = {
    val text = (ans \ "text").flatMap(_.child)
    val worth = MathExactNumber((ans \ "@worth").text).getOrElse(MathInteger(0))
    val feedback = (ans \ "feedback").flatMap(_.child)
    Answer(text, worth, feedback)
  }
}