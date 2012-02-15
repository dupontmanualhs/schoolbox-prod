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
	val javaLang = new Subject("Java Language Features", null)
	val types = new Subject("Types", javaLang)
	val ops = new Subject("Operators", javaLang)
	val loops = new Subject("Loops", javaLang)
	val fors = new Subject("For Loops", loops)
	val whiles = new Subject("While Loops", loops)
	val ifs = new Subject("Conditionals", javaLang)
	val standardClasses = new Subject("Standard Classes", null)
	val string = new Subject("String", standardClasses)
	val objectClass = new Subject("Object", standardClasses)
	val arrayList = new Subject("ArrayList", standardClasses)
	val inhPol = new Subject("Inheritance and Polymorphism", null)
	val comp = new Subject("Type Compatibility", inhPol)
	val interfaces = new Subject("Interfaces", inhPol)
	pm.makePersistentAll(List(javaLang, types, ops, loops, fors, whiles, ifs,
	    standardClasses, string, objectClass, arrayList, inhPol, comp, interfaces))
  }
}