package models.courses

import javax.jdo.annotations._
import scala.collection.mutable
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
  def start: Option[LocalDate] = Option(_start).map(LocalDate.fromDateFields(_))
  def start_=(theStart: Option[LocalDate]) {
    if (theStart.isDefined) _start = new java.sql.Date(theStart.get.toDateTimeAtStartOfDay.getMillis)
    else _start = null
  }
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _end: java.sql.Date = _
  def end: Option[LocalDate] = Option(_end).map(LocalDate.fromDateFields(_))
  def end_=(theEnd: Option[LocalDate]) {
    if (theEnd.isDefined) _end = new java.sql.Date(theEnd.get.toDateTimeAtStartOfDay.getMillis)
    else _end = null
  }
  
  def this(theTeacher: Teacher, theSection: Section, theStart: Option[LocalDate], theEnd: Option[LocalDate]) = {
    this()
    teacher_=(theTeacher)
    section_=(theSection)
    start_=(theStart)
    end_=(theEnd)
  }
  
  def term: mutable.Set[Term] = this.section.terms
  
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