package models.courses

import javax.jdo.annotations._
import org.joda.time.{LocalDate, DateTime}
import models.users.Student
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

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
  def start: LocalDate = if (_start != null) new DateTime(_start).toLocalDate else section.startDate
  def start_=(theStart: LocalDate) { _start = if (theStart != null) new java.sql.Date(theStart.toDateTimeAtStartOfDay.toDate.getTime) else null }
  
  @Persistent
  private[this] var _end: java.sql.Date = _
  def end: LocalDate = if (_end != null) new DateTime(_end).toLocalDate else section.endDate
  def end_=(theEnd: LocalDate) { _end = if (theEnd != null) new java.sql.Date(theEnd.toDateTimeAtStartOfDay.toDate.getTime) else null }
  
  def this(theStudent: Student, theSection: Section, theStart: LocalDate, theEnd: LocalDate) = {
    this()
    student_=(theStudent)
    section_=(theSection)
    start_=(theStart)
    end_=(theEnd)
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