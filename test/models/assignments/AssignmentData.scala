package models.assignments

import scala.math.min
import scala.xml.Text
import util.ScalaPersistenceManager

object AssignmentData {
  val tfqs = List(
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
        </question>,
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
        </question>,
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
        </question>,
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
  )
  
  val mcqs = List(
        <question kind="mult-choice" scramble="true" format="html" single-select="true">
    	  <text>The boolean expression <code>a[i] == max || !(max != a[i])</code> can be simplified to</text>
          <feedback>The expression <code>!(max != a[i])</code> is equivalent to <code>max == a[i]</code>, so the given expression is equivalent to <code>a[i] == max || max == a[i]</code>, which is equivalent to <code>a[i] == max</code>.</feedback>
          <answer worth="1">
            <text><code>a[i] == max</code></text>
          </answer>
          <answer worth="0">
            <text><code>a[i] != max</code></text>
          </answer>
          <answer worth="0">
            <text><code>a[i] &lt; max || a[i] &gt; max</code></text>
          </answer>
          <answer worth="0">
            <text><code>true</code></text>
          </answer>
          <answer worth="0">
            <text><code>false</code></text>
          </answer>
        </question>      
  )
  
  val fbqs = List(
        <question kind="fill-blanks" format="html">
          <text>The capital of Peru is <blank/>.</text>
          <answer blank="0" worth="1">
            <text>Lima</text>
          </answer>
          <answer blank="0" worth="0">
            <text>Cuzco</text>
            <feedback>Cuzco was the Inca capital, but is not the modern capital of Peru.</feedback>
          </answer>
        </question>,
        <question kind="fill-blanks" format="html">
          <text>In alphabetical order, the three primitive types on the AP test are <blank/>, <blank/>, and <blank/>.</text>
          <answer blank="0" worth="1">
            <text>boolean</text>
          </answer>
          <answer blank="0" worth="1/2">
            <text>Boolean</text>
            <feedback>This is the wrapper class; the primitive is lower-case.</feedback>
          </answer>
          <answer blank="1" worth="1">
            <text>double</text>
          </answer>
          <answer blank="1" worth="1/2">
            <text>Double</text>
            <feedback>This is the wrapper class; the primitive is lower-case.</feedback>
          </answer>
          <answer blank="2" worth="1">
            <text>int</text>
          </answer>
          <answer blank="2" worth="1/2">
            <text>Integer</text>
            <feedback>This is the wrapper class; the primitive is lower-case.</feedback>
          </answer>
          <answer blank="2" worth="0">
            <text>Int</text>
            <feedback>There is no <code>Int</code> in Java. The primitive is <code>int</code> and the wrapper class is <code>Integer</code>.</feedback>
          </answer>
		</question>
  )
  
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    loadQuestions(debug)
    loadTasks(debug)
  }
    
  def loadQuestions(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    val qs =
    pm.beginTransaction()
    for (q <- tfqs ++ mcqs ++ fbqs) {
      val qText = (q \ "text").text
      if (debug) println("Adding question: %s...".format(qText.substring(0, min(qText.length, 40))))
      DbQuestion.fromXml(q) match {
        case Some(dbq) => pm.makePersistent(dbq)
        case None => // do nothing
      }
    }
    pm.commitTransaction()
  }
  
  def loadTasks(debug: Boolean = false)(implicit pm: ScalaPersistenceManager)  {
    if (debug) println("Adding a task with all questions in it.")
	val qs: List[DbQuestion] = pm.query[DbQuestion].executeList()
    val task = new Task(qs)
    pm.makePersistent(task)
  }
}