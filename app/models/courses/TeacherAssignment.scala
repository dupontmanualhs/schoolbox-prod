package models.courses

import javax.jdo.annotations._
import models.users.Teacher
import org.joda.time._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class TeacherAssignment {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _teacher: Teacher = _
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) { _teacher = theTeacher }

  @Persistent(defaultFetchGroup="true")
  private[this] var _section: Section = _
  def section: Section = _section
  def section_=(theSection: Section) { _section = theSection }
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _start: java.sql.Date = _
  def start: LocalDate = if (_start != null) new DateTime(_start).toLocalDate else section.startDate
  def start_=(theStart: LocalDate) { _start = if (theStart != null) new java.sql.Date(theStart.toDateTimeAtStartOfDay.toDate.getTime) else null }
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _end: java.sql.Date = _
  def end: LocalDate = if (_end != null) new DateTime(_end).toLocalDate else section.endDate
  def end_=(theEnd: LocalDate) { _end = if (theEnd != null) new java.sql.Date(theEnd.toDateTimeAtStartOfDay.toDate.getTime) else null }
  
  def this(theTeacher: Teacher, theSection: Section, theStart: LocalDate, theEnd: LocalDate) = {
    this()
    teacher_=(theTeacher)
    section_=(theSection)
    start_=(theStart)
    end_=(theEnd)
  }
  
  def term: Set[Term] = this.section.terms
  
}

trait QTeacherAssignment extends PersistableExpression[TeacherAssignment] {
  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl[Teacher](this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher
  
  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section
    
  private[this] lazy val _start: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_start")
  def start: DateExpression[java.util.Date] = _start

  private[this] lazy val _end: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_end")
  def end: DateExpression[java.util.Date] = _end
}

object QTeacherAssignment {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QTeacherAssignment = {
    new PersistableExpressionImpl[TeacherAssignment](parent, name) with QTeacherAssignment
  }
  
  def apply(cls: Class[TeacherAssignment], name: String, exprType: ExpressionType): QTeacherAssignment = {
    new PersistableExpressionImpl[TeacherAssignment](cls, name, exprType) with QTeacherAssignment
  }
  
  private[this] lazy val jdoCandidate: QTeacherAssignment = candidate("this")
  
  def candidate(name: String): QTeacherAssignment = QTeacherAssignment(null, name, 5)
  
  def candidate(): QTeacherAssignment = jdoCandidate
  
  def parameter(name: String): QTeacherAssignment = QTeacherAssignment(classOf[TeacherAssignment], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QTeacherAssignment = QTeacherAssignment(classOf[TeacherAssignment], name, ExpressionType.VARIABLE)
}