package models.assignments

import scala.math.min
import scala.xml.Text
import util.ScalaPersistenceManager

object AssignmentData {
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    loadQuestions(debug)
  }
    
  def loadQuestions(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    val qs =
      <questions>
        <question kind="true-false" format="html">
          <text>The median is the value that occurs most often in a sample of data.</text>
          <feedback>The <em>mode</em> is actually this value.</feedback>
          <answer worth="0">
            <text>true</text>
            <feedback>The median is the center value.</feedback>
          </answer>
          <answer worth="1">
            <text>false</text>
            <feedback></feedback>
          </answer>
        </question>
        <question kind="true-false" format="html">
          <text>If there is a single, unique mode, the mean, median, and mode will all be equal when the distribution is symmetric.</text>
          <feedback>If the distribution is symmetric and there's a unique mode, all three measures of central tendency are the same.</feedback>
          <answer worth="1">
            <text>true</text>
            <feedback>Since the mode must be unique, only the median value can occur more than any others.</feedback>
          </answer>
          <answer worth="0">
            <text>false</text>
            <feedback>If there were no mode or there were more than one mode, this would be false.</feedback>
          </answer>
        </question>
        <question kind="true-false" format="html">
          <text>The <em>Empirical Rule</em> holds for any set of data, regardless of its shape.</text>
          <feedback>The Empirical Rule only holds for a normal distribution.</feedback>
          <answer worth="0">
            <text>true</text>
            <feedback>This was not a tricky question.</feedback>
          </answer>
          <answer worth="1">
            <text>false</text>
            <feedback></feedback>
          </answer>
        </question>
        <question kind="true-false" format="html">
          <text>Those elements that are not in Set <em>A</em> are called the complement of <em>A</em>.</text>
          <feedback>This is the definition of <em>complement</em>.</feedback>
          <answer worth="1">
            <text>true</text>
            <feedback></feedback>
          </answer>
          <answer worth="0">
            <text>false</text>
            <feedback>Make sure you study vocabulary!</feedback>
          </answer>
        </question>
      </questions>
    pm.beginTransaction()
    for (q <- (qs \ "question")) {
      val qText = (q \ "text").text
      if (debug) println("Adding question: %s...".format(qText.substring(0, min(qText.length, 40))))
      DbQuestion.fromXml(q) match {
        case Some(dbq) => pm.makePersistent(dbq)
        case None => // do nothing
      }
    }
    pm.commitTransaction()
  }
}