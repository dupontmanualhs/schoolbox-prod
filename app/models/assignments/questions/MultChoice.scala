package models.assignments.questions

import xml.NodeSeq

import play.api.data._

case class MultChoice(val text: NodeSeq, val answers: Array[MultChoiceAnswer], val explanation: NodeSeq) {
}

object MultChoice {
  def create(numAnswers: Int): MultChoice = {
    val answers = Array.fill(numAnswers)(MultChoiceAnswer(NodeSeq.Empty, false))
    MultChoice(NodeSeq.Empty, answers, NodeSeq.Empty)
  }
  
}

case class MultChoiceAnswer(val text: NodeSeq, val correct: Boolean) {
  
}