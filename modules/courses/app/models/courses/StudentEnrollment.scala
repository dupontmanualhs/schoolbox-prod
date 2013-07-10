package models.courses

import javax.jdo.annotations._
import org.joda.time.{LocalDate, DateTime}
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users.Permission

@PersistenceCapable(detachable="true")
class StudentEnrollment {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Persistent
  private[this] var _student: Student = _
  def student: Student = _student
  def student_=(theStudent: Student) { _student = theStudent }
  
  @Persistent
  private[this] var _section: Section = _
  def section: Section = _section
  def section_=(theSection: Section) { _section = theSection }

  @Persistent
  private[this] var _start: java.sql.Date = _
  def start: Option[LocalDate] = Option(_start).map(LocalDate.fromDateFields(_))
  def start_=(theStart: Option[LocalDate]) {
    if (theStart.isDefined) _start = new java.sql.Date(theStart.get.toDateTimeAtStartOfDay.getMillis)
    else _start = null
  }
  
  @Persistent
  private[this] var _end: java.sql.Date = _
  def end: Option[LocalDate] = Option(_end).map(LocalDate.fromDateFields(_))
  def end_=(theEnd: Option[LocalDate]) {
    if (theEnd.isDefined) _end = new java.sql.Date(theEnd.get.toDateTimeAtStartOfDay.getMillis)
    else _end = null
  }
  
  def this(theStudent: Student, theSection: Section, theStart: Option[LocalDate], theEnd: Option[LocalDate]) = {
    this()
    student_=(theStudent)
    section_=(theSection)
    start_=(theStart)
    end_=(theEnd)
  }
}

object StudentEnrollment {
  object Permissions {
    val Change = Permission(classOf[StudentEnrollment], 1, "Change", "can change any student's schedule")
    val View = Permission(classOf[StudentEnrollment], 2, "View", "can view any student's schedule")
  }
}

trait QStudentEnrollment extends PersistableExpression[StudentEnrollment] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student
  
  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section
  
  private[this] lazy val _start: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_start")
  def start: DateExpression[java.util.Date] = _start

  private[this] lazy val _end: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_end")
  def end: DateExpression[java.util.Date] = _end
}

object QStudentEnrollment {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QStudentEnrollment = {
    new PersistableExpressionImpl[StudentEnrollment](parent, name) with QStudentEnrollment
  }
  
  def apply(cls: Class[StudentEnrollment], name: String, exprType: ExpressionType): QStudentEnrollment = {
    new PersistableExpressionImpl[StudentEnrollment](cls, name, exprType) with QStudentEnrollment
  }
  
  private[this] lazy val jdoCandidate: QStudentEnrollment = candidate("this")
  
  def candidate(name: String): QStudentEnrollment = QStudentEnrollment(null, name, 5)
  
  def candidate(): QStudentEnrollment = jdoCandidate
  
  def parameter(name: String): QStudentEnrollment = QStudentEnrollment(classOf[StudentEnrollment], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QStudentEnrollment = QStudentEnrollment(classOf[StudentEnrollment], name, ExpressionType.VARIABLE)
}