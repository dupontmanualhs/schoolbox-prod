package models

import java.io.File
import java.sql._
import javax.jdo.annotations._
import org.joda.time.LocalDate
import models.blogs._
import models.books._
import models.conferences._
import models.courses._
import models.grades._
import models.lockers._
import models.mastery._
import models.users._
import scalajdo.ScalaPersistenceManager
import models.users.Gender
import models.users.User
import util.Helpers.{ isoDatetime, isoDate }
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory
import javax.jdo.JDOHelper
import java.util.Properties
import org.joda.time.{ LocalDate, LocalDateTime, LocalTime }
import scala.collection.JavaConverters._
import config.users.UsesDataStore

object TestData extends UsesDataStore {
  def load(debug: Boolean = true) {
    val classes = dataStore.persistentClasses.asJava
    val props = new Properties()
    dataStore.storeManager.deleteSchema(classes, props)
    dataStore.storeManager.createSchema(classes, props)
    loadScheduleData(debug)
  }

  def loadScheduleData(debug: Boolean = true) {
    dataStore.withTransaction { pm =>
      //create User Data
      if (debug) println("Creating sample users...")
      // Users
      // teachers
      val mary = new User("mary", "Mary", Some("King"), "Claire", None, Gender.Female, "mary@mary.com", "cla123")
      val christina = new User("christina", "Christina", Some("King"), "Teresa", Some("Tina"), Gender.Female, "christina@christina.com", "ter123")
      val richard = new User("richard", "Richard", Some("King"), "Will", None, Gender.Male, "richard@richard.com", "wil123")
      val todd = new User("todd", "Todd", Some("Allen"), "O'Bryan", None, Gender.Male, "todd@todd.com", "obr123")
      // students
      val jack = new User("jack", "Jack", Some("Oliver"), "Phillips", None, Gender.Male, "jack@jack.com", "phi123")
      val fitzgerald = new User("fitzgerald", "Fitzgerald", Some("Longfellow"), "Pennyworth", Some("Fitz of Fury"), Gender.Male, "fitzgerald@fitzgerald.com", "pen123")
      val tyler = new User("tyler", "Tyler", None, "Darnell", None, Gender.Male, "tyler@tyler.com", "dar123")
      val meriadoc = new User("meriadoc", "Meriadoc", None, "Brandybuck", Some("Merry"), Gender.Male, "meriadoc@meradoc.com", "bra123")
      val peregrin = new User("peregrin", "Peregrin", None, "Took", Some("Pippin"), Gender.Male, "peregrin@peregrin.com", "too123")
      val mack = new User("mack", "Mack", None, "House", Some("Brick"), Gender.Male, "mack@mack.com", "hou123")
      val andrew = new User("andrew", "Andrew", None, "Hamm", None, Gender.Male, "andrew@andrew.com", "ham123")
      val jordan = new User("jordan", "Jordan", None, "Jorgensen", None, Gender.Male, "jordan@jordan.com", "jor123")
      val emma = new User("emma", "Emma", Some("Kathryn"), "King", None, Gender.Female, "emma@emma.com", "kin123")
      val laura = new User("laura", "Laura", Some("Ann"), "King", None, Gender.Female, "laura@laura.com", "kin123")
      val john = new User("john", "John", Some("Francis"), "King", None, Gender.Male, "john@john.com", "kin123")
      val bobby = new User("bobby", "Bobby", None, "Hill", Some("Dangit Bobby"), Gender.Male, "bobby@bobby.com", "hil123")
      val eric = new User("eric", "Eric", None, "McKnight", Some("Dungeon Defenders"), Gender.Male, "eric@eric.com", "mck123")
      // guardians
      val reg = new User("reg", "Reginald", None, "Pennyworth", Some("Reg"), Gender.Male, null, "pen123")
      val hank = new User("hank", "Hank", None, "Hill", Some("Propane and Propane Accessories"), Gender.Male, null, "hil123")
      pm.makePersistentAll(List(mary, christina, richard, todd,
          jack, fitzgerald, tyler, meriadoc, peregrin, mack, andrew,
          jordan, emma, laura, john, bobby, eric,
          reg, hank))
      
      // Roles
      // teachers
      val maryTeacher = new Teacher(mary, "318508", "4284802048")
      val christinaTeacher = new Teacher(christina, "542358", "8795177958")
      val richardTeacher = new Teacher(richard, "423423", "4478340832")
      val toddTeacher = new Teacher(todd, "323423", "3042093480")
      pm.makePersistentAll(List(maryTeacher, christinaTeacher, richardTeacher, toddTeacher))
      
      // students
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
      val bobbyStud = new Student(bobby, "4235612205", "425451", 12, "Propane Studies")
      pm.makePersistentAll(List(ericStud, jackStud, fitzgeraldStud, tylerStud,
          meriadocStud, peregrinStud, mackStud, andrewStud,
          jordanStud, emmaStud, lauraStud, johnStud, bobbyStud))

      // guardians
      val toddGuardian = new Guardian(todd, None, Set(meriadocStud, peregrinStud))
      val regGuardian = new Guardian(reg, None, Set(fitzgeraldStud))
      val hankGuardian = new Guardian(hank, None, Set(bobbyStud))
      pm.makePersistentAll(List(toddGuardian, regGuardian, hankGuardian))
      
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
        bioA, chemA, alg1A, alg2A, geoA, eng1A, eng2A, eng3A, usHistA, worldHistA,
        bioB, chemB, alg1B, alg2B, geoB, eng1B, eng2B, eng3B, usHistB, worldHistB,
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
        jordanStud -> List(r1chemA, w2geoA, w1eng2A, r2usHistA, r1chemB, w2geoB, w1eng2B, r2usHistB))

      for ((student, sections) <- enrollments) {
        for (sect <- sections) {
          pm.makePersistent(new StudentEnrollment(student, sect, None, None))
        }
      }

      //makeTeacherAssignments
      if (debug) println("Creating teacher assignments to sections...")

      val teacherAssignments = Map(
        maryTeacher -> List(r1plan, r2usHistA, r2usHistB, w1worldHistA, w1worldHistB, w2studySkill),
        christinaTeacher -> List(r1eng1A, r2plan, w1eng2A, w2eng3A, r1eng1B, w1eng2B, w2eng3B),
        toddTeacher -> List(r1alg1A, r2alg2A, w1plan, w2geoA, r1alg1B, r2alg2B, w2geoB),
        richardTeacher -> List(r1chemA, r1chemB, r2chemA, r2chemB, w1bioA, w1bioB, w2bioA, w2bioB))

      for ((teacher, sections) <- teacherAssignments) {
        for (sect <- sections) {
          pm.makePersistent(new TeacherAssignment(teacher, sect, None, None))
        }
      }
      
      val event1 = new Event("September Conferences", false)
      pm.makePersistent(event1);

      val session1 = new Session(event1, new LocalDate(2013, 9, 27), new LocalDateTime(2013, 9, 25, 23, 59, 59), None,
          new LocalTime(9, 0, 0), new LocalTime(15, 0, 0))
      pm. makePersistent(session1)
      val session2 = new Session(event1, new LocalDate(2013, 9, 28), new LocalDateTime(2013, 9, 26, 23, 59, 59), None,
          new LocalTime(13, 0, 0), new LocalTime(18, 0, 0))
      pm.makePersistent(session2)
      
      val ta1_1 = new TeacherActivation(session1, maryTeacher, 15, None)
      val ta1_2 = new TeacherActivation(session1, toddTeacher, 15, None)
      val ta1_3 = new TeacherActivation(session2, maryTeacher, 15, None)
      val ta1_4 = new TeacherActivation(session2, richardTeacher, 15, None)
      pm.makePersistentAll(List(ta1_1, ta1_2, ta1_3, ta1_4))
      
      // TODO: two sessions for one event
      val event2 = new Event("October Conferences", true)
      pm.makePersistent(event2)
      
      val session3 = new Session(event2, new LocalDate(2013, 10, 8), new LocalDateTime(2013, 10, 6, 23, 59, 59), None,
          new LocalTime(7, 40, 0), new LocalTime(14, 20, 0))
      pm.makePersistent(session3)
      
      val ta3 = new TeacherActivation(session3, maryTeacher, 10, None)
      val ta4 = new TeacherActivation(session3, toddTeacher, 10, None)
      val ta5 = new TeacherActivation(session3, richardTeacher, 10, None)
      
      // Permissions
      toddTeacher.addPermission(User.Permissions.ListAll)
      maryTeacher.addPermission(Book.Permissions.Manage)
      maryTeacher.addPermission(Book.Permissions.LookUp)
      richardTeacher.addPermission(Conferences.Permissions.Manage)
      maryTeacher.addPermission(Conferences.Permissions.Manage)
      
      //makeBookData(debug)
      /*
      if (debug) println("Creating the blagosphere")
      // blogs
      val toddTeacherBlog = new Blog("Todd's Blag", toddTeacher)
      val toddGuardianBlog = new Blog("Father O'Bryan's Blog", toddGuardian)
      val tylerBlog = new Blog("Tydar's s'radyT", tylerStud)
      val jordanBlog = new Blog("Jordan doesn't 'Get It(R)'", jordanStud)

      pm.makePersistentAll(List(toddTeacherBlog, toddGuardianBlog, tylerBlog, jordanBlog))

      if (debug) println("Creating Titles...")
    val algebra1Book = new Title("Algebra 1 (Prentice Hall Mathematics)", Some("Bellman, Bragg and Charles"), 
        Some("Pearson Prentice Hall"), "9780130523167", Some(842), 
        Some("10.9 x 8.8 x 1.6 inches"), Some(4.5), true, new Date(System.currentTimeMillis()), None)
    val algebra2Book = new Title("Prentice Hall Mathematics: Algebra 2", Some("Dan Kennedy, Randall I. Charles and Sadie Chavis Bragg"),
        Some("Pearson Prentice Hall"), "9780131339989",Some(900), 
        Some("10.9 x 8.8 x 2 inches"), Some(5.3), true, new Date(System.currentTimeMillis()), None)
    val geometryBook = new Title("Geometry", Some("Harold R. Jacobs"), 
        Some("W.H. Freeman & Company"), "9780716717454", Some(668),
        Some("10.1 x 7.7 x 1.4 inches"), Some(3.3), true, new Date(System.currentTimeMillis()), None)
    val chemistryBook = new Title("Chemistry", Some("Steven S. Zumdahl and Susan A. Zumdahl"), 
        Some("Houghton Mifflin"), "9780618528448", Some(1056), 
        Some("10.6 x 8.5 x 1.6 inches"), Some(5.5), true, new Date(System.currentTimeMillis()), None)
    val biologyBook = new Title("Biology", Some("Neil A. Campbell, Jane B. Reece, Lisa A. Urry and Michael L. Cain"),
        Some("Pearson Benjamin Cummings"), "9780805368444", Some(1393),
        Some("8.5 x 2.1 x 11 inches"), Some(7.6), true, new Date(System.currentTimeMillis()), None)
    val english1Book = new Title("Glencoe Language Arts Grammar And Language Workbook Grade 9", Some("John King"), 
        Some("Glencoe/McGraw-Hill"), "9780028182940", Some(348), 
        Some("10.9 x 8.5 x 0.6 inches"), Some(9.6), true, new Date(System.currentTimeMillis()), None)
    val english2Book = new Title("Prentice Hall Literature Penguin: Grade 10: Student Edition (NATL)", Some("Todd O'Bryan"),
        Some("PRENTICE HALL"), "9780131317185", Some(1163), 
        Some("10 x 7.9 x 1.8 inches"), Some(5.2), true, new Date(System.currentTimeMillis()), None)
    val english3Book = new Title("Language of Literature, Grade 11 ", Some("Pat Day"),
        Some("McDougal Littel"), "9780395931813", Some(1408),
        Some("1 x 0.8 x 0.2 inches"), Some(6.0), true, new Date(System.currentTimeMillis()), None)
    val worldHistoryBook = new Title("World History: Patterns of Interaction: Atlas by Rand McNally", Some("Roger B. Beck, Linda Black and Larry S. Krieger"),
        Some("Mcdougal Littell/Houghton Mifflin"), "9780618690084", Some(1376), 
        Some("11.2 x 8.7 x 1.8 inches"), Some(6.8), true, new Date(System.currentTimeMillis()), None)
    val usHistoryBook = new Title("The American Pageant", Some("David M. Kennedy and Lizabeth Cohen"), 
        Some("Wadsworth Publishing"), "9781111349530", Some(1152), 
        Some("11 x 8.8 x 1.6 inches"), Some(5.2), true, new Date(System.currentTimeMillis()), None)


      //makeMasteryData
      mastery.QuizData.load(debug)

      //makeLockerData(debug)
      if (debug) println("Creating Lockers...")
      val locker1 = new Locker(15, "23-96-23", LockerLocation(1, "CW"), None, false)
      val locker2 = new Locker(16, "31-09-42", LockerLocation(1, "CW"), None, false)
      val locker3 = new Locker(17, "91-23-68", LockerLocation(1, "CW"), None, false)
      val locker4 = new Locker(18, "79-45-82", LockerLocation(1, "CW"), None, false)
      val locker5 = new Locker(19, "21-16-55", LockerLocation(1, "CW"), None, false)
      val locker6 = new Locker(20, "50-61-36", LockerLocation(1, "CW"), None, false)
      val locker7 = new Locker(21, "74-13-89", LockerLocation(1, "CW"), None, false)
      val locker8 = new Locker(22, "66-66-66", LockerLocation(1, "CW"), None, false)
      val locker9 = new Locker(23, "32-82-42", LockerLocation(1, "CW"), None, false)
      val locker10 = new Locker(24, "03-08-16", LockerLocation(2, "SE"), None, false)
      val lockerList = List(locker1, locker2, locker3, locker4, locker5, locker6, locker7, locker8, locker9, locker10)

      pm.makePersistentAll(lockerList)

      //makeConferenceData(debug)
      /*if(debug) println("Creating Conferences...")
    val springConf = new Event("Spring Conferences", true)
    val springSession = new Session(springConf, isoDate.parseLocalDate("2013-04-01"), isoDatetime.parseLocalDateTime("2013-04-21 23:59:59"), 
        Some(isoDatetime.parseLocalDateTime("2013-04-01 23:59:59")), Time.valueOf("00:00:00"), Time.valueOf("23:59:59"), 10)
    val firstSlot = new Slot(springSession, Teacher.getByUsername("736052").asInstanceOf[Teacher], Student.getByUsername("RASHAH01").asInstanceOf[Student], 
        Time.valueOf("12:00:00"), "Mark Shah", "fakeemail@n00b.com", "5025555555", null, null)
    }*/
      //makeCategories    
      if (debug) println("Creating Categories...")
      val r1alg1AQuizzes = new Category("Quizzes", r1alg1A, .1)
      val r1alg1ATests = new Category("Tests", r1alg1A, .1)
      val r1alg1AHomework = new Category("Homework", r1alg1A, .1)
      val r1alg1AClasswork = new Category("Classwork", r1alg1A, .1)
      val r1alg1AParticipation = new Category("Participation", r1alg1A, .6)

      val r2usHistAQuizzes = new Category("Quizzes", r2usHistA, .2)
      val r2usHistATests = new Category("Tests", r2usHistA, .35)
      val r2usHistAHomework = new Category("Homework", r2usHistA, .2)
      val r2usHistAAPPractice = new Category("AP Practice", r2usHistA, .2)
      val r2usHistAConduct = new Category("Conduct", r2usHistA, .05)

      val w2bioAQuizzes = new Category("Quizzes", w2bioA, .25)
      val w2bioATests = new Category("Tests", w2bioA, .4)
      val w2bioAHomework = new Category("Homework", w2bioA, .2)
      val w2bioALabs = new Category("Labs", w2bioA, .15)

      pm.makePersistentAll(List(r1alg1AQuizzes, r1alg1ATests, r1alg1AHomework, r1alg1AClasswork, r1alg1AParticipation,
        r2usHistAQuizzes, r2usHistATests, r2usHistAHomework, r2usHistAAPPractice,
        r2usHistAConduct, w2bioAQuizzes, w2bioATests, w2bioAHomework, w2bioALabs))

      //makeAssignments
      if (debug) println("Creating Assignments...")

      // r2usHistA Assignments
      val guildedAgeQuiz = new Assignment("Gilded Age Quiz", 25, isoDate.parseLocalDate("2012-03-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAQuizzes)
      val lincolnFavoriteFooodsQuiz = new Assignment("Lincoln's Favorite Foods Quiz", 35, isoDate.parseLocalDate("2012-03-05"), isoDatetime.parseLocalDateTime("2012-03-01 23:59:59"), r2usHistAQuizzes)

      val civilWarTest = new Assignment("Civil War Test", 20000, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistATests)
      val secondCivilWarTest = new Assignment("Second Civil War Test: East Coast vs West Coast Hip Hop", 200000, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistATests)
      val thirdCivilWarTest = new Assignment("Third Civil War Test: Northeasterly Residents vs Middle Southwest Utah", 20, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistATests)

      val aLVHReview = new Assignment("Abraham Lincoln Vampire Hunter Review", 1, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAHomework)
      val theHistoryOfUSHistory = new Assignment("Worksheet: The History of US History", 2, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAHomework)
      val robotsAndKoreanWar = new Assignment("Paper: Advanced Androids Behind the Korean War", 5, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAHomework)
      val georgeWashingtonvsGodzilla = new Assignment("George Washington: Savior of Our Union", 8, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAHomework)

      val apPractice1 = new Assignment("AP Practice 1", 111111, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAAPPractice)

      val conduct4 = new Assignment("4th 6 weeks Conduct", 10, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAConduct)
      val conduct5 = new Assignment("5th 6 weeks Conduct", 4, isoDate.parseLocalDate("2012-02-05"), isoDatetime.parseLocalDateTime("2012-03-13 23:59:59"), r2usHistAConduct)

      // r1alg1A Assignments
      val ass1 = new Assignment("Chaper 12.1 Quiz", 30, isoDate.parseLocalDate("2012-10-31"), isoDatetime.parseLocalDateTime("2012-10-31 23:59:59"), r1alg1AQuizzes)
      val ass2 = new Assignment("Chaper 12.2 Quiz", 30, isoDate.parseLocalDate("2012-11-07"), isoDatetime.parseLocalDateTime("2012-11-07 23:59:59"), r1alg1AQuizzes)
      val ass3 = new Assignment("Chaper 12.3 Quiz", 30, isoDate.parseLocalDate("2012-11-14"), isoDatetime.parseLocalDateTime("2012-11-14 23:59:59"), r1alg1AQuizzes)
      val ass4 = new Assignment("Chaper 12.4 Quiz", 30, isoDate.parseLocalDate("2012-11-21"), isoDatetime.parseLocalDateTime("2012-11-21 23:59:59"), r1alg1AQuizzes)

      val ass5 = new Assignment("Chaper 12 Test", 100, isoDate.parseLocalDate("2012-11-30"), isoDatetime.parseLocalDateTime("2012-11-30 23:59:59"), r1alg1ATests)

      val ass6 = new Assignment("Chapter 12.1 Homework", 10, isoDate.parseLocalDate("2012-10-28"), isoDatetime.parseLocalDateTime("2012-10-31 23:59:59"), r1alg1AHomework)
      val ass7 = new Assignment("Chapter 12.2 Homework", 10, isoDate.parseLocalDate("2012-11-24"), isoDatetime.parseLocalDateTime("2012-11-27 23:59:59"), r1alg1AHomework)
      val ass8 = new Assignment("Chapter 12.3 Homework", 10, isoDate.parseLocalDate("2012-11-11"), isoDatetime.parseLocalDateTime("2012-11-14 23:59:59"), r1alg1AHomework)
      val ass9 = new Assignment("Chapter 12.4 Homework", 10, isoDate.parseLocalDate("2012-11-18"), isoDatetime.parseLocalDateTime("2012-11-21 23:59:59"), r1alg1AHomework)

      val ass10 = new Assignment("Week 10 Participation", 10, isoDate.parseLocalDate("2012-10-31"), isoDatetime.parseLocalDateTime("2012-11-27 23:59:59"), r1alg1AParticipation)

      val ass11 = new Assignment("Chapter 12 Review", 15, isoDate.parseLocalDate("2012-11-23"), isoDatetime.parseLocalDateTime("2012-11-25 23:59:59"), r1alg1AClasswork)

      // w2bioA Assignments
      val carbonQuiz = new Assignment("Carbon Quiz", 40, isoDate.parseLocalDate("2012-11-27"), isoDatetime.parseLocalDateTime("2012-11-27 23:59:59"), w2bioAQuizzes)
      val popQuiz = new Assignment("Surprise Muthatrucka", 25, isoDate.parseLocalDate("2012-11-11"), isoDatetime.parseLocalDateTime("2012-11-11 23:59:59"), w2bioAQuizzes)
      val cellQuiz = new Assignment("Cell Quiz", 40, isoDate.parseLocalDate("2012-11-17"), isoDatetime.parseLocalDateTime("2012-11-17 23:59:59"), w2bioAQuizzes)

      val carbonTest = new Assignment("Carbon Test", 100, isoDate.parseLocalDate("2012-11-10"), isoDatetime.parseLocalDateTime("2012-11-10 23:59:59"), w2bioATests)
      val cellTest = new Assignment("Cell Test", 100, isoDate.parseLocalDate("2012-11-20"), isoDatetime.parseLocalDateTime("2012-11-20 23:59:59"), w2bioATests)

      val wordSearch = new Assignment("Word Search", 10, isoDate.parseLocalDate("2012-10-31"), isoDatetime.parseLocalDateTime("2012-11-23 23:59:59"), w2bioAHomework)
      val bookReading = new Assignment("Book Reading", 5, isoDate.parseLocalDate("2012-11-23"), isoDatetime.parseLocalDateTime("2012-11-25 23:59:59"), w2bioAHomework)
      val carbonWorksheet = new Assignment("Carbon Worksheet", 20, isoDate.parseLocalDate("2012-11-25"), isoDatetime.parseLocalDateTime("2012-11-27 23:59:59"), w2bioAHomework)
      val bondWorksheet = new Assignment("Bond Worksheet", 15, isoDate.parseLocalDate("2012-11-10"), isoDatetime.parseLocalDateTime("2012-11-12 23:59:59"), w2bioAHomework)
      val takeHomeProblems = new Assignment("Take Home Problems", 20, isoDate.parseLocalDate("2012-11-12"), isoDatetime.parseLocalDateTime("2012-11-14 23:59:59"), w2bioAHomework)
      val cellWorksheet = new Assignment("Cell Worksheet", 20, isoDate.parseLocalDate("2012-11-15"), isoDatetime.parseLocalDateTime("2012-11-16 23:59:59"), w2bioAHomework)

      val carbonExperiment = new Assignment("Carbon Experiment", 30, isoDate.parseLocalDate("2012-11-25"), isoDatetime.parseLocalDateTime("2012-11-27 23:59:59"), w2bioALabs)
      val cellLab = new Assignment("Cell Lab", 30, isoDate.parseLocalDate("2012-11-15"), isoDatetime.parseLocalDateTime("2012-11-17 23:59:59"), w2bioALabs)

      pm.makePersistentAll(List(guildedAgeQuiz, lincolnFavoriteFooodsQuiz, civilWarTest, secondCivilWarTest,
        thirdCivilWarTest, aLVHReview, theHistoryOfUSHistory, robotsAndKoreanWar,
        georgeWashingtonvsGodzilla, apPractice1, conduct4, conduct5, ass1, ass2, ass3, ass4,
        ass5, ass6, ass7, ass8, ass9, ass10, ass11, carbonQuiz, popQuiz, cellQuiz, carbonTest,
        cellTest, wordSearch, bookReading, carbonWorksheet, bondWorksheet, takeHomeProblems,
        cellWorksheet, carbonExperiment, cellLab))

      //makeAnnouncements
      if (debug) println("Creating Announcements...")

      //r2usHistA Announcements
      val ann1 = new Announcement(maryTeacher, r2usHistA, isoDatetime.parseLocalDateTime("2012-08-15 14:35:21"),
        "HEY KIDS! FIRST DAY OF CLASS AND I'M SOOOO EXCITED!")
      val ann2 = new Announcement(maryTeacher, r2usHistA, isoDatetime.parseLocalDateTime("2012-09-15 04:45:25"),
        "Review sheet #2 and old-school free-response. See you on Monday")
      val ann3 = new Announcement(maryTeacher, r2usHistA, isoDatetime.parseLocalDateTime("2012-09-15 14:34:25"),
        "hksgkdnf")
      val ann4 = new Announcement(maryTeacher, r2usHistA, isoDatetime.parseLocalDateTime("2012-10-15 23:35:21"),
        "I apologize to my students for showing up to class trapped in a plastic bag. I realize it severely impaired my teaching ability.")
      val ann5 = new Announcement(maryTeacher, r2usHistA, isoDatetime.parseLocalDateTime("2012-10-30 14:23:51"),
        "All further announcements will be copied from Mr. Purvis's Edmodo")
      val ann6 = new Announcement(maryTeacher, r2usHistA, isoDatetime.parseLocalDateTime("2012-10-31 00:00:00"), "Hi")
      val ann7 = new Announcement(maryTeacher, r2usHistA, isoDatetime.parseLocalDateTime("2012-11-01 19:19:19"),
        "Due Wednesday: 2011 Form B Free-Response Questions. Please bring your responses and" +
          "scores for each of the questions (scoring rubrics are posted below)\nDue Friday: Series" +
          "Exam Test corrections. Left column--what i did wrong; right column--what i should have done." +
          "See multiple choice questions below.")

      pm.makePersistentAll(List(ann1, ann2, ann3, ann4, ann5, ann6, ann7))

      if (debug) println("Creating Turnins...")

      //merry, mack, fitz, jordan

      val ti1 = new Turnin(meriadocStud, isoDatetime.parseLocalDateTime("2012-08-15 14:35:21"), guildedAgeQuiz, 98.0)
      val ti2 = new Turnin(mackStud, isoDatetime.parseLocalDateTime("2012-08-15 14:35:22"), guildedAgeQuiz, 100)
      val ti3 = new Turnin(fitzgeraldStud, isoDatetime.parseLocalDateTime("2012-08-15 14:35:23"), guildedAgeQuiz, 77)
      val ti4 = new Turnin(jordanStud, isoDatetime.parseLocalDateTime("2012-08-15 14:35:23"), guildedAgeQuiz, 40)

      val ti5 = new Turnin(meriadocStud, isoDatetime.parseLocalDateTime("2012-08-19 14:35:21"), aLVHReview, 67)
      val ti6 = new Turnin(mackStud, isoDatetime.parseLocalDateTime("2012-08-19 14:35:22"), aLVHReview, 54)
      val ti7 = new Turnin(fitzgeraldStud, isoDatetime.parseLocalDateTime("2012-08-19 14:35:23"), aLVHReview, 80)
      val ti8 = new Turnin(jordanStud, isoDatetime.parseLocalDateTime("2012-08-20 14:35:23"), aLVHReview, 94.33)

      pm.makePersistentAll(List(ti1, ti2, ti3, ti5, ti6, ti7, ti8))
	  */
      //TODO: make test data for announcements and gradebook
    }
  }

}
