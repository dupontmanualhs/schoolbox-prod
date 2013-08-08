package models

import play.api.Logger

import scala.collection.JavaConverters._
import scala.collection.mutable
import xml.{ Node, NodeSeq, Elem, XML }
import org.joda.time.{ LocalDate, LocalDateTime }
import org.joda.time.format.DateTimeFormat
import org.apache.poi.ss.usermodel.{ Sheet, Row, WorkbookFactory }
import models.users._
import models.courses._
import models.lockers._
import util.Helpers
import java.io.File
import java.util.Properties
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
import models.users.Gender
import models.users.User
import org.joda.time.format.DateTimeFormatter
import config.users.UsesDataStore

object ManualData extends UsesDataStore {
  
  val netIdMap: Map[String, String] = buildNetIdMap()

  def load() {
    // de-activate everyone and re-activate only the users in the data dump
    markAllUsersInactive()
    loadStudents()
    loadTeachers()
    loadCourses()
    loadSections()
    loadEnrollments()
  def markAllUsersInactive() {
    dataStore.withTransaction { pm =>
      val users = pm.query[User].executeList()
      users foreach { user => 
        user.isActive = false
        pm.makePersistent(user)
    //loadParents(debug)
    if (!debug) println("Importing teacher data...")
  }

  def loadStudents() {
    dataStore.withTransaction { implicit pm =>
      Logger.info("Importing students...")
      val doc = XML.load(new XZInputStream(getClass.getResourceAsStream("/manual-data/Students.xml.xz")))
      val students = doc \\ "student"
      students foreach ((student: Node) => {
        // grab data
        val studentNumber = asIdNumber((student \ "@student.studentNumber").text)
        val stateId = asIdNumber((student \ "@student.stateID").text)
        val first = (student \ "@student.firstName").text
        val middle = asOptionString((student \ "@student.middleName").text)
        val last = (student \ "@student.lastName").text
        val teamName = (student \ "@student.teamName").text
        val grade = asInt((student \ "@student.grade").text)
        val gender = asGender((student \ "@student.gender").text)
        val username = netIdMap.getOrElse(studentNumber, studentNumber)
        val info = s"($last, $first: stateId=$stateId; studentNumber=$studentNumber)"
        val maybeStudent: Option[Student] = 
            List(Student.getByStateId(stateId), Student.getByStudentNumber(studentNumber)).find(_.isDefined).getOrElse(None)
        maybeStudent match {
          case None => {
            Logger.debug(s"Creating new student $info")
            val user = new User(username, first, middle, last, None, gender, null, null)
            pm.makePersistent(user)
            Logger.trace("datastore user saved")
            val dbStudent = new Student(user, stateId, studentNumber, grade, teamName)
            pm.makePersistent(dbStudent)
            Logger.trace("datastore student saved")
          }
          case Some(oldStudent) => {
            Logger.debug(s"Student $info already exists; setting to new values, in case they've changed")
            // TODO log what happens and only change what needs changing
            oldStudent.user.username = username
            oldStudent.user.first = first
            oldStudent.user.middle = middle
            oldStudent.user.last = last
            oldStudent.user.gender = gender
            oldStudent.user.isActive = true
            pm.makePersistent(oldStudent.user)
            oldStudent.grade = grade
            oldStudent.teamName = teamName
            oldStudent.stateId = stateId
            oldStudent.studentNumber = studentNumber
            pm.makePersistent(oldStudent)
          }
        }
      })
    }
  }
  
  def loadTeachers() {
    dataStore.withTransaction { implicit pm =>
      val doc = XML.load(new XZInputStream(getClass.getResourceAsStream("/manual-data/Teachers.xml.xz")))
      val teachers = doc \\ "person"
      teachers foreach ((teacher: Node) => {
        val first = (teacher \ "@individual.firstName").text
        val middle = (teacher \ "@individual.middleName").text
        val last = (teacher \ "@individual.lastName").text
        val gender = if ((teacher \ "@individual.gender").text == "F") Gender.Female else Gender.Male
        val personId = asIdNumber((teacher \ "@individual.personID").text)
        val stateId = asIdNumber((teacher \ "@individual.stateID").text)
        val info = s"($last, $first: stateId=$stateId; personId=$personId)"
        val maybeTeacher = 
            List(Teacher.getByStateId(stateId), Teacher.getByPersonId(personId)).find(_.isDefined).getOrElse(None)
        maybeTeacher match {
          case None => {
            Logger.debug(s"Teacher $info not in datastore; adding")
            val user = new User(personId, first, Some(middle), last, None, gender, null, null)
            pm.makePersistent(user)
            Logger.trace("user saved")
            val dbTeacher = new Teacher(user, personId, stateId)
            pm.makePersistent(dbTeacher)
            Logger.trace("teacher saved")
          }
          case Some(oldTeacher) => {
            Logger.debug(s"Teacher $info already in database; updating to newest values")
            oldTeacher.user.first = first
            oldTeacher.user.middle = middle
            oldTeacher.user.last = last
            oldTeacher.user.gender = gender
            oldTeacher.user.isActive = true
            pm.makePersistent(oldTeacher.user)
            oldTeacher.stateId = stateId
            oldTeacher.personId = personId
            pm.makePersistent(oldTeacher)   
          }
        }
      })
    }
  }

  def loadCourses(debug: Boolean) {
    val doc = XML.load(new XZInputStream(getClass.getResourceAsStream("/manual-data/Courses.xml.xz")))
    val courses = doc \\ "curriculum"
    dataStore.withTransaction { implicit pm =>
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
    dataStore.withTransaction { implicit pm =>
      val doc = XML.load(new XZInputStream(getClass.getResourceAsStream("/manual-data/Sections.xml.xz")))
      val sections = doc \\ "curriculum"
      val fall13 = Term.getBySlug("f13").get
      val spring14 = Term.getBySlug("s14").get
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
          case ("1", "3") => List(fall13)
          case ("4", "6") => List(spring14)
          case ("1", "6") => List(fall13, spring14)
          case _ => Nil
        }
        val periodStart = (section \ "@sectionSchedule.periodStart").text
        val periodEnd = (section \ "@sectionSchedule.periodEnd").text
        val dayStart = (section \ "@sectionSchedule.scheduleStart").text
        val dayEnd = (section \ "@sectionSchedule.scheduleEnd").text
        val periods = periodNames(dayStart, dayEnd, periodStart, periodEnd) map ((p: String) => {
          pm.query[Period].filter(QPeriod.candidate.name.eq(p)).executeOption().get
        })
        val teacherPersonId = (section \ "@sectionInfo.teacherPersonID").text
        val teacher = pm.query[Teacher].filter(QTeacher.candidate.personId.eq(teacherPersonId)).executeOption()
        val dbSection = new Section(course, sectionId, terms.toSet[Term], periods.toSet[Period], room)
        pm.makePersistent(dbSection)
        if (teacher.isDefined) {
          //terms foreach ((term: Term) => { // this isn't necessary, is it?
            val teacherAssignment = new TeacherAssignment(teacher.get, dbSection, None, None)
            pm.makePersistent(teacherAssignment)
          //})
        }
      })
    }
  }

  def loadEnrollments(debug: Boolean) {
    dataStore.withTransaction { implicit pm =>
      var wellThen = false
      import scala.collection.JavaConversions.asScalaSet
      val doc = XML.load(new XZInputStream(getClass.getResourceAsStream("/manual-data/Schedule.xml.xz")))
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
            section.terms.foreach((term: Term) => {
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

  /*def loadLockers(debug: Boolean) {
    dataStore.withTransaction { implicit pm =>
      val doc = XML.load(new XZInputStream(getClass.getResourceAsStream("/manual-data/Lockers.xml.xz")))
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
  }*/

  def asLocalDate(date: String): Option[LocalDate] = {
    val format = DateTimeFormat.forPattern("MM/dd/yyyy")
    date match {
      case "" => None
      case _ => Some(format.parseDateTime(date).toLocalDate)
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
    val wb = WorkbookFactory.create(new XZInputStream(getClass.getResourceAsStream("/manual-data/StudentNetworkSecurityInfo.xls.xz")))
    val sheet: Sheet = wb.getSheetAt(0)
    sheet.removeRow(sheet.getRow(0))
    val pairs = (sheet.asScala.map { (row: Row) =>
      row.getCell(0).getNumericCellValue.toInt.toString -> row.getCell(6).getStringCellValue
    }).toList
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

  def asOptionString(s: String): Option[String] = {
    if (s.trim() == "") None else Some(s.trim())
  }
  
  def asGender(s: String): Gender.Gender = s match {
    case "M" => Gender.Male
    case "F" => Gender.Female
    case _ => Gender.NotListed
  }
  
  def asOptionDateTime[T](parseFunction: (String => T), str: String): Option[T] = {
    try {
      Some(parseFunction(str))
    } catch {
      case e: IllegalArgumentException => None
    }
  }

  def loadTitles(titles: NodeSeq, debug: Boolean = false): mutable.Map[Long, Long] = {
    val df = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS")
    val titleIdMap = mutable.Map[Long, Long]()
    dataStore.withTransaction { implicit pm =>
      for (t <- (titles \ "title")) {
        val djId = (t \ "id").text.toLong
        val title = new Title((t \ "name").text, asOptionString((t \ "author").text), asOptionString((t \ "publisher").text), (t \ "isbn").text,
          Option(asInt((t \ "numPages").text)), asOptionString((t \ "dimensions").text), Option(asDouble((t \ "weight").text)),
          (t \ "verified").text.toBoolean, asOptionDateTime(df.parseLocalDateTime _, (t \ "lastModified").text), asOptionString((t \ "image").text))
        if (debug) println("Adding title: %s...".format(title.name))
        pm.makePersistent(title)
        titleIdMap += (djId -> title.id)
      }
    }
    titleIdMap
  }

  def loadPurchaseGroups(pgs: NodeSeq, titleIdMap: mutable.Map[Long, Long], debug: Boolean = false): mutable.Map[Long, Long] = {
    val df = DateTimeFormat.forPattern("yyyy-MM-dd")
    val pgIdMap = mutable.Map[Long, Long]()
    dataStore.withTransaction { implicit pm =>
      for (pg <- (pgs \ "purchaseGroup")) {
        val djId = (pg \ "id").text.toLong
        val title = Title.getById(titleIdMap((pg \ "titleId").text.toLong)).get
        val purchaseGroup = new PurchaseGroup(title, df.parseLocalDate((pg \ "purchaseDate").text), asDouble((pg \ "price").text))
        if (debug) println("Adding purchase group: %s...".format(purchaseGroup))
        pm.makePersistent(purchaseGroup)
        pgIdMap += (djId -> purchaseGroup.id)
      }
    }
    pgIdMap
  }

  def loadCopies(copies: NodeSeq, pgIdMap: mutable.Map[Long, Long], debug: Boolean = false): mutable.Map[Long, Long] = {
    val copyIdMap = mutable.Map[Long, Long]()
    dataStore.withTransaction { implicit pm =>
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
    val df = DateTimeFormat.forPattern("yyyy-MM-dd")
    // only loads items checked out to students in the db; older students aren't included
    dataStore.withTransaction { implicit pm =>
      for (co <- (checkouts \ "checkout")) {
        Student.getByStudentNumber((co \ "studentNumber").text) match {
          case None => // do nothing
          case Some(student) => {
            val copy = Copy.getById(copyIdMap((co \ "copyId").text.toLong)).get
            val startDate = (co \ "startDate").text match {
              case "None" => None
              case s: String => Some(df.parseLocalDate(s))
            }
            val endDate = (co \ "endDate").text match {
              case "None" => None
              case s: String => Some(df.parseLocalDate(s))
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
