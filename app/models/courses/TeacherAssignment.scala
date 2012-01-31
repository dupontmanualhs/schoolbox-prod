package models.courses

import javax.jdo.annotations._
import models.users.Teacher
import org.joda.time._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

import java.sql.Date

@PersistenceCapable(detachable="true")
class TeacherAssignment {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _teacher: Teacher = _
  private[this] var _section: Section = _
  private[this] var _term: Term = _
  @Persistent
  private[this] var _start: Date = _
  @Persistent
  private[this] var _end: Date = _
  
  def this(teacher: Teacher, section: Section,
      term: Term, start: LocalDate, end: LocalDate) = {
    this()
    _teacher = teacher
    _section = section
    _term = term
    start_=(start)
    end_=(end)
  }

  def id: Long = _id
  
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) { _teacher = theTeacher }

  def section: Section = _section
  def section_=(theSection: Section) { _section = theSection }
  
  def term: Term = _term
  def term_=(theTerm: Term) { _term = theTerm }
  
  def start: LocalDate = new DateTime(_start).toLocalDate
  def start_=(theStart: LocalDate) { _start = if (theStart != null) new Date(theStart.toDateTimeAtStartOfDay.toDate.getTime) else null }
  
  def end: LocalDate = new DateTime(_end).toLocalDate
  def end_=(theEnd: LocalDate) { _end = if (theEnd != null) new Date(theEnd.toDateTimeAtStartOfDay.toDate.getTime) else null }
}

trait QTeacherAssignment extends PersistableExpression[TeacherAssignment] {
  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl[Teacher](this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher
  
  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section
  
  private[this] lazy val _term: ObjectExpression[Term] = new ObjectExpressionImpl[Term](this, "_term")
  def term: ObjectExpression[Term] = _term
  
  private[this] lazy val _start: DateExpression[java.util.Date] = new DateExpressionImpl[Date](this, "_start")
  def start: DateExpression[java.util.Date] = _start

  private[this] lazy val _end: DateExpression[java.util.Date] = new DateExpressionImpl[Date](this, "_end")
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