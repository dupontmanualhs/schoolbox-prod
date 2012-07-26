package models.users
import util.ScalaPersistenceManager
import org.joda.time.LocalDate
import models.courses._

object ScheduleData {
	
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    //createYearsAndTerms(debug)
    UserData.load(debug)
    //makeCourses(debug)
    //makeSections(debug)
    //makeEnrollments(debug)
    //makeBookData(debug)
  
    //createYearsAndTerms(debug)
    pm.beginTransaction()
    val acadYear = new AcademicYear("2012-13")
    pm.makePersistent(acadYear)
    val fall2012 = new Term("Fall 2012", acadYear, "f12", new LocalDate(2012, 8, 24), new LocalDate(2012, 12, 16))
    pm.makePersistent(fall2012)
    val spring2013 = new Term("Spring 2013", acadYear, "s13", new LocalDate(2013, 1, 3), new LocalDate(2013, 6, 4))
    pm.makePersistent(spring2013)
    val r1 = new Period("Red 1", 1)
    val r2 = new Period("Red 2", 2)
    val w1 = new Period("White 1", 3)
    val w2 = new Period("White 2", 4)
    val periods: List[Period] = List(r1, r2, w1, w2)
    pm.makePersistentAll(periods)
    if (debug) println("Created AcademicYear, Terms, and Periods")
    pm.commitTransaction()
    
    //makeCourses(debug)
    val scienceDept = new Department("Science")
    val englishDept = new Department("English")
    val mathematicsDept = new Department("Mathematics")
    val socialStudiesDept = new Department("Social Studies")
    val miscDept = new Department("Misc")
    val chemistry = new Course("Chemistry", "42994334", scienceDept)
    val biology = new Course("Biology", "42424334", scienceDept)
    val algebra1 = new Course("Algebra 1", "23884932", mathematicsDept)
    val algebra2 = new Course("Algebra 2", "12345678", mathematicsDept)
    val geometry = new Course("Geometry", "23884932", mathematicsDept)
    val english1 = new Course("English 1", "22403924", englishDept)
    val english2 = new Course("English 2", "22255524", englishDept)
    val english3 = new Course("English 3", "22442434", englishDept)
    val usHistory = new Course("US History", "43223924", socialStudiesDept)
    val worldHistory = new Course("World History", "42423924", socialStudiesDept)
    val planning = new Course("Planning", "90998998", miscDept)
    val studySkills = new Course("Study Skills", "32434244", miscDept)
    pm.makePersistentAll(List(scienceDept, englishDept, mathematicsDept, socialStudiesDept, biology, chemistry, algebra1,algebra2, geometry, english1, english2, english3, usHistory, worldHistory))
    
    //makeSections(debug)
    val r1r2chemistry = new Section(chemistry, "133321", Set(fall2012, spring2013), Set(r1, r2), new Room("201"))
    val w1w2biology = new Section(biology, "653756", Set(fall2012, spring2013), Set(w1, w2), new Room("201"))
    
  }
}