package models.assignments
import util.ScalaPersistenceManager

object AssignmentData {
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
	val barrons = new Source("Barron's Book", null)
	val barCh1 = new Source("Chapter 1", barrons)
	val barCh2 = new Source("Chapter 2", barrons)
	val litvin = new Source("Be Prepared for the AP Comp Sci Exam", null)
	val litCh1 = new Source("Ch 1", litvin)
	val litCh2 = new Source("Ch 2", litvin)
	val litCh3 = new Source("Ch 3", litvin)
	pm.makePersistentAll(List(barrons, barCh1, barCh2, litvin, litCh1, litCh2, litCh3))
  }
}