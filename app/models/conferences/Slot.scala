package models.conferences

import javax.jdo.annotations._
import models.courses.{ Student, QStudent, Teacher, QTeacher}
import java.sql.{ Date, Time }
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import util.QueryClass
import util.Helpers.localTime2SqlTime

import org.joda.time.LocalTime

import scalajdo.DataStore

@PersistenceCapable(detachable="true")
class Slot {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
   
  @Persistent
  @Column(allowsNull="false")
  private[this] var _session : Session = _
  def session: Session = _session
  def session_=(theSession: Session) { _session = theSession }
  
  @Persistent
  @Column(allowsNull="false")
  private[this] var _teacher : Teacher = _
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) {_teacher = theTeacher}
  
  @Persistent
  @Column(allowsNull="false")
  private[this] var _student : Student = _
  def student: Student = _student
  def student_=(theStudent: Student) {_student = theStudent}
  
  @Column(allowsNull="true")
  private[this] var _parentName : String = _
  def parentName: String = _parentName
  def parentName_=(theParentName: String) {_parentName = theParentName}
  
  // TODO: should we use models.users.Email?
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
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _startTime : Time = _
  def startTime: LocalTime = LocalTime.fromDateFields(_startTime)
  def startTime_=(theStartTime: LocalTime) { _startTime = localTime2SqlTime(theStartTime) }
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _endTime : Time = _
  def endTime: LocalTime = LocalTime.fromDateFields(_endTime)
  def endTime_=(theEndTime: LocalTime) { _endTime = localTime2SqlTime(theEndTime) }
  
  /**
   * length of conference in minutes
   */
  @Column(allowsNull="false")
  private[this] var _slotInterval: Int = _
  def slotInterval: Int = _slotInterval
  def slotInterval_=(theSlotInterval: Int) {_slotInterval = theSlotInterval}
  
  def this(theSession: Session, theTeacher: Teacher, theStudent: Student, theStartTime: LocalTime, theParentName: String,
      theEmail: String, thePhone: String, theAlternatePhone: Option[String], theComment: Option[String], theSlotInterval: Int) = {
    this()
    session_=(theSession)
    teacher_=(theTeacher)
    student_=(theStudent)
    startTime_=(theStartTime)
    parentName_=(theParentName)
    email_=(theEmail)
    phone_=(thePhone)
    alternatePhone_=(theAlternatePhone)
    comment_=(theComment)
    slotInterval_=(theSlotInterval)
    endTime_=(theStartTime.plusMinutes(theSlotInterval))
  }
  
  def calculateEndTime(startTime: Time, slotInterval: Int): java.sql.Time = {
    new Time(startTime.getTime() + (60000 * slotInterval))
  }
  
  //Checks if the slot lies within the session's start and end times
  def validateSession: Boolean = {
    val timeOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
    import timeOrdering._
    (this.startTime < this.session.startTime || this.startTime >= this.session.endTime) ||
    (this.endTime <= this.session.startTime || this.endTime > this.session.endTime)
  } 
  
  //Checks if the slot overlaps another slot's time period
  def validateSlot: Boolean = {
    val dateOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
    import dateOrdering._
    val startTime = this.startTime
    val endTime = this.endTime
    DataStore.execute { implicit pm =>
      val cand = QSlot.candidate
	  val slots = pm.query[Slot].filter(cand.teacher.eq(this.teacher).and(cand.session.eq(this.session))).executeList()
      slots.exists(s => (startTime >= s.startTime && startTime <
      	s.endTime) || (endTime > s.startTime && endTime <= s.endTime))
    }
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
  
  private[this] lazy val _slotInterval: NumericExpression[Int] = new NumericExpressionImpl(this, "_slotInterval")
  def slotInterval: NumericExpression[Int] = _slotInterval
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