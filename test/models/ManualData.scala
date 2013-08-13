package models

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
import scala.util.matching.Regex.Match
import com.typesafe.scalalogging.slf4j.Logging

object ManualData extends UsesDataStore with Logging {

  val folder = "/manual-data-2013-08-12"
  
  // Needs to be updated each year
  val currentYear = dataStore.pm.detachCopy(AcademicYear.getByName("2013-14").get)
  val fall13 = dataStore.pm.detachCopy(Term.getBySlug("f13").get)
  val spring14 = dataStore.pm.detachCopy(Term.getBySlug("s14").get)
  def icToTerms(start: String, end: String): List[Term] = (start, end) match {
    case ("1", "3") => List(fall13)
    case ("4", "6") => List(spring14)
    case ("1", "6") => List(fall13, spring14)
    case _ => Nil
  }

  val netIdMap: Map[String, String] = buildNetIdMap()

  def load() {
    val classes = dataStore.persistentClasses.asJava
    val props = new Properties()
    dataStore.storeManager.validateSchema(classes, props)
    // de-activate everyone and re-activate only the users in the data dump
    markAllUsersInactive()
    loadStudents()
    loadGuardians()
    loadTeachers()
    loadCourses()
    val unusedSections = loadSections()
    loadEnrollments()
    //graduateSeniors()
  }

  def markAllUsersInactive() {
    dataStore.withTransaction { pm =>
      val users = pm.query[User].executeList()
      users foreach { user =>
        user.isActive = false
        pm.makePersistent(user)
      }
    }
  }

  def loadStudents() {
    dataStore.withTransaction { implicit pm =>
      logger.info("Importing students...")
      val doc = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/Students.xml.xz")))
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
        val maybeStudent: Option[Student] = Student.getByStateId(stateId).orElse(Student.getByStudentNumber(studentNumber))
        maybeStudent match {
          case None => {
            logger.debug(s"Creating new student $info")
            val user = new User(username, first, middle, last, None, gender, None, None, true, false, false)
            pm.makePersistent(user)
            logger.trace("datastore user saved")
            val dbStudent = new Student(user, stateId, studentNumber, grade, teamName)
            pm.makePersistent(dbStudent)
            logger.trace("datastore student saved")
          }
          case Some(oldStudent) => {
            logger.debug(s"Student $info already exists; setting to new values ($last, $first), in case they've changed")
            // TODO log what happens and only change what needs changing
            val oldUser = oldStudent.user
            oldUser.username = username
            oldUser.first = first
            oldUser.middle = middle
            oldUser.last = last
            oldUser.gender = gender
            oldUser.isActive = true
            pm.makePersistent(oldUser)
            logger.trace("datastore user saved")
            oldStudent.grade = grade
            oldStudent.teamName = teamName
            oldStudent.stateId = stateId
            oldStudent.studentNumber = studentNumber
            pm.makePersistent(oldStudent)
            logger.trace("datastore student saved")
          }
        }
      })
    }
  }
  
  def altStudentNumber(studentNumber: String): Option[String] = {
    // if studentNumber begins with 0's, we should try stripping them
    val alt = """^0+(\d+)$""".r.replaceAllIn(studentNumber, m => m.group(1))
    if (alt != studentNumber) Some(alt) else None
  }

  def loadGuardians() {
    val doc = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/Parents.xml.xz")))
    val contacts = doc \\ "student"
    logger.info("Importing guardians...")
    val cand = QGuardian.candidate
    val userVar = QUser.variable("userVar")
    contacts foreach ((contact: Node) => {
      dataStore.withTransaction { implicit pm =>
        val studentNumber = (contact \ "@student.studentNumber").text
        val altNumber = altStudentNumber(studentNumber)
        val isGuardian = ((contact \ "@contacts.guardian").text == "1")
        logger.trace(s"read guardian for student number $studentNumber")
        if (isGuardian) {
          logger.trace("processing, because is guardian")
          Student.getByStudentNumber(studentNumber).orElse(altNumber.flatMap(Student.getByStudentNumber(_))) match {
            case None => logger.info(s"No student with studentNumber $studentNumber in database.")
            case Some(student) => {
              logger.trace(s"guardian belongs to Student: ${student.formalName}")
              val contactId = ((contact \ "@contacts.contactPersonID").text)
              val first = ((contact \ "@contacts.firstName").text)
              val last = ((contact \ "@contacts.lastName").text)
              val email = asOptionString((contact \ "@contacts.email").text)
              logger.trace(s"Trying to find Guardian (contactId: $contactId, last: $last, first $first")
              val guardianById = if (contactId != "") Guardian.getByContactId(contactId) else None
              val guardianByName = pm.query[Guardian].filter(cand.user.eq(userVar).and(cand.contactId.eq(null.asInstanceOf[String])).and(
                userVar.first.eq(first)).and(userVar.last.eq(last))).executeOption()
              val maybeGuardian: Option[Guardian] = guardianById.orElse(guardianByName)
              maybeGuardian match {
                case None => {
                  logger.debug(s"Creating new Guardian($first, $last) for Student($student, grade ${student.grade})")
                  val newUsername = if (email.isDefined && User.getByUsername(email.get).isEmpty) {
                    email.get
                  } else if (User.getByUsername(contactId).isEmpty) {
                    contactId
                  } else {
                    java.util.UUID.randomUUID().toString()
                  }
                  val newUser = new User(newUsername, first, None, last, None,
                    Gender.NotListed, email, None, true, false, false)
                  pm.makePersistent(newUser)
                  val newGuardian = new Guardian(newUser, Some(contactId), Set(student))
                }
                case Some(guardian) => {
                  // update with most recent info
                  // TODO: log what's actually updated
                  logger.debug("Guardian already exists, updating.")
                  guardian.user.first = first
                  guardian.user.last = last
                  guardian.user.isActive = true
                  guardian.user.email = email
                  guardian.contactId = Some(contactId)
                  if (!guardian.children.contains(student)) {
                    guardian.children = guardian.children + student
                  }
                }
              }
            }
          }
        } else {
          logger.trace("skipped because not guardian")
        }
      }
    })
  }

  def loadTeachers() {
    val doc = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/Teachers.xml.xz")))
    val teachers = doc \\ "person"
    logger.info("Importing teachers...")
    dataStore.withTransaction { implicit pm =>
      teachers foreach ((teacher: Node) => {
        val first = (teacher \ "@individual.firstName").text
        val middle = (teacher \ "@individual.middleName").text
        val last = (teacher \ "@individual.lastName").text
        val gender = asGender((teacher \ "@individual.gender").text)
        val personId = asIdNumber((teacher \ "@individual.personID").text)
        val stateId = asIdNumber((teacher \ "@individual.stateID").text)
        val info = s"($last, $first: stateId=$stateId; personId=$personId)"
        val maybeTeacher = Teacher.getByStateId(stateId).orElse(Teacher.getByPersonId(personId))
        maybeTeacher match {
          case None => {
            logger.debug(s"Teacher $info not in datastore; adding")
            val user = new User(personId, first, Some(middle), last, None, gender, None, None, true, false, false)
            pm.makePersistent(user)
            logger.trace("user saved")
            val dbTeacher = new Teacher(user, personId, stateId)
            pm.makePersistent(dbTeacher)
            logger.trace("teacher saved")
          }
          case Some(oldTeacher) => {
            logger.debug(s"Teacher $info already in database; updating to newest values")
            // TODO log what happens and only change what needs changing
            oldTeacher.user.first = first
            oldTeacher.user.middle = middle
            oldTeacher.user.last = last
            oldTeacher.user.gender = gender
            oldTeacher.user.isActive = true
            pm.makePersistent(oldTeacher.user)
            logger.trace("user updated")
            oldTeacher.stateId = stateId
            oldTeacher.personId = personId
            pm.makePersistent(oldTeacher)
            logger.trace("teacher updated")
          }
        }
      })
    }
  }
  
  def normalize(s: String): String = {
    // TODO: Tests for these
    def cap(m: Match): String = m.group(0).toUpperCase
    val acronyms = """(^|\s|\()(a[bp]?|bc|mst|ii|iii|iv|ups|us|ypas|ece)(?=\s|/|$|\))""".r
    val asl = """amsignlang""".r
    val sci1a = """(?<=science\s)1a""".r
    val pe = """(^|\s)pe(\s|$|\d|/)""".r
    val newWord = """(^|\s|&|-|/|\()[a-z]""".r
    val s2 = acronyms.replaceAllIn(s, cap _)
    val s3 = asl.replaceAllIn(s2, "AmSignLang")
    val s4 = sci1a.replaceAllIn(s3, cap _)
    val s5 = pe.replaceAllIn(s4, cap _)
    val s6 = newWord.replaceAllIn(s5, cap _)
    if (s6 == "") "None" else s6
  }

  def loadCourses() {
    logger.trace("Fetching courses already in database...")
    val coursesByMasterNumber: Map[String, Course] = dataStore.pm.query[Course].executeList().map(c => (c.masterNumber, c)).toMap
    logger.trace("Comparing to courses in dump...")
    val doc = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/Courses.xml.xz")))
    val courses = doc \\ "curriculum"
    dataStore.withTransaction { implicit pm =>
      courses foreach ((course: Node) => {
        val courseName = normalize((course \ "@courseInfo.courseName").text.toLowerCase)
        val deptName = normalize((course \ "@courseInfo.departmentName").text.toLowerCase)
        val masterNumber = asIdNumber((course \ "@courseInfo.courseMasterNumber").text)
        logger.debug(s"Looking at Course($courseName, $deptName, $masterNumber)")
        if (coursesByMasterNumber.contains(masterNumber)) {
          logger.trace("Course already in database.")
          val dbCourse = coursesByMasterNumber(masterNumber)
          if (dbCourse.department.name != deptName) {
            val oldDept = dbCourse.department
            val newDept = Department.getOrCreate(deptName)
            logger.info(s"Changed department of $courseName from ${oldDept.name} to $deptName.")
            dbCourse.department = newDept
            val courseCand = QCourse.candidate
            val oldDeptNumCourses: Long = pm.query[Course].filter(courseCand.department.eq(oldDept)).executeResultUnique(true, courseCand.countDistinct()).asInstanceOf[Long]
            if (oldDeptNumCourses == 0) {
              logger.info("Deleting department ${oldDept.name} because it no longer has any courses.")
              pm.deletePersistent(oldDept)
            }
          }
          if (dbCourse.name != courseName) {
            logger.info(s"Changing course name from ${dbCourse.name} to $courseName.")
            dbCourse.name = courseName
          }
        } else {
          val dbCourse = new Course(courseName, masterNumber, Department.getOrCreate(deptName))
          pm.makePersistent(dbCourse)      
        }
      })
    }
  }

  def loadSections(): mutable.Map[String, Section] = {
    logger.info("Importing sections...")
    val dbSectionsByNumber = mutable.Map(dataStore.pm.query[Section].executeList().map(s => (s.sectionId -> s)): _*)
    val doc = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/Sections.xml.xz")))
    val sections = doc \\ "curriculum"
    sections foreach ((section: Node) => {
      dataStore.withTransaction { implicit pm =>
        val sectionId = (section \ "@sectionInfo.sectionID").text
        logger.debug(s"Working on section: $sectionId")
        val courseMasterNumber = asIdNumber((section \ "@courseInfo.courseMasterNumber").text)
        val course = pm.query[Course].filter(QCourse.candidate.masterNumber.eq(courseMasterNumber)).executeOption().get
        val roomNum = (section \ "@sectionInfo.roomName").text
        val room = Room.getOrCreate(roomNum)
        val termStart = (section \ "@sectionSchedule.termStart").text
        val termEnd = (section \ "@sectionSchedule.termEnd").text
        val terms = icToTerms(termStart, termEnd).toSet
        val periodStart = (section \ "@sectionSchedule.periodStart").text
        val periodEnd = (section \ "@sectionSchedule.periodEnd").text
        val dayStart = (section \ "@sectionSchedule.scheduleStart").text
        val dayEnd = (section \ "@sectionSchedule.scheduleEnd").text
        val periods = (periodNames(dayStart, dayEnd, periodStart, periodEnd) map ((p: String) => {
          pm.query[Period].filter(QPeriod.candidate.name.eq(p)).executeOption().get
        })).toSet
        // assumes a single teacher is assigned to a section in the file
        val teacherPersonId = (section \ "@sectionInfo.teacherPersonID").text
        val teacher = pm.query[Teacher].filter(QTeacher.candidate.personId.eq(teacherPersonId)).executeOption()
        dbSectionsByNumber.get(sectionId) match {
          case Some(dbSection) => {
            logger.debug(s"Section already in datastore. Updating to latest info.")
            if (dbSection.course != course) logger.info(s"This section's course changed from ${dbSection.course.name} to ${course.name}. Check on this.")
            else {
              dbSection.room = room
              dbSection.terms.clear
              dbSection.terms ++= terms
              dbSection.periods.clear
              dbSection.periods ++= periods
              val oldTeacherAssignment = pm.query[TeacherAssignment].filter(QTeacherAssignment.candidate.section.eq(dbSection)).executeOption()
              oldTeacherAssignment match {
                case None if (teacher == None) => logger.info("No teacher assigned to this section.")
                case None => {
                  logger.info(s"Assigning ${teacher.get.formalName} to this section.")
                  pm.makePersistent(new TeacherAssignment(teacher.get, dbSection, None, None))
                }
                case Some(ta) if (teacher == None) => {
                  logger.info(s"Section was assigned to ${ta.teacher.formalName}, but not currently assigned. Leaving alone.")
                }
                case Some(ta) => if (ta.teacher != teacher.get) {
                  logger.info(s"Changing teacher from ${ta.teacher.formalName} to ${teacher.get.formalName}.")
                  pm.deletePersistent(ta)
                  pm.makePersistent(new TeacherAssignment(teacher.get, dbSection, None, None))
                } else {
                  logger.debug(s"Section information is unchanged.")
                }
              }
            }
          }
          case None => {
            logger.debug(s"Section not in datastore. Creating.")
            val dbSection = new Section(course, sectionId, terms, periods, room)
            pm.makePersistent(dbSection)
            if (teacher.isDefined) {
              pm.makePersistent(new TeacherAssignment(teacher.get, dbSection, None, None))
            }
          }
          dbSectionsByNumber -= sectionId
        }
      }
    })
    logger.info(s"The following sections weren't in the latest data dump. They will be deleted if they are empty: ${dbSectionsByNumber.keySet.mkString(", ")}")
    dbSectionsByNumber
  }

  def loadEnrollments() {
    val enrCand = QStudentEnrollment.candidate
    val sectionVar = QSection.variable("sectionVar")
    val termVar = QTerm.variable("termVar")
    val dbEnrollments = dataStore.pm.query[StudentEnrollment].filter(
        enrCand.section.eq(sectionVar).and(sectionVar.terms.contains(termVar)).and(
            termVar.year.eq(currentYear))).executeList()
    val dbEnrollmentIds = mutable.Set(dbEnrollments.map(_.id): _*)
    val unknownSections = mutable.Set[String]()
    val doc = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/Schedule.xml.xz")))
    val fileEnrollments = doc \\ "student"
    fileEnrollments foreach ((enrollment: Node) => {
      dataStore.withTransaction { implicit pm =>
        val sectionId = asIdNumber((enrollment \ "@courseSection.sectionID").text)
        val maybeSection: Option[Section] = pm.query[Section].filter(QSection.candidate.sectionId.eq(sectionId)).executeOption()
        val studentNumber = asIdNumber((enrollment \ "@student.studentNumber").text)
        val student = pm.query[Student].filter(QStudent.candidate.studentNumber.eq(studentNumber)).executeOption().get
        val startDate = asLocalDate((enrollment \ "@roster.startDate").text)
        val endDate = asLocalDate((enrollment \ "@roster.endDate").text)
        maybeSection match {
          case Some(section) => {
        	logger.trace("Finding any previous enrollments of this student in this section.")
        	val prevEnrs = pm.query[StudentEnrollment].filter(
        	    enrCand.student.eq(student).and(enrCand.section.eq(section))).executeList()
        	prevEnrs.find(enr => enr.start == startDate || enr.end == endDate) match {
        	  case Some(enr) => {
        	    logger.debug("Student already enrolled in this section. Modifying.")
        	    enr.start = startDate
        	    enr.end = endDate
        	    if (dbEnrollmentIds.contains(enr.id)) dbEnrollmentIds -= enr.id
        	  }
        	  case None => {
        	    logger.debug("Creating new enrollment.")
        	    pm.makePersistent(new StudentEnrollment(student, section, startDate, endDate))
        	  }
        	}
          }
          case None => {
            logger.error(s"Student ${student.formalName} is in section ${sectionId}, which doesn't exist.")
            unknownSections += sectionId
          }
        }
      }
    })
    if (!dbEnrollmentIds.isEmpty) {
      logger.info(s"There were ${dbEnrollmentIds.size} previous enrollments weren't in the data dump. Deleting.")
      dataStore.withTransaction { pm =>
        dbEnrollmentIds foreach (id => {
          pm.query[StudentEnrollment].filter(enrCand.id.eq(id)).executeOption().map(pm.deletePersistent(_))  
        })
      }
    }
    if (!unknownSections.isEmpty) {
      logger.error(s"The following sections were in the file dump, but not in the database: ${unknownSections.mkString(",")}")
    }
  }
  
  def deleteEmptySections(dbSectionsByNumber: Map[String, Section]) {
    dbSectionsByNumber.map { case (sectionId, section) => 
      dataStore.withTransaction { pm => 
        val seCand = QStudentEnrollment.candidate()
        val enrs = pm.query[StudentEnrollment].filter(seCand.section.eq(section)).executeList()
        if (enrs.isEmpty) {
          val taCand = QTeacherAssignment.candidate()
          val tas = pm.query[TeacherAssignment].filter(taCand.section.eq(section)).executeList()
          tas.foreach { ta => pm.deletePersistent(ta) }
          pm.deletePersistent(section)
        }
      }
    }
  }
  
  

  /*def loadLockers(debug: Boolean) {
    dataStore.withTransaction { implicit pm =>
      val doc = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/Lockers.xml.xz")))
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
    val wb = WorkbookFactory.create(new XZInputStream(getClass.getResourceAsStream(s"$folder/StudentNetworkSecurityInfo.xlsx.xz")))
    val sheet: Sheet = wb.getSheetAt(0)
    sheet.removeRow(sheet.getRow(0))
    val pairs = (sheet.asScala.map { (row: Row) =>
      row.getCell(0).getNumericCellValue.toInt.toString -> row.getCell(6).getStringCellValue
    }).toList
    Map(pairs: _*)
  }

  def loadBookData(debug: Boolean = false) {
    if (debug) println("Loading book data...")
    val data = XML.load(new XZInputStream(getClass.getResourceAsStream(s"$folder/bookData.xml.xz")))
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
