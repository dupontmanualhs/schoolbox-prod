package models

import scala.collection.mutable
import java.util.Properties
import scala.collection.JavaConverters._
import scala.xml.{ Elem, Node, NodeSeq, XML }
import config.users.UsesDataStore
import org.tukaani.xz.XZInputStream
import org.joda.time.DateTime
import models.users._
import models.books._
import models.conferences._
import models.courses._
import org.joda.time.{ LocalDate, LocalDateTime, LocalTime }

object AccuScholarData extends UsesDataStore {
  def load(debug: Boolean = true) {
    val classes = dataStore.persistentClasses.asJava
    val props = new Properties()
    dataStore.storeManager.deleteSchema(classes, props)
    dataStore.storeManager.createSchema(classes, props)
    loadAccuScholarData(debug)
  }

  def loadAccuScholarData(debug: Boolean) {
    val doc = XML.load(new XZInputStream(getClass.getResourceAsStream("/accuscholar-data.xml.xz")))
    loadStudents(doc \ "students", debug)
    loadTeachers(doc \ "teachers", debug)
    loadGuardians(doc \ "guardians", debug)
    loadYearsTermsAndPeriods(debug)
    loadCourses(doc \ "courses", debug)
    loadSections(doc \ "sections", debug)
    loadConferences(doc \ "conferences", debug)
    loadBooks(doc \ "books", debug)
  }

  def loadStudents(elem: NodeSeq, debug: Boolean) {
    val students = elem \\ "student"
    dataStore.withTransaction { pm =>
      students foreach ((student: Node) => {
        val username = (student \ "@username").text
        val last = (student \ "@lastName").text
        val first = (student \ "@firstName").text
        val preferred = asOptionString((student \ "@preferredName").text)
        val middle = asOptionString((student \ "@middle").text)
        val gender = asGender((student \ "@gender").text)
        val stateId = (student \ "@stateId").text
        val studentNumber = (student \ "@studentNumber").text
        val email = asOptionString((student \ "@email").text)
        val grade = asInt((student \ "@grade").text)
        val teamName = (student \ "@magnet").text
        val password = (student \ "@password").text
        val isActive = false
        val isSuperuser = false
        val isStaff = false
        val lastLogin = DateTime.parse((student \ "@lastLogin").text)
        val dateJoined = DateTime.parse((student \ "@dateJoined").text)
        if (debug) println(s"\nAdding student $username, id $stateId")
        val user = new User(username, first, middle, last, preferred, gender, email, None, isActive, isStaff, isSuperuser)
        user.password = Password.fromEncoding(password)
        pm.makePersistent(user)
        if (debug) println("  user saved")
        val dbStudent = new Student(user, stateId, studentNumber, grade, teamName)
        pm.makePersistent(dbStudent)
        if (debug) println("  student saved")
      })
    }
  }
  
  def loadTeachers(elem: NodeSeq, debug: Boolean) {
    val teachers = elem \\ "teacher"
    dataStore.withTransaction { pm =>
      teachers.foreach { teacher =>
        val username = (teacher \ "@username").text
        val last = (teacher \ "@lastName").text
        val first = (teacher \ "@firstName").text
        val preferred = asOptionString((teacher \ "@preferredName").text)
        val middle = asOptionString((teacher \ "@middle").text)
        val gender = asGender((teacher \ "@gender").text)
        val stateId = (teacher \ "@stateId").text
        val personId = (teacher \ "@personId").text
        val email = asOptionString((teacher \ "@email").text)
        val password = (teacher \ "@password").text
        val isActive = false
        val isSuperuser = false
        val isStaff = false
        val lastLogin = DateTime.parse((teacher \ "@lastLogin").text)
        val dateJoined = DateTime.parse((teacher \ "@dateJoined").text)
        if (debug) println(s"\nAdding teacher $username, id $stateId")
        val user = new User(username, first, middle, last, preferred, gender, email, None, isActive, isStaff, isSuperuser)
        user.password = Password.fromEncoding(password)
        pm.makePersistent(user)
        if (debug) println("  user saved")
        val dbTeacher = new Teacher(user, personId, stateId)
        pm.makePersistent(dbTeacher)
        if (debug) println("  teacher saved")   
      }
    }
  }
  
  def loadGuardians(elem: NodeSeq, debug: Boolean) {
    val guardians = elem \\ "guardian"
    dataStore.withTransaction { pm =>
      guardians foreach ((guardian: Node) => {
        val username = (guardian \ "@username").text
        val last = (guardian \ "@lastName").text
        val first = (guardian \ "@firstName").text
        val preferred = asOptionString((guardian \ "@preferredName").text)
        val middle = asOptionString((guardian \ "@middle").text)
        val gender = asGender((guardian \ "@gender").text)
        val email = asOptionString((guardian \ "@email").text)
        val password = (guardian \ "@password").text
        val isActive = false
        val isSuperuser = false
        val isStaff = false
        val lastLogin = DateTime.parse((guardian \ "@lastLogin").text)
        val dateJoined = DateTime.parse((guardian \ "@dateJoined").text)
        val childrenUserNames = (guardian \ "child").map(_.text).toSet
        if (debug) println(s"\nAdding guardian $username")
        val user = User.getByUsername(username) match {
          case Some(dbUser) => {
            // already in database (teacher or student)
            if (debug) println("  user already in database")
            dbUser
          }
          case None => {
            val dbUser = new User(username, first, middle, last, preferred, gender, email, None, isActive, isStaff, isSuperuser)
            dbUser.password = Password.fromEncoding(password)
            pm.makePersistent(dbUser) 
            if (debug) println("  user saved")
            dbUser
          }
        }
        val children = childrenUserNames.map(Student.getByUsername(_).get)
        val dbGuardian = new Guardian(user, children)
        pm.makePersistent(dbGuardian)
        if (debug) println("  guardian saved")
      }
    )}
  }
  
  val yearData = 
    <years>
      <year name="2009-10">
        <term name="Fall 2009" slug="f09" start="2009-08-13" end="2009-12-18" />
        <term name="Spring 2010" slug="s10" start="2010-01-05" end="2010-05-25" />
      </year>
      <year name="2010-11">
        <term name="Fall 2010" slug="f10" start="2010-08-13" end="2010-12-18" />
        <term name="Spring 2011" slug="s11" start="2011-01-05" end="2011-08-01" />
      </year>
      <year name="2011-12">
        <term name="Fall 2011" slug="f11" start="2011-08-15" end="2011-12-16" />
        <term name="Spring 2012" slug="s12" start="2012-01-03" end="2012-08-01" />
      </year>
      <year name="2012-13">
        <term name="Fall 2012" slug="f12" start="2012-08-01" end="2013-01-11" />
        <term name="Spring 2013" slug="s13" start="2013-01-14" end="2013-06-05" />
      </year>
      <year name="2013-14">
        <term name="Fall 2013" slug="f13" start="2013-08-01" end="2014-01-10" />
        <term name="Spring 2014" slug="s14" start="2014-01-13" end="2014-06-30" />
      </year>
    </years>

  
  def loadYearsTermsAndPeriods(debug: Boolean) {
    val years = yearData \\ "year"
    dataStore.withTransaction { implicit pm =>
      years.foreach { (year: Node) => 
        val name = (year \ "@name").text
        val acadYear = new AcademicYear(name)
        pm.makePersistent(acadYear)
        (year \\ "term").foreach { term =>
          val termName = (term \ "@name").text
          val slug = (term \ "@slug").text
          val start = LocalDate.parse((term \ "@start").text)
          val end = LocalDate.parse((term \ "@end").text)
          val dbTerm = new Term(termName, acadYear, slug, start, end)
          pm.makePersistent(dbTerm)
        }
      }
      val periods: List[Period] = List(
        new Period("Red 1", 1, "r1"), new Period("Red 2", 2, "r2"), new Period("Red 3", 3, "r3"), 
        new Period("Red 4", 4, "r4"), new Period("Red 5", 5, "r5"),
        new Period("Red Activity", 6, "ract"), new Period("Red Advisory", 7, "radv"),
        new Period("White 1", 8, "w1"), new Period("White 2", 9, "w2"), new Period("White 3", 10, "w3"),
        new Period("White 4", 11, "w4"), new Period("White 5", 12, "w5"),
        new Period("White Activity", 13, "wact"), new Period("White Advisory", 14, "wadv"))
      pm.makePersistentAll(periods)
      if (debug) println("Created AcademicYear, Terms, and Periods")
    }
  }
  
  def loadCourses(elem: NodeSeq, debug: Boolean) {
    val courses = elem \\ "course"
    dataStore.withTransaction { pm =>
      courses foreach ((course: Node) => {
        val name = (course \ "@name").text
        val masterNumber = (course \ "@masterNumber").text
        val dept = {
          val deptName = (course \ "@department").text
          if (deptName == "") "None"
          else deptName
        }
        if (debug) println(s"\nSaving course $name, $masterNumber, $dept")
        val dbCourse = new Course(name, masterNumber, Department.getOrCreate(dept))
        pm.makePersistent(dbCourse)
        if (debug) println("  course saved")
      })
    }
  }
  
  def loadSections(elem: NodeSeq, debug: Boolean) {
    def translate(s: String): String = {
      if (s.startsWith("spr")) "s" + s.substring(3)
      else if (s.startsWith("fall")) "f" + s.substring(4)
      else s
    }
    val sections = elem \\ "section"
    sections foreach ((section: Node) => {
      dataStore.withTransaction { pm =>
        val number = (section \ "@number").text
        if (debug) println(s"\nCreating section number $number...")
        val room = Room.getOrCreate((section \ "@room").text)
        val course = Course.getByMasterNumber((section \ "@course").text).get
        val terms = (section \ "terms").text.split(",").toSet[String].map(s => Term.getBySlug(translate(s))).flatten
        val periods = (section \ "periods").text.split(",").toSet[String].map(Period.getBySlug(_)).flatten
        val teachers = (section \ "teachers").text.split(",").toSet[String].map(Teacher.getByUsername(_)).flatten
        val students = (section \ "students").text.split(",").toSet[String].map(Student.getByUsername(_)).flatten
        val dbSection = new Section(course, number, terms, periods, room)
        pm.makePersistent(dbSection)
        if (debug) println("  section saved")
        teachers foreach ((teacher: Teacher) => {
          val teacherAssignment = new TeacherAssignment(teacher, dbSection, None, None)
          pm.makePersistent(teacherAssignment)
        })
        students foreach ((student: Student) => {
          val enrollment = new StudentEnrollment(student, dbSection, None, None)
          pm.makePersistent(enrollment)
        })
        if (debug) println("  teachers and students saved")
      }
    })
  }
  
  def loadConferences(elem: NodeSeq, debug: Boolean) {
    val events = elem \\ "event"
    events foreach ((event: Node) => {
      dataStore.withTransaction { pm => 
        val name = (event \ "@name").text
        if (debug) println(s"\nCreating conference event $name")
        val dbEvent = new Event(name, false)
        pm.makePersistent(dbEvent)
        if (debug) println("  done")
        val sessions = event \\ "session"
        sessions foreach ((session: Node) => {
          val date = LocalDate.parse((session \ "@date").text)
          val cutoff = LocalDateTime.parse((session \ "@cutoff").text)
          val start = LocalTime.parse((session \ "@start").text)
          val end = LocalTime.parse((session \ "@end").text)
          val interval = (session \ "@interval").text.toInt
          val dbSession = new Session(dbEvent, date, cutoff, None, start, end)
          if (debug) println(s"  Creating session on $date at $start")
          pm.makePersistent(dbSession)
          if (debug) println("    done")
          val slots = session \\ "slot"
          slots foreach ((slot: Node) => {
            val teacher = Teacher.getByUsername((slot \ "@teacher").text).get
            val startTime = LocalTime.parse((slot \ "@startTime").text)
            val guardians = (slot \ "parents").text.split(",").toSet[String].map(s => Guardian.getByUsername(s)).flatten
            val phone = asOptionString((slot \ "@phone").text)
            val altPhone = asOptionString((slot \ "@altPhone").text)
            val comment = asOptionString((slot \ "comments").text)
            if (debug) println(s"  Creating slot for ${teacher.displayName} at $startTime")
            val dbSlot = new Slot(dbSession, teacher, startTime, interval, Set(), guardians, phone, altPhone, comment)
            pm.makePersistent(dbSlot)
            if (debug) println("    done")
          })
        })
      }
    })
  }
  
  def loadBooks(elem: NodeSeq, debug: Boolean) {
    val titles = elem \\ "title"
    titles foreach ((title: Node) => {
      dataStore.withTransaction { pm => 
        val name = (title \ "@name").text
        if (debug) println(s"\nCreating book $name")
        val author = asOptionString((title \ "@author").text)
        val publisher = asOptionString((title \ "@publisher").text)
        val isbn = (title \ "@isbn").text
        val numPages = asOptionInt((title \ "@numPages").text)
        val dimensions = asOptionString((title \ "@dimensions").text)
        val weight = asOptionDouble((title \ "@weight").text)
        val lastModified = Some(LocalDateTime.parse((title \ "@lastModified").text))
        val dbTitle = new Title(name, author, publisher, isbn, numPages, dimensions,
            weight, true, lastModified, None)
        pm.makePersistent(dbTitle)
        if (debug) println("  saved")
        val pgs = title \\ "purchaseGroup"
        pgs foreach ((pg: Node) => {
          val date = LocalDate.parse((pg \ "@date").text)
          if (debug) println(s"  Creating purchase group from $date")
          val price = asDouble((pg \ "@price").text)
          val dbPg = new PurchaseGroup(dbTitle, date, price)
          pm.makePersistent(dbPg)
          if (debug) println("    done")
          val copies = pg \\ "copy"
          copies foreach ((copy: Node) => {
            val number = asInt((copy \ "@number").text)
            if (debug) println(s"  Creating copy $number")
            val isLost = (copy \ "@isLost").text.toBoolean
            val dbCopy = new Copy(dbPg, number, isLost, false)
            pm.makePersistent(dbCopy)
            if (debug) println("    done")
            val checkouts = copy \\ "checkout"
            checkouts foreach ((co: Node) => {
              val student = Student.getByUsername((co \ "@student").text).get
              val start = LocalDate.parse((co \ "@start").text)
              val end = asOptionString((co \ "@end").text).map(s => LocalDate.parse(s))
              val dbCheckout = new Checkout(student, dbCopy, Some(start), end)
              pm.makePersistent(dbCheckout)
            })
          })
        })
      }
    })
  }
         
  def asInt(s: String): Int = {
    try {
      s.toInt
    } catch {
      case e: NumberFormatException => null.asInstanceOf[Int]
    }
  }
  
  def asOptionInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }

  def asDouble(s: String): Double = {
    try {
      s.toDouble
    } catch {
      case e: NumberFormatException => null.asInstanceOf[Double]
    }
  }

  def asOptionDouble(s: String): Option[Double] = {
    try {
      Some(s.toDouble)  
    } catch {
      case e: NumberFormatException => None
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
}