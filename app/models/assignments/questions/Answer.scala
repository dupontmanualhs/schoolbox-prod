package models.assignments.questions

import scala.xml.{Elem, Node, NodeSeq}
import math.ExactNumber
import math.Integer

case class Answer(text: NodeSeq, worth: ExactNumber, feedback: NodeSeq) {
  def toXml: Elem = <answer worth={ worth.toString }><text>{ text }</text><feedback>{ feedback }</feedback></answer>
}
case class TrueAnswer(worth: ExactNumber, feedback: NodeSeq) {
  def toXml: Elem = <answer worth={ worth.toString }><text>true</text><feedback>{ feedback }</feedback></answer>
}
case class FalseAnswer(worth: ExactNumber, feedback: NodeSeq) {
  def toXml: Elem = <answer worth={ worth.toString }><text>false</text><feedback>{ feedback }</feedback></answer>
}

object Answer {
  def fromXml(ans: Node): Answer = {
    val text = (ans \ "text").flatMap(_.child)
    val worth = ExactNumber((ans \ "@worth").text).getOrElse(Integer(0))
    val feedback = if ((ans \ "feedback").isEmpty) NodeSeq.Empty else (ans \ "feedback").flatMap(_.child)
    Answer(text, worth, feedback)
  }
}