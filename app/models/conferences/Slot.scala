package models.conferences

import javax.jdo.annotations._
import models.users._
import java.sql.Date
import java.sql.Time
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import util.QueryClass

@PersistenceCapable(detachable="true")
class Slot {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
    
  @Column(allowsNull="false")
  private[this] var _session : Session = _
  def session: Session = _session
  def session_=(theSession: Session) { _session = theSession }
  
  @Column(allowsNull="false")
  private[this] var _teacher : Teacher = _
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) {_teacher = theTeacher}
  
  @Column(allowsNull="false")
  private[this] var _student : Student = _
  def student: Student = _student
  def student_=(theStudent: Student) {_student = theStudent}
  
  @Column(allowsNull="true")
  private[this] var _parentName : String = _
  def parentName: String = _parentName
  def parentName_=(theParentName: String) {_parentName = theParentName}
  
  @Column(allowsNull="true")
  private[this] var _email : String = _
  def email: String = _email
  def email_=(theEmail: String) {_email = theEmail}
  
  @Column(allowsNull="true")
  private[this] var _phone : String = _
  def phone: String = _phone
  def phone_=(thePhone: String) {_phone = thePhone}
  
  @Column(allowsNull="true")
  private[this] var _alternatePhone : String = _
  def alternatePhone: Option[String] = if (_alternatePhone == null) None else Some(_alternatePhone)
  def alternatePhone_=(theAlternatePhone: Option[String]) {_alternatePhone = theAlternatePhone.getOrElse(null)}
  
  @Column(allowsNull="true")
  private[this] var _comment : String = _
  def comment: Option[String] = if (_comment == null) None else Some(_comment)
  def comment_=(theComment: Option[String]) {_comment = theComment.getOrElse(null)}
  
  @Column(allowsNull="false")
  private[this] var _startTime : java.sql.Time = _
  def startTime: java.sql.Time = _startTime
  def startTime_=(theStartTime: java.sql.Time) {_startTime = theStartTime}
  
  @Column(allowsNull="false")
  private[this] var _endTime : java.sql.Time = _
  def endTime: java.sql.Time = _endTime
  def endTime_=(theEndTime: java.sql.Time) {_endTime = theEndTime}
  
  def this(session: Session, teacher: Teacher, student: Student, startTime: java.sql.Time, parentName: String, email: String, phone: String, alternatePhone: Option[String], comment: Option[String]) = {
    this()
    _session = session
    _teacher = teacher
    _student = student
    _startTime = startTime
    _endTime = calculateEndTime(_startTime, _session.slotInterval)
    _parentName = parentName
    _email = email
    _phone = phone
    _alternatePhone = alternatePhone.getOrElse(null)
    _comment = comment.getOrElse(null)
  }
  
  
  //TODO: Work on adding 0's in front of single digits and finish it
  def calculateEndTime(startTime: java.sql.Time, slotInterval: Int): java.sql.Time = {
    //converts startTime to "hh:mm:dd" format, if someone knows a better way tell Ken
    var initialTime = startTime.toString
    //splits the sections of the time
    var sections = initialTime.split(":")
    var hours = sections(0)
    var minutes = sections(1)
    var seconds = sections(2)
    //Adds the slot Time to the current time, accounts for the bounds on minutes(0-59) and hours(0-23)
    if (minutes.toInt + slotInterval > 59) {
      hours = ((hours.toInt + 1) % 24).toString 
    }
    minutes = ((minutes.toInt + slotInterval) % 60).toString
    //Adds 0 to hours and minutes if they are a single digit
    if (hours.length == 1) hours = 0 + hours
    if (minutes.length == 1) minutes = 0 + minutes
    Time.valueOf(hours + ":" + minutes + ":" + seconds)
  }
  
}

trait QSlot extends PersistableExpression[Slot] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _session: ObjectExpression[Session] = new ObjectExpressionImpl[Session](this, "_session")
  def session: ObjectExpression[Session] = _session
  
  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl(this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher
  
  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student
  
  private[this] lazy val _startTime: TimeExpression[java.util.Date] = new TimeExpressionImpl[java.sql.Time](this, "_startTime")
  def startTime: TimeExpression[java.util.Date] = _startTime
  
  private[this] lazy val _endTime: TimeExpression[java.util.Date] = new TimeExpressionImpl[java.sql.Time](this, "_endTime")
  def endTime: TimeExpression[java.util.Date] = _endTime
  
  private[this] lazy val _parentName: StringExpression = new StringExpressionImpl(this, "_parentName")
  def parentName: StringExpression = _parentName
  
  private[this] lazy val _email: StringExpression = new StringExpressionImpl(this, "_email")
  def email: StringExpression = _email
  
  private[this] lazy val _phone: StringExpression = new StringExpressionImpl(this, "_phone")
  def phone: StringExpression = _phone
  
  private[this] lazy val _alternatePhone: StringExpression = new StringExpressionImpl(this, "_alternatePhone")
  def alternatePhone: StringExpression = _alternatePhone
  
  private[this] lazy val _comment: StringExpression = new StringExpressionImpl(this, "_comment")
  def comment: StringExpression = _comment
}

object QSlot extends QueryClass[Slot, QSlot] {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QSlot = {
    new PersistableExpressionImpl[Slot](parent, name) with QSlot
  }
  
  def apply(cls: Class[Slot], name: String, exprType: ExpressionType): QSlot = {
    new PersistableExpressionImpl[Slot](cls, name, exprType) with QSlot
  }
  
  def myClass = classOf[Slot]
}