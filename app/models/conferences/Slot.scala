package models.conferences

import javax.jdo.annotations._
import models.users._
import java.sql.Date

@PersistenceCapable(detachable="true")
class Slot {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _session : Session = _
  private[this] var _teacher : Teacher = _
  private[this] var _student : Student = _
  private[this] var _startTime : java.sql.Time = _
  private[this] var _parentName : String = _
  private[this] var _email : String = _
  private[this] var _phone : String = _
  private[this] var _alternatePhone : String = _
  private[this] var _comment : String = _
  
  def this(session: Session, teacher: Teacher, student: Student, startTime: java.sql.Time, parentName: String, email: String, phone: String, alternatePhone: String, comment: String) = {
    this()
    _session = session
    _teacher = teacher
    _student = student
    _startTime = startTime
    _parentName = parentName
    _email = email
    _phone = phone
    _alternatePhone = alternatePhone
    _comment = comment
  }
  
  def session: Session = _session
  def session_=(theSession: Session) {_session = theSession}
  
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) {_teacher = theTeacher}
  
  def student: Student = _student
  def student_=(theStudent: Student) {_student = theStudent}
  
  def startTime: java.sql.Time = _startTime
  def startTime_=(theStartTime: java.sql.Time) {_startTime = theStartTime}
  
  def parentName: String = _parentName
  def parentName_=(theParentName: String) {_parentName = theParentName}
  
  def email: String = _email
  def email_=(theEmail: String) {_email = theEmail}
  
  def phone: String = _phone
  def phone_=(thePhone: String) {_phone = thePhone}
  
  def alternatePhone: String = _alternatePhone
  def alternatePhone_=(theAlternatePhone: String) {_alternatePhone = theAlternatePhone}
  
  def comment: String = _comment
  def comment_=(theComment: String) {_comment = theComment}
}

//TODO OB Stuff