package models

import java.io.File
import java.sql.Date
import util.{DataStore, ScalaPersistenceManager}
import models.users._
import models.books._
import models.courses._
import org.joda.time.LocalDate

object TestData {
  def load(debug: Boolean = false) {
    val dbFile = new File("data.h2.db")
    dbFile.delete()
    DataStore.withManager { implicit pm =>
      loadScheduleData(debug)
      pm.close()
    }
  }
  
  def loadScheduleData(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    //createUserData(debug
    //createYearsAndTerms(debug)
    //makeCourses(debug)
    //makeSections(debug)
    //makeEnrollments(debug)
    //makeTeacherAssignments(debug)
    //makeBookData(debug)
    
    //create User Data
    if (debug) println("Creating sample users...")
    // teachers
    val mary = new User("mary", "Mary", Some("King"), "Claire", None, Gender.FEMALE, "mary@mary.com", "cla123")
    val christina = new User("christina", "Christina", Some("King"), "Teresa", Some("Tina"), Gender.FEMALE, "christina@christina.com", "ter123")
    val richard = new User("richard", "Richard", Some("King"), "Will", None, Gender.MALE, "richard@richard.com", "wil123")
    val todd = new User("todd", "Todd", Some("Allen"), "O'Bryan", None, Gender.MALE, "todd@todd.com", "obr123")
    val maryTeacher = new Teacher(mary, "318508", "4284802048")
    val christinaTeacher = new Teacher(christina, "542358", "8795177958")
    val richardTeacher = new Teacher(richard, "423423", "4478340832")
    val toddTeacher = new Teacher(todd, "323423", "3042093480")
    
    // students
    val jack = new User("jack", "Jack", Some("Oliver"), "Phillips", None, Gender.MALE, "jack@jack.com", "phi123")
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

    // guardians
    val reg = new User("reg", "Reginald", None, "Pennyworth", Some("Reg"), Gender.MALE, null, "pen123")
    val toddGuardian = new Guardian(Set(meriadocStud, peregrinStud))
    val regGuardian = new Guardian(Set(fitzgeraldStud))
    
    
    pm.makePersistentAll(List(
        mary, christina, richard, todd, 
        maryTeacher, christinaTeacher, richardTeacher, toddTeacher,
          jack, john, fitzgerald, emma, laura, tyler, jordan,  andrew, mack, meriadoc, peregrin, eric, 
          ericStud, johnStud, fitzgeraldStud, emmaStud, lauraStud, tylerStud, jordanStud, jackStud, andrewStud, mackStud, meriadocStud, peregrinStud,
          reg, toddGuardian, regGuardian))
 
    //createYearsAndTerms(debug)
    if (debug) println("Creating AcademicYear, Terms, and Periods...")
    val acadYear = new AcademicYear("2012-13")
    pm.makePersistent(acadYear)
    val fall2012 = new Term("Fall 2012", acadYear, "f12", new LocalDate(2012, 8, 21), new LocalDate(2012, 1, 11))
    pm.makePersistent(fall2012)
    val spring2013 = new Term("Spring 2013", acadYear, "s13", new LocalDate(2013, 1, 14), new LocalDate(2013, 6, 5))
    pm.makePersistent(spring2013)
    val r1 = new Period("Red 1", 1)
    val r2 = new Period("Red 2", 2)
    val w1 = new Period("White 1", 3)
    val w2 = new Period("White 2", 4)
    val periods: List[Period] = List(r1, r2, w1, w2)
    pm.makePersistentAll(periods)
    
    //makeCourses(debug)
    if (debug) println("Creating Departments and Courses...")
    val scienceDept = new Department("Science")
    val englishDept = new Department("English")
    val mathematicsDept = new Department("Mathematics")
    val socialStudiesDept = new Department("Social Studies")
    val miscDept = new Department("Misc")
    val chemA = new Course("Chemistry A", "429943341", scienceDept)
    val bioA = new Course("Biology A", "424243341", scienceDept)
    val alg1A = new Course("Algebra 1A", "238849321", mathematicsDept)
    val alg2A = new Course("Algebra 2A", "123456781", mathematicsDept)
    val geoA = new Course("Geometry A", "238849421", mathematicsDept)
    val eng1A = new Course("English 1A", "224039241", englishDept)
    val eng2A = new Course("English 2A", "222555241", englishDept)
    val eng3A = new Course("English 3A", "224424341", englishDept)
    val usHistA = new Course("US History A", "432239241", socialStudiesDept)
    val worldHistA = new Course("World History A", "424239241", socialStudiesDept)
    val chemB = new Course("Chemistry B", "429943342", scienceDept)
    val bioB = new Course("Biology B", "424243342", scienceDept)
    val alg1B = new Course("Algebra 1B", "238849322", mathematicsDept)
    val alg2B = new Course("Algebra 2B", "123456782", mathematicsDept)
    val geoB = new Course("Geometry B", "238849422", mathematicsDept)
    val eng1B = new Course("English 1B", "224039242", englishDept)
    val eng2B = new Course("English 2B", "222555242", englishDept)
    val eng3B = new Course("English 3B", "224424342", englishDept)
    val usHistB = new Course("US History B", "432239242", socialStudiesDept)
    val worldHistB = new Course("World History B", "424239242", socialStudiesDept)
    val planning = new Course("Planning", "909989980", miscDept)
    val studySkills = new Course("Study Skills", "324342440", miscDept)
    pm.makePersistentAll(List(scienceDept, englishDept, mathematicsDept, socialStudiesDept, 
        bioA, chemA, alg1A,alg2A, geoA, eng1A, eng2A, eng3A, usHistA, worldHistA,
        bioB, chemB, alg1B,alg2B, geoB, eng1B, eng2B, eng3B, usHistB, worldHistB,
        planning, studySkills))
    
    // makeRooms
    if (debug) println("Creating Rooms...")
    val r201 = new Room("201")
    val r202 = new Room("202")
    val r203 = new Room("203")
    val r204 = new Room("204")
    pm.makePersistentAll(List(r201, r202, r203, r204))
    
    //makeSections(debug)
    if (debug) println("Creating Sections of courses...")
    val r1chemA = new Section(chemA, "333211", Set(fall2012), Set(r1), r201)
    val r1chemB = new Section(chemB, "333212", Set(spring2013), Set(r1), r201)
    val r2chemA = new Section(chemA, "626561", Set(fall2012), Set(r2), r201)
    val r2chemB = new Section(chemB, "626562", Set(spring2013), Set(r2), r201)
    val w1bioA = new Section(bioA, "537561", Set(fall2012), Set(w1), r201)
    val w1bioB = new Section(bioB, "537562", Set(spring2013), Set(w1), r201)
    val w2bioA = new Section(bioA, "564561", Set(fall2012), Set(w2), r201)
    val w2bioB = new Section(bioB, "564562", Set(spring2013), Set(w2), r201)
    val r1alg1A = new Section(alg1A, "223461", Set(fall2012), Set(r1), r202)
    val r1alg1B = new Section(alg1B, "223462", Set(spring2013), Set(r1), r202)
    val r2alg2A = new Section(alg2A, "423421", Set(fall2012), Set(r2), r202)
    val r2alg2B = new Section(alg2B, "423422", Set(spring2013), Set(r2), r202)
    val w1plan = new Section(planning, "777777", Set(fall2012, spring2013), Set(w1), r202)
    val w2geoA = new Section(geoA, "685671", Set(fall2012), Set(w2), r202)
    val w2geoB = new Section(geoB, "685672", Set(spring2013), Set(w2), r202)
    val r1eng1A = new Section(eng1A, "676771", Set(fall2012), Set(r1), r203)
    val r1eng1B = new Section(eng1B, "676772", Set(spring2013), Set(r1), r203)
    val r2plan = new Section(planning, "666666", Set(fall2012, spring2013), Set(r2), r203)
    val w1eng2A = new Section(eng2A, "455771", Set(fall2012), Set(w1), r203)
    val w1eng2B = new Section(eng2B, "455772", Set(spring2013), Set(w1), r203)
    val w2eng3A = new Section(eng3A, "999991", Set(fall2012), Set(w2), r203)
    val w2eng3B = new Section(eng3B, "999992", Set(spring2013), Set(w2), r203)
    val r1plan = new Section(planning, "555555", Set(fall2012, spring2013), Set(r1), r204)
    val r2usHistA = new Section(usHistA, "645721", Set(fall2012), Set(r2), r204)
    val r2usHistB = new Section(usHistB, "645722", Set(spring2013), Set(r2), r204)
    val w1worldHistA = new Section(worldHistA, "888881", Set(fall2012), Set(w1), r204)
    val w1worldHistB = new Section(worldHistB, "888882", Set(spring2013), Set(w1), r204)
    val w2studySkill = new Section(studySkills, "444444", Set(fall2012, spring2013), Set(w2), r204)
    pm.makePersistentAll(List(
        r1chemA, r2chemA, w1bioA, w2bioA, r1alg1A, r2alg2A, w2geoA, r1eng1A, w1eng2A, w2eng3A, r2usHistA, w1worldHistA,
        r1chemB, r2chemB, w1bioB, w2bioB, r1alg1B, r2alg2B, w2geoB, r1eng1B, w1eng2B, w2eng3B, r2usHistB, w1worldHistB,
        r1plan, w1plan, r2plan, w2studySkill))

    //makeEnrollments(debug)
    if (debug) println("Creating student enrollments in sections...")
    val enrollments = Map(
        johnStud -> List(r2chemA, r2chemB, r1alg1A, r1alg1B, w2eng3A, w2eng3B, w1worldHistA, w1worldHistB),
        emmaStud -> List(r2chemA, r2chemB, w2geoA, w2geoB, r1eng1A, r1eng1B, w1worldHistA, w1worldHistB),
        lauraStud -> List(r1chemA, r2alg2A, w2eng3A, w1worldHistA, r1chemB, r2alg2B, w2eng3B, w1worldHistB),
        jackStud -> List(w1bioA, r2alg2A, r1eng1A, w2studySkill, w1bioB, r2alg2B, r1eng1B),
        meriadocStud -> List(w2bioA, r1alg1A, w1eng2A, r2usHistA, w2bioB, r1alg1B, w1eng2B, r2usHistB),
        peregrinStud -> List(w2bioA, r2alg2A, r1eng1A, w1worldHistA, w2bioB, r2alg2B, r1eng1B, w1worldHistB),
        fitzgeraldStud -> List(w1bioA, r1alg1A, w2eng3A, r2usHistA, w1bioB, r1alg1B, w2eng3B, r2usHistB),
        mackStud -> List(w1bioA, r1alg1A, w2eng3A, r2usHistA, w1bioB, r1alg1B, w2eng3B, r2usHistB),
        ericStud -> List(r2chemA, w2geoA, r1eng1A, w1worldHistA, r2chemB, w2geoB, r1eng1B, w1worldHistB),
        tylerStud -> List(r1chemA, r2alg2A, w1eng2A, w2studySkill, r1chemB, r2alg2B, w1eng2B),
        jordanStud -> List(r1chemA, w2geoA, w1eng2A, r2usHistA, r1chemB, w2geoB, w1eng2B, r2usHistB)
    )

    for ((student, sections) <- enrollments) {
      for (sect <- sections) {
        pm.makePersistent(new StudentEnrollment(student, sect, null, null))
      }
    }
    
    //makeTeacherAssignments
    if (debug) println("Creating teacher assignments to sections...")
    
    val assignments = Map(
        maryTeacher -> List(r1plan, r2usHistA, r2usHistB, w1worldHistA, w1worldHistB, w2studySkill),
        christinaTeacher -> List(r1eng1A, r2plan, w1eng2A, w2eng3A, r1eng1B, w1eng2B, w2eng3B),
        toddTeacher -> List(r1alg1A, r2alg2A, w1plan, w2geoA, r1alg1B, r2alg2B, w2geoB),
        richardTeacher -> List(r1chemA, r1chemB, r2chemA, r2chemB, w1bioA, w1bioB, w2bioA, w2bioB)
    )

    for ((teacher, sections) <- assignments) {
      for (sect <- sections) {
        pm.makePersistent(new TeacherAssignment(teacher, sect, null, null))
      }
    }
    
    
    //makeBookData(debug)
    if (debug) println("Creating Titles...")
    val algebra1Book = new Title("Algebra 1 (Prentice Hall Mathematics)", "Bellman, Bragg and Charles", "Pearson Prentice Hall", "978-0130523167", 842, "10.9 x 8.8 x 1.6 inches", 4.5, true, new Date(System.currentTimeMillis()))
    val algebra2Book = new Title("Prentice Hall Mathematics: Algebra 2", "Dan Kennedy, Randall I. Charles and Sadie Chavis Bragg", "Pearson Prentice Hall", "978-0131339989",900, "10.9 x 8.8 x 2 inches", 5.3, true, new Date(System.currentTimeMillis()))
    val geometryBook = new Title("Geometry", "Harold R. Jacobs", "W.H. Freeman & Company", "978-0716717454", 668, "10.1 x 7.7 x 1.4 inches", 3.3, true, new Date(System.currentTimeMillis()))
    val chemistryBook = new Title("Chemistry", "Steven S. Zumdahl and Susan A. Zumdahl", "Houghton Mifflin", "978-0618528448", 1056, "10.6 x 8.5 x 1.6 inches", 5.5, true, new Date(System.currentTimeMillis()))
    val biologyBook = new Title("Biology", "Neil A. Campbell, Jane B. Reece, Lisa A. Urry and Michael L. Cain", " Pearson Benjamin Cummings", "978-0805368444", 1393, "8.5 x 2.1 x 11 inches", 7.6, true, new Date(System.currentTimeMillis()))
    val english1Book = new Title("Glencoe Language Arts Grammar And Language Workbook Grade 9", "John King", " Glencoe/McGraw-Hill", "978-0028182940", 348, "10.9 x 8.5 x 0.6 inches", 9.6, true, new Date(System.currentTimeMillis()))
    val english2Book = new Title("Prentice Hall Literature Penguin: Grade 10: Student Edition (NATL)", "Todd O'Bryan", "PRENTICE HALL", "978-0131317185", 1163, "10 x 7.9 x 1.8 inches", 5.2, true, new Date(System.currentTimeMillis()))
    val english3Book = new Title("Language of Literature, Grade 11 ", "Pat Day", "McDougal Littel", "978-0395931813", 1408, "1 x 0.8 x 0.2 inches", 6.0, true, new Date(System.currentTimeMillis()))
    val worldHistoryBook = new Title("World History: Patterns of Interaction: Atlas by Rand McNally", "Roger B. Beck, Linda Black and Larry S. Krieger", "Mcdougal Littell/Houghton Mifflin", "978-0618690084", 1376, "11.2 x 8.7 x 1.8 inches", 6.8, true, new Date(System.currentTimeMillis()))
    val usHistoryBook = new Title("The American Pageant", "David M. Kennedy and Lizabeth Cohen", "Wadsworth Publishing", "978-1111349530", 1152, "11 x 8.8 x 1.6 inches", 5.2, true, new Date(System.currentTimeMillis()))
  }

}