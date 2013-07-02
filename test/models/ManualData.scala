package models

import scala.collection.JavaConversions._
import scala.collection.mutable
import xml.{ Node, NodeSeq, Elem, XML }
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.apache.poi.ss.usermodel.{ Sheet, Row, WorkbookFactory }
import models.users._
import models.courses._
import models.lockers._
import util.Helpers
import java.io.File
import models.assignments.AssignmentData
import org.tukaani.xz.XZInputStream
import java.text.SimpleDateFormat
import models.books.Title
import java.text.DateFormat
import java.text.ParseException
import models.books.PurchaseGroup
import models.books.Copy
import models.books.Checkout
import models.blogs.Blog
import scalajdo.DataStore
import models.users.Gender
import models.users.User

object ManualData {
  val netIdMap: Map[String, String] = buildNetIdMap()

  def load(debug: Boolean = false) {
    val dbFile = new File("data.h2.db")
    dbFile.delete()
    loadManualData(debug)
  }

  def loadL(debug: Boolean = false) {
    val dbFile = new File("data.h2.db")
    dbFile.delete()
    loadLockers(debug)
  }

  def loadManualData(debug: Boolean = false) {
    if (!debug) println("Creating Year and Term Data...")
    createYearsAndTerms(debug)
    if (!debug) println("Creating Student Data...")
    loadStudents(debug)
    if (!debug) println("Creating Teacher Data...")
    loadTeachers(debug)
    if (!debug) println("Creating Course Data...")
    loadCourses(debug)
    if (!debug) println("Creating Section Data...")
    loadSections(debug)
    if (!debug) println("Creating Enrollment Data...")
    loadEnrollments(debug)
    if (!debug) println("Creating Book Data...")
    loadBookData(debug)
    if (!debug) println("Creating Locker Data...")
    loadLockers(debug)
  }

  def createYearsAndTerms(debug: Boolean) {
    DataStore.withTransaction { implicit pm =>
      val acadYear = new AcademicYear("2011-12")
      pm.makePersistent(acadYear)
      val fall2011 = new Term("Fall 2011", acadYear, "f11", new LocalDate(2011, 8, 17), new LocalDate(2012, 12, 16))
      pm.makePersistent(fall2011)
      val spring2012 = new Term("Spring 2012", acadYear, "s12", new LocalDate(2012, 1, 3), new LocalDate(2012, 5, 25))
      pm.makePersistent(spring2012)
      val periods: List[Period] = List(
        new Period("Red 1", 1), new Period("Red 2", 2), new Period("Red 3", 3), new Period("Red 4", 4),
        new Period("Red Activity", 5), new Period("Red Advisory", 6),
        new Period("White 1", 7), new Period("White 2", 8), new Period("White 3", 9), new Period("White 4", 10),
        new Period("White Activity", 11), new Period("White Advisory", 12))
      pm.makePersistentAll(periods)
      if (debug) println("Created AcademicYear, Terms, and Periods")
    }
  }

  def loadStudents(debug: Boolean) {
    DataStore.withTransaction { implicit pm =>
      val doc = XML.load(getClass.getResourceAsStream("/manual-data/Students.xml"))
      val students = doc \\ "student"
      students foreach ((student: Node) => {
        // grab data
        val studentNumber = asIdNumber((student \ "@student.studentNumber").text)
        val stateId = asIdNumber((student \ "@student.stateID").text)
        val first = (student \ "@student.firstName").text
        val middle = (student \ "@student.middleName").text
        val last = (student \ "@student.lastName").text
        val teamName = (student \ "@student.teamName").text
        val grade = (student \ "@student.grade").text.toInt
        val gender = if ((student \ "@student.gender").text == "F") Gender.Female else Gender.Male
        val username = netIdMap.getOrElse(studentNumber, studentNumber)
        if (debug) {
          println()
          println("%s, %s %s".format(last, first, middle))
          println("#: %s, id: %s, grade: %d".format(studentNumber, stateId, grade))
          println("name: %s, magnet: %s, gender: %s".format(username, teamName, gender))
        }
        // create User
        val user = new User(username, first, Some(middle), last, None, gender, null, "temp123")
        pm.makePersistent(user)
        if (debug) println("user saved")
        // create Student
        val dbStudent = new Student(user, stateId, studentNumber, grade, teamName)
        pm.makePersistent(dbStudent)
        if (debug) println("student saved")
        // create Blog
        val blog = new Blog(username + "'s Blog", dbStudent)
        pm.makePersistent(blog)
        if (debug) println("blog saved")
      })
    }
  }

  def loadTeachers(debug: Boolean) {
    DataStore.withTransaction { implicit pm =>
      val doc = XML.load(getClass.getResourceAsStream("/manual-data/Teachers.xml"))
      val teachers = doc \\ "person"
      teachers foreach ((teacher: Node) => {
        val username = asIdNumber((teacher \ "@individual.personID").text) // TODO: get real login name
        val first = (teacher \ "@individual.firstName").text
        val middle = (teacher \ "@individual.middleName").text
        val last = (teacher \ "@individual.lastName").text
        val gender = if ((teacher \ "@individual.gender").text == "F") Gender.Female else Gender.Male
        val personId = asIdNumber((teacher \ "@individual.personID").text)
        val stateId = asIdNumber((teacher \ "@individual.stateID").text)
        if (debug) {
          println()
          println("%s, %s %s".format(last, first, middle))
          println("#: %s, id: %s".format(personId, stateId))
          println("name: %s, gender: %s".format(username, gender))
        }
        val user = new User(username, first, Some(middle), last, None, gender, null, "temp123")
        pm.makePersistent(user)
        if (debug) println("user saved")
        val dbTeacher = new Teacher(user, personId, stateId)
        pm.makePersistent(dbTeacher)
        if (debug) println("teacher saved")
      })
    }
  }

  def loadCourses(debug: Boolean) {
    DataStore.withTransaction { implicit pm =>
      val doc = XML.load(getClass.getResourceAsStream("/manual-data/Courses.xml"))
      val courses = doc \\ "curriculum"
      courses foreach ((course: Node) => {
        val name = (course \ "@courseInfo.courseName").text
        val masterNumber = asIdNumber((course \ "@courseInfo.courseMasterNumber").text)
        val dept = Department.getOrCreate((course \ "@courseInfo.departmentName").text)
        if (debug) println("%s, %s (%s)".format(name, masterNumber, dept))
        val dbCourse = new Course(name, masterNumber, dept)
        pm.makePersistent(dbCourse)
      })
    }
  }

  def loadSections(debug: Boolean) {
    DataStore.withTransaction { implicit pm =>
      val doc = XML.load(getClass.getResourceAsStream("/manual-data/Sections.xml"))
      val sections = doc \\ "curriculum"
      val fall11 = Term.getBySlug("f11").get
      val spring12 = Term.getBySlug("s12").get
      sections foreach ((section: Node) => {
        val sectionId = (section \ "@sectionInfo.sectionID").text
        if (debug) println("Working on section: %s".format(sectionId))
        val courseMasterNumber = asIdNumber((section \ "@courseInfo.courseMasterNumber").text)
        val course = pm.query[Course].filter(QCourse.candidate.masterNumber.eq(courseMasterNumber)).executeOption().get
        val roomNum = (section \ "@sectionInfo.roomName").text
        val room = Room.getOrCreate(roomNum)
        val termStart = (section \ "@sectionSchedule.termStart").text
        val termEnd = (section \ "@sectionSchedule.termEnd").text
        val terms: List[Term] = (termStart, termEnd) match {
          case ("1", "3") => List(fall11)
          case ("4", "6") => List(spring12)
          case ("1", "6") => List(fall11, spring12)
        }
        val periodStart = (section \ "@sectionSchedule.periodStart").text
        val periodEnd = (section \ "@sectionSchedule.periodEnd").text
        val dayStart = (section \ "@sectionSchedule.scheduleStart").text
        val dayEnd = (section \ "@sectionSchedule.scheduleEnd").text
        val periods = periodNames(dayStart, dayEnd, periodStart, periodEnd) map ((p: String) => {
          pm.query[Period].filter(QPeriod.candidate.name.eq(p)).executeOption().get
        })
        val teacherPersonId = (section \ "@sectionInfo.teacherPersonID").text
        val teacher = pm.query[Teacher].filter(QTeacher.candidate.personId.eq(teacherPersonId)).executeOption().get
        val dbSection = new Section(course, sectionId, terms.toSet[Term], periods.toSet[Period], room)
        pm.makePersistent(dbSection)
        terms foreach ((term: Term) => {
          val teacherAssignment = new TeacherAssignment(teacher, dbSection, null, null)
          pm.makePersistent(teacherAssignment)
        })
      })
    }
  }

  def loadEnrollments(debug: Boolean) {
    DataStore.withTransaction { implicit pm =>
      var wellThen = false
      import scala.collection.JavaConversions.asScalaSet
      val doc = XML.load(getClass.getResourceAsStream("/manual-data/Schedule.xml"))
      val enrollments = doc \\ "student"
      enrollments foreach ((enrollment: Node) => {
        var isOk = true
        val sectionId = asIdNumber((enrollment \ "@courseSection.sectionID").text)
        val maybeSection: Option[Section] = pm.query[Section].filter(QSection.candidate.sectionId.eq(sectionId)).executeOption()
        val studentNumber = asIdNumber((enrollment \ "@student.studentNumber").text)
        val student = pm.query[Student].filter(QStudent.candidate.studentNumber.eq(studentNumber)).executeOption().get
        maybeSection match {
          case Some(section) => {
            if (debug) println("Adding student #%s to section #%s".format(studentNumber, sectionId))
            val startDate = asLocalDate((enrollment \ "@roster.startDate").text)
            val endDate = asLocalDate((enrollment \ "@roster.endDate").text)
            asScalaSet[Term](section.terms) foreach ((term: Term) => {
              val dbEnrollment = new StudentEnrollment(student, section, startDate, endDate)
              pm.makePersistent(dbEnrollment)
            })
          }
          case None => {
            if (debug) println("Student %s (id #%s) is in section #%s, which doesn't exist.".format(student.user.formalName, studentNumber, sectionId))
            if (!debug) wellThen = true
          }
        }
      })
      if (wellThen) println("Some sections were not found D:")
    }
  }

  def loadLockers(debug: Boolean) {
    DataStore.withTransaction { implicit pm =>
      val doc = XML.load(getClass.getResourceAsStream("/manual-data/Lockers.xml"))
      val lockers = doc \\ "student"
      lockers foreach ((locker: Node) => {
        val number = util.Helpers.toInt((locker \ "@lockerDetail.lockerNumber").text)
        val location = LockerData.locationCreator((locker \ "@lockerDetail.location").text)
        val combination = LockerData.randomCombination
        if (debug) println("Adding Locker: #%d %s %s".format(number, combination, location))
        val dbLocker = new Locker(number, combination, location, None, false)
        pm.makePersistent(dbLocker)

      })
    }
  }

  def asLocalDate(date: String): LocalDate = {
    val format = DateTimeFormat.forPattern("MM/dd/yyyy")
    date match {
      case "" => null
      case _ => format.parseDateTime(date).toLocalDate
    }
  }

  def periodNames(dayStart: String, dayEnd: String,
    periodStart: String, periodEnd: String): List[String] = {
    val days = List(dayStart, dayEnd).distinct map ((d: String) => {
      d match {
        case "RED" => "Red"
        case "WHT" => "White"
      }
    })
    val periods: List[String] = (periodStart, periodEnd) match {
      case ("ACT", "ACT") => List("Activity")
      case ("ADV", "ADV") => List("Advisory")
      case (_, _) => (periodStart.toInt to periodEnd.toInt).toList.map(_.toString)
    }
    days flatMap ((d: String) => {
      periods map ((p: String) => {
        "%s %s".format(d, p)
      })
    })
  }

  def asIdNumber(s: String): String = {
    val num = s.replaceAll("^0+", "")
    if (num.equals("")) null else num
  }

  def buildNetIdMap(): Map[String, String] = {
    val wb = WorkbookFactory.create(getClass.getResourceAsStream("/manual-data/StudentNetworkSecurityInfo.xls"))
    val sheet: Sheet = wb.getSheetAt(0)
    sheet.removeRow(sheet.getRow(0))
    val pairs = (sheet map ((row: Row) => {
      row.getCell(0).getNumericCellValue.toInt.toString -> row.getCell(6).getStringCellValue
    })).toList
    Map(pairs: _*)
  }

  def loadBookData(debug: Boolean = false) {
    if (debug) println("Loading book data...")
    val data = XML.load(new XZInputStream(getClass.getResourceAsStream("/manual-data/bookData.xml.xz")))
    val titleIdMap = loadTitles((data \ "titles"), debug)
    val pgIdMap = loadPurchaseGroups((data \ "purchaseGroups"), titleIdMap, debug)
    val copyIdMap = loadCopies((data \ "copies"), pgIdMap, debug)
    loadCheckouts((data \ "checkouts"), copyIdMap, debug)
  }

  def asInt(s: String): Int = {
    try {
      s.toInt
    } catch {
      case e: NumberFormatException => null.asInstanceOf[Int]
    }
  }

  def asDouble(s: String): Double = {
    try {
      s.toDouble
    } catch {
      case e: NumberFormatException => null.asInstanceOf[Double]
    }
  }

  def asDate(s: String, df: DateFormat): java.sql.Date = {
    try {
      new java.sql.Date(df.parse(s).getTime)
    } catch {
      case e: ParseException => null.asInstanceOf[java.sql.Date]
    }
  }

  def asOptionString(s: String): Option[String] = {
    if (s.trim() == "") None else Some(s.trim())
  }

  def loadTitles(titles: NodeSeq, debug: Boolean = false): mutable.Map[Long, Long] = {
    val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val titleIdMap = mutable.Map[Long, Long]()
    DataStore.withTransaction { implicit pm =>
      for (t <- (titles \ "title")) {
        val djId = (t \ "id").text.toLong
        val title = new Title((t \ "name").text, asOptionString((t \ "author").text), asOptionString((t \ "publisher").text), (t \ "isbn").text,
          Option(asInt((t \ "numPages").text)), asOptionString((t \ "dimensions").text), Option(asDouble((t \ "weight").text)),
          (t \ "verified").text.toBoolean, asDate((t \ "lastModified").text, df), asOptionString((t \ "image").text))
        if (debug) println("Adding title: %s...".format(title.name))
        pm.makePersistent(title)
        titleIdMap += (djId -> title.id)
      }
    }
    titleIdMap
  }

  def loadPurchaseGroups(pgs: NodeSeq, titleIdMap: mutable.Map[Long, Long], debug: Boolean = false): mutable.Map[Long, Long] = {
    val df = new SimpleDateFormat("yyyy-MM-dd")
    val pgIdMap = mutable.Map[Long, Long]()
    DataStore.withTransaction { implicit pm =>
      for (pg <- (pgs \ "purchaseGroup")) {
        val djId = (pg \ "id").text.toLong
        val title = Title.getById(titleIdMap((pg \ "titleId").text.toLong)).get
        val purchaseGroup = new PurchaseGroup(title, asDate((pg \ "purchaseDate").text, df), asDouble((pg \ "price").text))
        if (debug) println("Adding purchase group: %s...".format(purchaseGroup))
        pm.makePersistent(purchaseGroup)
        pgIdMap += (djId -> purchaseGroup.id)
      }
    }
    pgIdMap
  }

  def loadCopies(copies: NodeSeq, pgIdMap: mutable.Map[Long, Long], debug: Boolean = false): mutable.Map[Long, Long] = {
    val copyIdMap = mutable.Map[Long, Long]()
    DataStore.withTransaction { implicit pm =>
      for (c <- (copies \ "copy")) {
        val djId = (c \ "id").text.toLong
        val pg = PurchaseGroup.getById(pgIdMap((c \ "purchaseGroupId").text.toLong)).get
        val copy = new Copy(pg, (c \ "number").text.toInt, (c \ "isLost").text.toBoolean)
        if (debug) println("Adding copy: %s...".format(copy))
        pm.makePersistent(copy)
        copyIdMap += (djId -> copy.id)
      }
    }
    copyIdMap
  }

  def loadCheckouts(checkouts: NodeSeq, copyIdMap: mutable.Map[Long, Long], debug: Boolean = false) {
    val df = new SimpleDateFormat("yyyy-MM-dd")
    // only loads items checked out to students in the db; older students aren't included
    DataStore.withTransaction { implicit pm =>
      for (co <- (checkouts \ "checkout")) {
        Student.getByStudentNumber((co \ "studentNumber").text) match {
          case None => // do nothing
          case Some(student) => {
            val copy = Copy.getById(copyIdMap((co \ "copyId").text.toLong)).get
            val startDate = (co \ "startDate").text match {
              case "None" => null
              case s: String => asDate(s, df)
            }
            val endDate = (co \ "endDate").text match {
              case "None" => null
              case s: String => asDate(s, df)
            }
            val checkout = new Checkout(student, copy, startDate, endDate)
            if (debug) println("Adding checkout: %s".format(checkout))
            pm.makePersistent(checkout)
          }
        }
      }
    }
  }
}
