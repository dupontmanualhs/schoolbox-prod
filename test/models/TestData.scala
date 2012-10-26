package models

import java.io.File
import java.sql.Date

import org.joda.time.LocalDate

import javax.jdo.annotations.Inheritance
import javax.jdo.annotations.PersistenceCapable
import javax.jdo.annotations.Unique
import models.blogs.Blog
import models.books.Title
import models.courses.AcademicYear
import models.courses.Course
import models.courses.Department
import models.courses.Period
import models.courses.Room
import models.courses.Section
import models.courses.StudentEnrollment
import models.courses.TeacherAssignment
import models.courses.Term
import models.mastery.M
import models.mastery.Question
import models.mastery.QuestionSet
import models.mastery.Quiz
import models.mastery.QuizSection
import models.users.Gender
import models.users.Guardian
import models.users.Student
import models.users.Teacher
import models.users.User
import util.DataStore
import util.ScalaPersistenceManager

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
    val bobby = new User("bobby", "Bobby", None, "Hill", Some("Dangit Bobby"), Gender.MALE, "bobby@bobby.com", "hil123")
    val eric = new User("eric", "Eric", None, "McKnight", Some("Dungeon Defenders"), Gender.MALE, "eric@eric.com", "mck123")
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

    // guardians
    val reg = new User("reg", "Reginald", None, "Pennyworth", Some("Reg"), Gender.MALE, null, "pen123")
    val hank = new User("hank", "Hank", None, "Hill", Some("Propane and Propane Accessories"), Gender.MALE, null, "hil123")
    val toddGuardian = new Guardian(todd, Set(meriadocStud, peregrinStud))
    val regGuardian = new Guardian(reg, Set(fitzgeraldStud))
    val hankGuardian = new Guardian(hank, Set(bobbyStud))

    if (debug) println("Creating the blagosphere")
    // blogs
    val toddTeacherBlog = new Blog("Todd's Blag", toddTeacher)
    val toddGuardianBlog = new Blog("Father O'Bryan's Blog", toddGuardian)
    val tylerBlog = new Blog("Tydar's s'radyT", tylerStud)
    val jordanBlog = new Blog("Jordan doesn't 'Get It(R)'", jordanStud)

    pm.makePersistentAll(List(
      mary, christina, richard, todd,
      maryTeacher, christinaTeacher, richardTeacher, toddTeacher,
      jack, john, fitzgerald, emma, laura, tyler, jordan, andrew, mack, meriadoc, peregrin, eric,
      ericStud, johnStud, fitzgeraldStud, emmaStud, lauraStud, tylerStud, jordanStud, jackStud, andrewStud, mackStud, meriadocStud, peregrinStud,
      reg, toddGuardian, regGuardian, bobby, bobbyStud, hank, hankGuardian, toddTeacherBlog, toddGuardianBlog, tylerBlog, jordanBlog))

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
        pm.makePersistent(new StudentEnrollment(student, sect, null, null))
      }
    }

    //makeTeacherAssignments
    if (debug) println("Creating teacher assignments to sections...")

    val assignments = Map(
      maryTeacher -> List(r1plan, r2usHistA, r2usHistB, w1worldHistA, w1worldHistB, w2studySkill),
      christinaTeacher -> List(r1eng1A, r2plan, w1eng2A, w2eng3A, r1eng1B, w1eng2B, w2eng3B),
      toddTeacher -> List(r1alg1A, r2alg2A, w1plan, w2geoA, r1alg1B, r2alg2B, w2geoB),
      richardTeacher -> List(r1chemA, r1chemB, r2chemA, r2chemB, w1bioA, w1bioB, w2bioA, w2bioB))

    for ((teacher, sections) <- assignments) {
      for (sect <- sections) {
        pm.makePersistent(new TeacherAssignment(teacher, sect, null, null))
      }
    }

    //makeBookData(debug)
    if (debug) println("Creating Titles...")
    val algebra1Book = new Title("Algebra 1 (Prentice Hall Mathematics)", "Bellman, Bragg and Charles", "Pearson Prentice Hall", "978-0130523167", 842, "10.9 x 8.8 x 1.6 inches", 4.5, true, new Date(System.currentTimeMillis()))
    val algebra2Book = new Title("Prentice Hall Mathematics: Algebra 2", "Dan Kennedy, Randall I. Charles and Sadie Chavis Bragg", "Pearson Prentice Hall", "978-0131339989", 900, "10.9 x 8.8 x 2 inches", 5.3, true, new Date(System.currentTimeMillis()))
    val geometryBook = new Title("Geometry", "Harold R. Jacobs", "W.H. Freeman & Company", "978-0716717454", 668, "10.1 x 7.7 x 1.4 inches", 3.3, true, new Date(System.currentTimeMillis()))
    val chemistryBook = new Title("Chemistry", "Steven S. Zumdahl and Susan A. Zumdahl", "Houghton Mifflin", "978-0618528448", 1056, "10.6 x 8.5 x 1.6 inches", 5.5, true, new Date(System.currentTimeMillis()))
    val biologyBook = new Title("Biology", "Neil A. Campbell, Jane B. Reece, Lisa A. Urry and Michael L. Cain", " Pearson Benjamin Cummings", "978-0805368444", 1393, "8.5 x 2.1 x 11 inches", 7.6, true, new Date(System.currentTimeMillis()))
    val english1Book = new Title("Glencoe Language Arts Grammar And Language Workbook Grade 9", "John King", " Glencoe/McGraw-Hill", "978-0028182940", 348, "10.9 x 8.5 x 0.6 inches", 9.6, true, new Date(System.currentTimeMillis()))
    val english2Book = new Title("Prentice Hall Literature Penguin: Grade 10: Student Edition (NATL)", "Todd O'Bryan", "PRENTICE HALL", "978-0131317185", 1163, "10 x 7.9 x 1.8 inches", 5.2, true, new Date(System.currentTimeMillis()))
    val english3Book = new Title("Language of Literature, Grade 11 ", "Pat Day", "McDougal Littel", "978-0395931813", 1408, "1 x 0.8 x 0.2 inches", 6.0, true, new Date(System.currentTimeMillis()))
    val worldHistoryBook = new Title("World History: Patterns of Interaction: Atlas by Rand McNally", "Roger B. Beck, Linda Black and Larry S. Krieger", "Mcdougal Littell/Houghton Mifflin", "978-0618690084", 1376, "11.2 x 8.7 x 1.8 inches", 6.8, true, new Date(System.currentTimeMillis()))
    val usHistoryBook = new Title("The American Pageant", "David M. Kennedy and Lizabeth Cohen", "Wadsworth Publishing", "978-1111349530", 1152, "11 x 8.8 x 1.6 inches", 5.2, true, new Date(System.currentTimeMillis()))

    //makeMasteryData
    if (debug) println("Creating Masteries")
    val exponents1 = new Question("You can only add monomials if they have the same _____", List("bases with the same exponents", "bases with the same exponents."))
    val exponents2parta = new Question("When you are adding monomials, ____ coefficients", List("add"))
    val exponents2partb = new Question("When you are adding monomials, the bases and exponents _____", List("stay the same", "stay the same."))
    val exponents3 = new Question("When you are multiplying monomials, ____ the coefficients, and ____ the exponents whose ____ are the ____. (List answers with commas)", List("multiply,add,bases,same"))
    val exponents4 = new Question("When you are raising monomials to a power, ____ coefficients ______ and ____ the exponents inside the parenthesis by _____. (List answers with commas)", List("raise, to the power outside, multiply, the exponents outside the ()"))
    val exponents5 = new Question("When you are dividing monomials, _____ the coefficients by the ____ and _____ the exponents whose ____ are the ____. (List answers with commas)", List("divide, GCF, subtract, bases, same", "divide, gcf, subtract, bases, same", "divide, GCF, cancel, same", "divide, gcf, cancel, bases, same"))
    val exponents6a = new Question(M("5a+a="), List("6a", "6*a"))
    val exponents6b = new Question(M("3a+a="), List("4a", "4*a"))
    val exponents6c = new Question(M("-4a+a="), List("-3a", "-3*a"))
    val exponents6d = new Question(M("-5a+a="), List("-4a", "-4*a"))
    val exponents7a = new Question(M("(a^5)^4="), List("a^{20}"))
    val exponents7b = new Question(M("(a^3)^4="), List("a^{12}"))
    val exponents7c = new Question(M("(a^2)^4="), List("a^8"))
    val exponents7d = new Question(M("(a^4)^3="), List("a^{12}"))
    val exponents7e = new Question(M("(a^5)^3="), List("a^{15}"))
    val exponents8a = new Question(M("a^{-3}="), List("\\frac{1}{a^3}"))
    val exponents8b = new Question(M("a^{-5}="), List("\\frac{1}{a^5}"))
    val exponents8c = new Question(M("a^{-4}="), List("\\frac{1}{a^4}"))
    val exponents8d = new Question(M("a^{-6}="), List("\\frac{1}{a^6}"))
    val exponents9a = new Question(M("\\frac{a^4}{a^7}="), List("\\frac{1}{a^3}"))
    val exponents9b = new Question(M("a^3/a^5="), List("1/a^2"))
    val exponents9c = new Question(M("a^2/a^7="), List("1/a^5"))
    val exponents9d = new Question(M("a^3/a^7="), List("1/a^4"))
    val exponents9e = new Question(M("a^5/a^7="), List("1/a^2"))
    val exponents9f = new Question(M("a^5/a^9="), List("1/a^4"))
    val exponents10a = new Question(M("a\\cdot a\\cdot a="), List("a^3"))
    val exponents10b = new Question(M("a\\cdot a="), List("a^2"))
    val exponents11a = new Question(M("3a^3+a^3+a^2"), List("4a^3+a^2", "a^2+4a^3"))
    val exponents11b = new Question(M("a^3+a^3+a^2"), List("2a^3+a^2", "a^2+2a^3"))
    val exponents11c = new Question(M("2a^3+a^3+a^2"), List("3a^3+a^2", "a^2+3a^3"))
    val exponents11d = new Question(M("2a^3+2a^3+2a^2"), List("4a^3+2a^2", "2a^2+4a^3"))
    val exponents12a = new Question(M("7a*2a"), List("14a^2"))
    val exponents12b = new Question(M("3a*4a"), List("12a^2"))
    val exponents12c = new Question(M("5a*4a"), List("20a^2"))
    val exponents12d = new Question(M("7a*3a"), List("21a^2"))
    val exponents12e = new Question(M("6a*3a"), List("18a^2"))
    val exponents12f = new Question(M("6a*5a"), List("30a^2"))
    val exponents13a = new Question(M("-6m^2n+3m^2n"), List("-3m^2n", "-3nm^2"))
    val exponents13b = new Question(M("-7m^2n+3m^2n"), List("-4m^2n", "-4nm^2"))
    val exponents13c = new Question(M("-5m^2n+3m^2n"), List("-2m^2n", "-2nm^2"))
    val exponents13d = new Question(M("-5m^2n+8m^2n"), List("3m^2n", "3nm^2"))
    val exponents13e = new Question(M("-6m^2n+8m^2n"), List("2m^2n", "2nm^2"))
    val exponents14a = new Question(M("(-3a^5)^3"), List("-27a^15"))
    val exponents14b = new Question(M("(-5a^2)^3"), List("-125a^6"))
    val exponents14c = new Question(M("(-5a^4)^3"), List("-125a^12"))
    val exponents14d = new Question(M("(-4a^5)^3"), List("-64a^15"))
    val exponents14e = new Question(M("(-3a^7)^3"), List("-27a^21"))
    val exponents15a = new Question(M("-5m^4n*-4mn^3"), List("20m^5n^4", "20n^4m^5"))
    val exponents15b = new Question(M("-5m^2n*-2mn^3"), List("10m^3n^4", "10n^4m^3"))
    val exponents15c = new Question(M("-5m^4n*-3mn^3"), List("15m^5n^4", "15n^4m^5"))
    val exponents15d = new Question(M("-5m^4n*-2mn^3"), List("10m^5n^4", "10n^4m^5"))
    val exponents15e = new Question(M("-5m^4n*-3mn^4"), List("15m^5n^5", "15n^5m^5"))
    val exponents16a = new Question(M("-5m^2n-2mn^3"), List("A/S", "a/s"))
    val exponents16b = new Question(M("-5m^2n-2mn"), List("A/S", "a/s"))
    val exponents16c = new Question(M("-5m^2n-4mn^3"), List("A/S", "a/s"))
    val exponents17a = new Question(M("a^(-2)/a^(-7)"), List("a^5"))
    val exponents17b = new Question(M("a^(-2)/a^(-5)"), List("a^3"))
    val exponents17c = new Question(M("a^(-4)/a^(-6)"), List("a^2"))
    val exponents17d = new Question(M("a^(-3)/a^(-7)"), List("a^4"))
    val exponents17e = new Question(M("a^(-3)/a^(-8)"), List("a^5"))
    val exponents18a = new Question(M("3m^(-3)/21m^3n^(-2)"), List("n^2/7m^6"))
    val exponents18b = new Question(M("3m^(-5)/15m^3n^(-4)"), List("n^4/5m^8"))
    val exponents18c = new Question(M("3m^(-3)/18m^3n^(-5)"), List("n^5/6m^6"))
    val exponents18d = new Question(M("6m^(-3)/18m^4n^(-5)"), List("n^5/3m^7"))
    val exponents18e = new Question(M("15m^(-3)/18m^4n^(-2)"), List("5n^2/6m^7"))
    val exponents19a = new Question(M("(3a^3b^2)^4="), List("81a^12b^8", "81b^8a^12"))
    val exponents19b = new Question(M("(2a^3b^2)^4="), List("16a^12b^8", "16b^8a^12"))
    val exponents19c = new Question(M("(2a^4b^2)^5="), List("32a^20b^10", "32b^10a^20"))
    val exponents19d = new Question(M("(2a^4b^3)^5="), List("32a^20b^15", "32b^15a^20"))
    val exponents19e = new Question(M("(2a^3b^7)^5="), List("32a^15b^35", "32b^35a^15"))
    val exponents20a = new Question(M("(-6m^2n^4)/(2m^4n)="), List("-3n^3/m^2"))
    val exponents20b = new Question(M("(-6m^2n^4)/(3m^5n^(-1))="), List("-2n^5/m^3"))
    val exponents20c = new Question(M("(-10m^4n^4)/(2m^5n^(-2))="), List("-5n^6/m"))
    val exponents20d = new Question(M("(-10m^4n^4)/(20m^4n^(-2))="), List("-n^6/2", "-1/2n^6", "-0.5n^6", "-.5n^6"))
    val exponents20e = new Question(M("(-14m^4n^4)/(20m^4n^(-5))="), List("-7n^9/10", "-7/10n^9", "(-7/10)n^9"))
    val exponents21a = new Question(M("(2a-5b)^2="), List("4a^2-20ab+25b^2", "25b^2-20ab+4a^2", "4a^2+25b^2-20ab", "25b^2+4a^2-20ab", "-20ab+25b^2+4a^2", "-20ab+4a^2+25b^2"))
    val exponents21b = new Question(M("(3a-2b)^2="), List("9a^2-12ab+4b^2", "4b^2-20ab+9a^2", "9a^2+4b^2-12ab", "4b^2+9a^2-12ab", "-12ab+4b^2+9a^2", "-12ab+9a^2+4b^2"))
    val exponents21c = new Question(M("(a+b)^2="), List("a^2+2ab+b^2", "b^2+2ab+a^2", "a^2+b^2+2ab", "b^2+a^2+2ab", "2ab+b^2+a^2", "2ab+a^2+b^2"))
    val exponents21d = new Question(M("(3a+4b)^2="), List("9a^2+24ab+16b^2", "16b^2+24ab+9a^2", "9a^2+16b^2+24ab", "16b^2+9a^2+24ab", "24ab+16b^2+9a^2", "24ab+9a^2+16b^2"))
    val exponents21e = new Question(M("(5a+4b)^2="), List("25a^2+40ab+16b^2", "16b^2+40ab+25a^2", "25a^2+16b^2+40ab", "16b^2+25a^2+40ab", "40ab+16b^2+25a^2", "40ab+25a^2+16b^2"))
    val exponents22a = new Question(M("(6a+9b)/21a="), List("(2a+3b)/7a", "2/7+3b/7a"))
    val exponents22b = new Question(M("(6a+10b)/4a="), List("(3a+5b)/2a", "3/2+5b/2a"))
    val exponents22c = new Question(M("(6a^2+10ab)/4a="), List("(3a+5b)/2", "3a/2+5b/2"))
    val exponents22d = new Question(M("(6a+12ab)/4a="), List("(3+6b)/2", "3/2+3b"))
    val exponents22e = new Question(M("(6a+12ab)/10a="), List("(3+6b)/5", "3/5+6b/5"))
    val exponents22f = new Question(M("(8a+12ab)/10a="), List("(4+6b)/5", "4/5+6b/5", "4/5+(6/5)b"))
    val exponents23a = new Question(M("(10a^3+15a^2)/5a^3="), List("(2a+3)/a", "2+3/a"))
    val exponents23b = new Question(M("(10a^3+15a^2)/5a^4="), List("(2a+3)/a^2", "2/a+3/a^2"))
    val exponents23c = new Question(M("(20a^3+15a^2)/5a^4="), List("(4a+3)/a^2", "4/a+3/a^2"))
    val exponents23d = new Question(M("(10a^3+15a^2)/10a^3="), List("(2a+3)/2a", "1+3/2a"))
    val exponents23e = new Question(M("(10a^3+15a^2)/10a^2="), List("(2a+3)/2", "a+3/2", "3/2+a"))
    val exponents23f = new Question(M("(10a^3+15a^2)/25a="), List("(2a^2+3a)/5", "2a^2/5+3a/5", "3a/5+2a^2/5"))
    pm.makePersistentAll(List(exponents1, exponents2parta, exponents2partb, exponents3, exponents4, exponents5, exponents6a, exponents6b, exponents6c,
      exponents6d, exponents7a, exponents7b, exponents7c, exponents7d, exponents7e, exponents8a, exponents8b, exponents8c, exponents8d, exponents9a,
      exponents9b, exponents9c, exponents9d, exponents9e, exponents9f, exponents10a, exponents10b, exponents11a, exponents11b, exponents11c, exponents11d,
      exponents12a, exponents12b, exponents12c, exponents12d, exponents12e, exponents12f, exponents13a, exponents13b, exponents13c, exponents13d, exponents13e,
      exponents14a, exponents14b, exponents14c, exponents14d, exponents14e, exponents15a, exponents15b, exponents15c, exponents15d, exponents15e, exponents16a,
      exponents16b, exponents16c, exponents17a, exponents17b, exponents17c, exponents17d, exponents17e, exponents18a, exponents18b, exponents18c, exponents18d,
      exponents18e, exponents19a, exponents19b, exponents19c, exponents19d, exponents19e, exponents20a, exponents20b, exponents20c, exponents20d, exponents20e,
      exponents21a, exponents21b, exponents21c, exponents21d, exponents21e, exponents22a, exponents22b, exponents22c, exponents22d, exponents22e, exponents22f,
      exponents23a, exponents23b, exponents23c, exponents23d, exponents23e, exponents23f))
    val exponentsQset1 = new QuestionSet(List(exponents1))
    val exponentsQset2a = new QuestionSet(List(exponents2parta))
    val exponentsQset2b = new QuestionSet(List(exponents2partb))
    val exponentsQset3 = new QuestionSet(List(exponents3))
    val exponentsQset4 = new QuestionSet(List(exponents4))
    val exponentsQset5 = new QuestionSet(List(exponents5))
    val exponentsQset6 = new QuestionSet(List(exponents6a, exponents6b, exponents6c, exponents6d))
    val exponentsQset7 = new QuestionSet(List(exponents7a, exponents7b, exponents7c, exponents7d, exponents7e))
    val exponentsQset8 = new QuestionSet(List(exponents8a, exponents8b, exponents8c, exponents8d))
    val exponentsQset9 = new QuestionSet(List(exponents9a, exponents9b, exponents9c, exponents9d, exponents9e, exponents9f))
    val exponentsQset10 = new QuestionSet(List(exponents10a, exponents10b))
    val exponentsQset11 = new QuestionSet(List(exponents11a, exponents11b, exponents11c, exponents11d))
    val exponentsQset12 = new QuestionSet(List(exponents12a, exponents12b, exponents12c, exponents12d, exponents12e, exponents12f))
    val exponentsQset13 = new QuestionSet(List(exponents13a, exponents13b, exponents13c, exponents13d, exponents13e))
    val exponentsQset14 = new QuestionSet(List(exponents14a, exponents14b, exponents14c, exponents14d, exponents14e))
    val exponentsQset15 = new QuestionSet(List(exponents15a, exponents15b, exponents15c, exponents15d, exponents15e))
    val exponentsQset16 = new QuestionSet(List(exponents16a, exponents16b, exponents16c))
    val exponentsQset17 = new QuestionSet(List(exponents17a, exponents17b, exponents17c, exponents17d, exponents17e))
    val exponentsQset18 = new QuestionSet(List(exponents18a, exponents18b, exponents18c, exponents18d, exponents18e))
    val exponentsQset19 = new QuestionSet(List(exponents19a, exponents19b, exponents19c, exponents19d, exponents19e))
    val exponentsQset20 = new QuestionSet(List(exponents20a, exponents20b, exponents20c, exponents20d, exponents20e))
    val exponentsQset21 = new QuestionSet(List(exponents21a, exponents21b, exponents21c, exponents21d, exponents21e))
    val exponentsQset22 = new QuestionSet(List(exponents22a, exponents22b, exponents22c, exponents22d, exponents22e, exponents22f))
    val exponentsQset23 = new QuestionSet(List(exponents23a, exponents23b, exponents23c, exponents23d, exponents23e, exponents23f))
    pm.makePersistentAll(List(exponentsQset1, exponentsQset2a, exponentsQset2b, exponentsQset3, exponentsQset4, exponentsQset5, exponentsQset6, exponentsQset7,
      exponentsQset8, exponentsQset9, exponentsQset10, exponentsQset11, exponentsQset12, exponentsQset13, exponentsQset14, exponentsQset15, exponentsQset16, exponentsQset17,
      exponentsQset18, exponentsQset19, exponentsQset20, exponentsQset21, exponentsQset22, exponentsQset23))
    val ExponentSection1 = new QuizSection("Sentences", "Complete the sentences below:", List(exponentsQset1, exponentsQset2a, exponentsQset2b, exponentsQset3, exponentsQset4, exponentsQset5))
    val ExponentSection2 = new QuizSection("Simplifing", "Simplify the following:", List(exponentsQset6, exponentsQset7, exponentsQset8, exponentsQset9, exponentsQset10,
      exponentsQset11, exponentsQset12, exponentsQset13, exponentsQset14, exponentsQset15, exponentsQset16, exponentsQset17, exponentsQset18, exponentsQset19, exponentsQset20,
      exponentsQset21, exponentsQset22, exponentsQset23))
    val ExponentsMastery = new Quiz("Exponents Mastery", List(ExponentSection1, ExponentSection2))
    pm.makePersistentAll(List(ExponentsMastery))
    val solvequations1a = new Question(M("10-(3/4)n=14:n="), List("-16/3", "-5+1/3"))
    val solvequations1b = new Question(M("8-(4/5)n=12:n="), List("-5"))
    val solvequations1c = new Question(M("2-(4/5)n=12:n="), List("-25/2", "-12+1/2"))
    val solvequations1d = new Question(M("8-(3/4)n=14:n="), List("-8"))
    val solvequations1e = new Question(M("8-(2/3)n=14:n="), List("-9"))
    val solvequations2a = new Question(M("0=5(8n+4):n="), List("-1/2"))
    val solvequations2b = new Question(M("0=2(5n-9):n="), List("9/5", "1+4/5"))
    val solvequations2c = new Question(M("3(2n-5)+7=6n-8+n:n="), List("0"))
    val solvequations2d = new Question(M("0=3(4n-1):n="), List("1/4"))
    val solvequations2e = new Question(M("0=5(9n-1):n="), List("1/9"))
    val solvequations2f = new Question(M("0=5(9n-4):n="), List("4/9"))
    val solvequations3a = new Question(M("n^2-12=4n:n="), List("6, -2", "-2, 6"))
    val solvequations3b = new Question(M("n^2+10n=24:n="), List("-12,2", "2,-12"))
    val solvequations3c = new Question(M("n^2+20=9n:n="), List("4,5", "5,4"))
    val solvequations3d = new Question(M("n^2=24-5n:n="), List("5/2,-4", "-4,5/2"))
    val solvequations3e = new Question(M("n^2-12=n:n="), List("4,-3", "-3,4"))
    val solvequations3f = new Question(M("n^2-18=3n:n="), List("9,-6", "-6,9"))
    val solvequations4a = new Question(M("5-(3n+6)+n=3-2n:n="), List("ns", "NS", "Ns", "no solution"))
    val solvequations4b = new Question(M("3(2n-1)+7=5n+4+n:n="), List("all real numbers", "ARN", "arn"))
    val solvequations4c = new Question(M("0=2(4n-9):n="), List("9/4", "2+1/4"))
    val solvequations4d = new Question(M("(2n-5)(n+4)=0:n="), List("5/2,-4", "-4,5/2"))
    val solvequations4e = new Question(M("3(2n-5)+10=6n-5+n:n="), List("0"))
    val solvequations4f = new Question(M("9-(3n+6)+n=3-2n:n="), List("all real numbers", "ARN", "arn"))
    val solvequations5a = new Question(M("(2/5)n+2=(2/3)n-3:n="), List("75/4", "18+3/4"))
    val solvequations5b = new Question(M("(1/5)n+2=(3/4)n-3:n="), List("100/11", "9+1/11"))
    val solvequations5c = new Question(M("(2/5)n+4=(3/4)n-4:n="), List("20"))
    val solvequations5d = new Question(M("(4/5)n+4=(3/4)n-3:n="), List("-140"))
    val solvequations5e = new Question(M("(3/5)n+4=(2/3)n-3:n="), List("105"))
    val solvequations5f = new Question(M("(2/5)n+4=(2/3)n-3:n="), List("105/4", "26+1/4"))
    val solvequations6a = new Question(M("(3n+5)(2n-6)=0:n="), List("-5/3,3", "3,-5/3"))
    val solvequations6b = new Question(M("(2n-5)(n+4)=0:n="), List("5/2,-4", "-4,5/2"))
    val solvequations6c = new Question(M("(2n-7_(3n+1)=0:n="), List("7/2,-1/3", "-1/3,7/2"))
    val solvequations6d = new Question(M("1-3n=5n+1-8n:n="), List("all real numbers", "ARN", "arn"))
    val solvequations6e = new Question(M("(3n+4)(2n-9)=0:n="), List("-4/3,9/2", "9/2,-4/3"))
    val solvequations6f = new Question(M("(3n+2)(2n-9)=0:n="), List("-2/3,9/2", "9/2,-2/3"))
    val solvequations7a = new Question(M("3(2n-2)+10=6n+4+n:n="), List("0"))
    val solvequations7b = new Question(M("9-(3n+10)=7+n:n="), List("-2"))
    val solvequations7c = new Question(M("9-(3n+6)=7+5n:n="), List("-1/2"))
    val solvequations7d = new Question(M("9-(3n+6)+n=7-2n:n="), List("no solution", "NS", "Ns", "ns"))
    val solvequations7e = new Question(M("3(2n-5)+10=6n-5+n:n="), List("no solution", "NS", "Ns", "ns"))
    val solvequations8a = new Question(M("4(n+2)-10=5n-(6-2n)-2:n="), List("2"))
    val solvequations8b = new Question(M("10-3(n+3)=1:n="), List("0"))
    val solvequations8c = new Question(M("4(n+2)-10=3n-(3-n)+1:n="), List("all real numbers", "ARN", "arn"))
    val solvequations8d = new Question(M("4(n+2)-10=3n-(3+n)+1:n="), List("0"))
    val solvequations8e = new Question(M("4(n+2)-10=5n-(6-2n)+1:n="), List("1"))
    val solvequations8f = new Question(M("4(n+2)-10=5n-(6-2n)-2:n="), List("2"))
    val solvequations9a = new Question(M("b=(2/3)a+14"), List("3/2b-21", "3b/2-21"))
    val solvequations9b = new Question(M("b=(2/3)a+4"), List("3/2b-6", "3b/2-6"))
    val solvequations9c = new Question(M("b=(4/3)a+4"), List("3/4b-3", "3b/4-3"))
    val solvequations9d = new Question(M("b=(5/8)a+10"), List("8/5b-16", "8b/5-16"))
    val solvequations9e = new Question(M("b=(4/3)a+10"), List("3/4b-15/2", "3b/4-15/2", "3/4b-15/2", "3b/4-15/2"))
    val solvequations9f = new Question(M("b=(2/3)a+10"), List("3/2b-15", "2b/2-15"))
    val solvequations10a = new Question(M("(4(3a+2b))/5c=60"), List("25c-2/3b", "25c-2b/3"))
    val solvequations10b = new Question(M("(4(2a+6b))/3c=40"), List("15c-3b", "-3b+15c"))
    val solvequations10c = new Question(M("(4(5a+6b))/3c=40"), List("6c-6/5b", "6c-6b/5", "-6b/5+6c", "-6/5b+6c"))
    val solvequations10d = new Question(M("(2(5a-b))/5c=40"), List("b/5+20c", "1/5b+20c", "20c+1/5b", "20c+b/5"))
    val solvequations10e = new Question(M("(2(5a+b))/5c=40"), List("20c-b/5", "-b/5+20c", "-1/5b+20c", "20c-1/5b"))
    val solvequations10f = new Question(M("(4(5a+b))/5c=40"), List("10c-b/5", "-b/5+10c", "10c-1/5b", "-1/5b+10c"))
    pm.makePersistentAll(List(solvequations1a, solvequations1b, solvequations1c, solvequations1d, solvequations1e, solvequations2a, solvequations2b, solvequations2c, solvequations2d,
      solvequations2e, solvequations2f, solvequations3a, solvequations3b, solvequations3c, solvequations3d, solvequations3d, solvequations3f, solvequations4a, solvequations4b,
      solvequations4c, solvequations4d, solvequations4e, solvequations4f, solvequations5a, solvequations5b, solvequations5c, solvequations5d, solvequations5e, solvequations5f,
      solvequations6a, solvequations6b, solvequations6c, solvequations6d, solvequations6e, solvequations6f, solvequations7a, solvequations7b, solvequations7c, solvequations7d,
      solvequations7e, solvequations8a, solvequations8b, solvequations8c, solvequations8d, solvequations8e, solvequations8f, solvequations9a, solvequations9b, solvequations9c,
      solvequations9d, solvequations9e, solvequations9f, solvequations10a, solvequations10b, solvequations10c, solvequations10d, solvequations10e, solvequations10f))
    val solvequationsQSet1 = new QuestionSet(List(solvequations1a, solvequations1b, solvequations1c, solvequations1d, solvequations1e))
    val solvequationsQSet2 = new QuestionSet(List(solvequations2a, solvequations2b, solvequations2c, solvequations2d, solvequations2e, solvequations2f))
    val solvequationsQSet3 = new QuestionSet(List(solvequations3a, solvequations3b, solvequations3c, solvequations3d, solvequations3e, solvequations3f))
    val solvequationsQSet4 = new QuestionSet(List(solvequations4a, solvequations4b, solvequations4c, solvequations4d, solvequations4e, solvequations4f))
    val solvequationsQSet5 = new QuestionSet(List(solvequations5a, solvequations5b, solvequations5c, solvequations5d, solvequations5e, solvequations5f))
    val solvequationsQSet6 = new QuestionSet(List(solvequations6a, solvequations6b, solvequations6c, solvequations6d, solvequations6e, solvequations6f))
    val solvequationsQSet7 = new QuestionSet(List(solvequations7a, solvequations7b, solvequations7c, solvequations7d, solvequations7e))
    val solvequationsQSet8 = new QuestionSet(List(solvequations8a, solvequations8b, solvequations8c, solvequations8d, solvequations8e, solvequations8f))
    val solvequationsQSet9 = new QuestionSet(List(solvequations9a, solvequations9b, solvequations9c, solvequations9d, solvequations9e, solvequations9f))
    val solvequationsQSet10 = new QuestionSet(List(solvequations10a, solvequations10b, solvequations10c, solvequations10d, solvequations10e, solvequations10f))
    pm.makePersistentAll(List(solvequationsQSet1, solvequationsQSet2, solvequationsQSet3, solvequationsQSet4, solvequationsQSet5, solvequationsQSet6, solvequationsQSet7,
      solvequationsQSet8, solvequationsQSet9, solvequationsQSet10))
    val solvequationsSection1 = new QuizSection("Solve for n", "Solve for n:", List(solvequationsQSet1, solvequationsQSet2, solvequationsQSet3, solvequationsQSet4, solvequationsQSet5,
      solvequationsQSet6, solvequationsQSet7, solvequationsQSet8))
    val solvequationsSection2 = new QuizSection("Solve for b", "Solve for b. Do not use parenthesis or a large division bar:", List(solvequationsQSet9, solvequationsQSet10))
    pm.makePersistentAll(List(solvequationsSection1, solvequationsSection2))
    val SolvequationsMastery = new Quiz("Solving Equations Mastery", List(solvequationsSection1, solvequationsSection2))
    pm.makePersistentAll(List(SolvequationsMastery))
    if (debug) println("Loading Complete!")
  }

}