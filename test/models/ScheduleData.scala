package models.users
import util.ScalaPersistenceManager
import java.util.Date
import org.joda.time.LocalDate
import models.courses._
import models.books._

object ScheduleData {
	
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    //createUserData(debug
    //createYearsAndTerms(debug)
    //makeCourses(debug)
    //makeSections(debug)
    //makeEnrollments(debug)
    //makeBookData(debug)
    
    //create User Data
    val mary = new User("mary", "Mary", Some("King"), "Claire", None, Gender.FEMALE, "mary@mary.com", "cla123")
    val christina = new User("christina", "Christina", Some("King"), "Teresa", Some("Tina"), Gender.FEMALE, "christina@christina.com", "ter123")
    val jack = new User("jack", "Jack", Some("Oliver"), "Phillips", None, Gender.MALE, "jack@jack.com", "phi123")
    val richard = new User("richard", "Richard", Some("King"), "Will", None, Gender.MALE, "richard@richard.com", "wil123")
    val todd = new User("todd", "Todd", Some("Allen"), "O'Bryan", None, Gender.MALE, "todd@todd.com", "obr123")
    val fitzgerald = new User("fitzgerald", "Fitzgerald", Some("Longfellow"), "Pennyworth", Some("Fitz of Fury"), Gender.MALE, "fitzgerald@fitzgerald.com", "pen123")
    val tyler = new User("tyler", "Tyler", None, "Darnell", None, Gender.MALE, "tyler@tyler.com", "dar123")
    val meriadoc = new User("meriadoc", "Meriadoc", None, "Brandybuck", Some("Merry"), Gender.MALE, "meriadoc@meradoc.com", "bra123")
    val peregrin = new User("peregrin", "Peregrin", None, "Took", Some("Pippin"), Gender.MALE, "peregrin@peregrin.com", "too123")
    val mack = new User("mack", "Mack", None, "House", Some("Brick"), Gender.MALE, "mack@mack.com", "hou123")
    val andrew = new User("andrew", "Andrew", None, "Hamm", None, Gender.MALE, "andrew@andrew.com", "ham123")
    val jordan = new User("jordan", "Jordan", None, "Jorgensen", None, Gender.MALE, "jordan@jordan.com", "jor123")
    val emma = new User("emma", "Emma", Some("Kathryn"), "King", None, Gender.FEMALE, "emma@emma.com", "kin123")
    val laura = new User("laura", "Laura", Some("Ann"), "King", None, Gender.FEMALE, "laura@laura.com", "kin123")
    val john = new User("john", "John", Some("Francis"), "King", None, Gender.MALE, "john@john.com", "kin123")
    val eric = new User("eric", "Eric", None, "McKnight", None, Gender.MALE, "eric@eric.com", "mck123")
    val ericStud = new Student(eric, "4208935702", "384979", 6, "MST")
    val jackStud = new Student(jack, "3757202948", "425636", 0, "MST")
    val fitzgeraldStud = new Student(fitzgerald, "8340522509", "382085", 4, "VA")
    val tylerStud = new Student(tyler, "2558203943", "246666", 8, "MST")
    val meriadocStud = new Student(meriadoc, "6872037839", "495312", 9, "HSU")
    val peregrinStud = new Student(peregrin, "0974781434", "375012", 10, "HSU")
    val mackStud = new Student(mack, "4907532423", "819823", 11, "MST")
    val andrewStud = new Student(andrew, "0572059453", "745105", 12, "MST")
    val jordanStud = new Student(jordan, "2094298408", "037432", 1, "MST")
    val emmaStud = new Student(emma, "4534414554", "245434", 6, "CMA")
    val lauraStud = new Student(laura, "3943334223", "403024", 3, "YPAS")
    val johnStud = new Student(john, "5022165324", "154524", 12, "HSU")
    val maryTeacher = new Teacher(mary, "318508", "4284802048")
    val christinaTeacher = new Teacher(christina, "542358", "8795177958")
    val richardTeacher = new Teacher(richard, "423423", "4478340832")
    val toddTeacher = new Teacher(todd, "323423", "3042093480")
    val toddGuardian = new Guardian(Set(meriadocStud, peregrinStud))
    pm.makePersistentAll(List(mary, christina, jack, richard, john, fitzgerald, emma, laura, tyler, jordan, todd, andrew, mack, meriadoc, peregrin, eric, ericStud, maryTeacher, christinaTeacher, toddTeacher, richardTeacher, johnStud, fitzgeraldStud, emmaStud, lauraStud, tylerStud, jordanStud, jackStud, andrewStud, mackStud, meriadocStud, peregrinStud))
  
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
    val r1chemistry = new Section(chemistry, "133321", Set(fall2012, spring2013), Set(r1), new Room("201"))
    val r2chemistry = new Section(chemistry, "462656", Set(fall2012, spring2013), Set(r2), new Room("201"))
    val w1biology = new Section(biology, "653756", Set(fall2012, spring2013), Set(w1), new Room("201"))
    val w2biology = new Section(biology, "456456", Set(fall2012, spring2013), Set(w2), new Room("201"))
    val r1algebra1 = new Section(algebra1, "322346", Set(fall2012, spring2013), Set(r1), new Room("202"))
    val r2algebra2 = new Section(algebra2, "342342", Set(fall2012, spring2013), Set(r2), new Room("202"))
    val w1planning = new Section(planning, "777777", Set(fall2012, spring2013), Set(w1), new Room("202"))
    val w2geometry = new Section(geometry, "568567", Set(fall2012, spring2013), Set(w2), new Room("202"))
    val r1english1 = new Section(english1, "367677", Set(fall2012, spring2013), Set(r1), new Room("203"))
    val r2planning = new Section(planning, "666666", Set(fall2012, spring2013), Set(r2), new Room("203"))
    val w1english2 = new Section(english2, "545577", Set(fall2012, spring2013), Set(w1), new Room("203"))
    val w2english3 = new Section(english3, "999999", Set(fall2012, spring2013), Set(w2), new Room("203"))
    val r1planning = new Section(planning, "555555", Set(fall2012, spring2013), Set(r1), new Room("204"))
    val r2usHistory = new Section(usHistory, "564572", Set(fall2012, spring2013), Set(r2), new Room("204"))
    val w1worldHistory = new Section(worldHistory, "888888", Set(fall2012, spring2013), Set(w1), new Room("204"))
    val w2studySkill = new Section(studySkills, "444444", Set(fall2012, spring2013), Set(w2), new Room("204"))
    pm.makePersistentAll(List(r1chemistry, r2chemistry, w1biology, w2biology, r1algebra1, r2algebra2, w1planning, w2geometry, r1english1, r2planning, w1english2, w2english3, r1planning, r2usHistory, w1worldHistory, w2studySkill))
    
    //makeEnrollments(debug)
    val johnEnrollment = List(r2chemistry, r1algebra1, w2english3, w1worldHistory)
    for(cls <- johnEnrollment){
      val enr = new StudentEnrollment(johnStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(johnStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val emmaEnrollment = List(r2chemistry, w2geometry, r1english1, w1worldHistory)
    for(cls <- emmaEnrollment){
      val enr = new StudentEnrollment(emmaStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(emmaStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val lauraEnrollment = List(r1chemistry, r2algebra2, w2english3, w1worldHistory)
    for(cls <- lauraEnrollment){
      val enr = new StudentEnrollment(lauraStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(lauraStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val jackEnrollment = List(w1biology, r2algebra2, r1english1, w2studySkill)
    for(cls <- jackEnrollment){
      val enr = new StudentEnrollment(jackStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(jackStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val meriadocEnrollment = List(w2biology, r1algebra1, w1english2, r2usHistory)
    for(cls <- meriadocEnrollment){
      val enr = new StudentEnrollment(meriadocStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(meriadocStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val peregrinEnrollment = List(w2biology, r2algebra2, r1english1, w1worldHistory)
    for(cls <- peregrinEnrollment){
      val enr = new StudentEnrollment(peregrinStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(peregrinStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val fitzgeraldEnrollment = List(w1biology, r1algebra1, w2english3, r2usHistory)
    for(cls <- fitzgeraldEnrollment){
      val enr = new StudentEnrollment(fitzgeraldStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(fitzgeraldStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val mackEnrollment = List(w1biology, r1algebra1, w2english3, r2usHistory)
    for(cls <- mackEnrollment){
      val enr = new StudentEnrollment(mackStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(mackStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val ericEnrollment = List(r2chemistry, w2geometry, r1english1, w1worldHistory)
    for(cls <- ericEnrollment){
      val enr = new StudentEnrollment(ericStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(ericStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val tylerEnrollment = List(r1chemistry, r2algebra2, w1english2, w2studySkill)
    for(cls <- tylerEnrollment){
      val enr = new StudentEnrollment(tylerStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(tylerStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    val jordanEnrollment = List(r1chemistry, w2geometry, w1english2, r2usHistory)
    for(cls <- jordanEnrollment){
      val enr = new StudentEnrollment(jordanStud, cls, fall2012, null, null)
      val enr2 = new StudentEnrollment(jordanStud, cls, spring2013, null, null)
      pm.makePersistentAll(List(enr, enr2))
    }
    
    
    //makeBookData(debug)
    val algebra1Book = new Title("Algebra 1 (Prentice Hall Mathematics)", "Pearson Prentice Hall", "978-0130523167", 842, "10.9 x 8.8 x 1.6 inches", 4.5, true, new Date())
    val algebra2Book = new Title("Prentice Hall Mathematics: Algebra 2", "Pearson Prentice Hall", "978-0131339989", 842, "10.9 x 8.8 x 2 inches", 5.3, true, new Date())
    val geometryBook = new Title("Geometry", "W.H. Freeman & Company", "978-0716717454", 668, "10.1 x 7.7 x 1.4 inches", 3.3, true, new Date())
    val chemistryBook = new Title("Chemistry", "Houghton Mifflin", "978-0618528448", "1056", "10.6 x 8.5 x 1.6 inches", 5.5, true, new Date())
    val biologyBook = new Title("Biology", " Pearson Benjamin Cummings", "978-0805368444", "1393", "8.5 x 2.1 x 11 inches", 7.6, true, new Date())
    val english1Book = new Title("Glencoe Language Arts Grammar And Language Workbook Grade 9", " Glencoe/McGraw-Hill", "978-0028182940", "348", "10.9 x 8.5 x 0.6 inches", 9.6, true, new Date())
    val english2Book = new Title("Prentice Hall Literature Penguin: Grade 10: Student Edition (NATL)", "PRENTICE HALL", "978-0131317185", "1163", "10 x 7.9 x 1.8 inches", 5.2, true, new Date())
    val english3Book = new Title("Language of Literature, Grade 11 ", "McDougal Littel", "978-0395931813", "1408", "1 x 0.8 x 0.2 inches", 6.0, true, new Date())
    val worldHistoryBook = new Title("World History: Patterns of Interaction: Atlas by Rand McNally", "Mcdougal Littell/Houghton Mifflin", "978-0618690084", "1376", "11.2 x 8.7 x 1.8 inches", 6.8, true, new Date())
    val usHistoryBook = new Title("The American Pageant", "Wadsworth Publishing", "978-1111349530", "1152", "11 x 8.8 x 1.6 inches", 5.2, true, new Date())
  }
}