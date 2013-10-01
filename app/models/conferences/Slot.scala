package models.conferences

import scala.collection.JavaConverters._
import javax.jdo.annotations._
import models.courses.{ Guardian, QGuardian, Student, QStudent, Teacher, QTeacher}
import java.sql.{ Date, Time }
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import util.QueryClass
import util.Helpers.localTime2SqlTime
import org.joda.time.LocalTime
import config.users.UsesDataStore
import models.users.DbEquality
import models.courses.Term
import org.dupontmanual.forms.validators.ValidationError

@PersistenceCapable(detachable="true")
class Slot extends UsesDataStore with DbEquality[Slot] {
  @NotPersistent
  val dateOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
  import dateOrdering._

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
  def teacher_=(theTeacher: Teacher) { _teacher = theTeacher }
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _startTime : Time = _
  def startTime: LocalTime = LocalTime.fromMillisOfDay(_startTime.getTime())
  def startTime_=(theStartTime: LocalTime) { _startTime = localTime2SqlTime(theStartTime) }
  
  /**
   * length of conference in minutes
   */
  @Column(allowsNull="false")
  private[this] var _slotInterval: Int = _
  def slotInterval: Int = _slotInterval
  def slotInterval_=(theSlotInterval: Int) { _slotInterval = theSlotInterval }
  
  @Persistent
  @Join
  @Element(types=Array(classOf[Student]))
  private[this] var _students : java.util.Set[Student] = _
  def students: Set[Student] = {
    if (!_students.isEmpty()) _students.asScala.toSet
    else {
      // TODO: term should be based on when the conference is held, not current
      val fromTeacher = teacher.studentsTaught(Term.current).toSet
      val fromGuardians = guardians.flatMap(_.children)
      val intersection = fromTeacher.intersect(fromGuardians)
      _students = intersection.asJava
      intersection
    }
  }
  def students_=(theStudents: Set[Student]) { _students = theStudents.asJava }
  
  @Persistent
  @Join
  @Element(types=Array(classOf[Guardian]))
  private[this] var _guardians: java.util.Set[Guardian] = _
  def guardians: Set[Guardian] = _guardians.asScala.toSet
  def guardians_=(theGuardians: Set[Guardian]) { _guardians = theGuardians.asJava }
    
  @Column(allowsNull="true")
  private[this] var _phone : String = _
  def phone: Option[String] = Option(_phone)
  def phone_=(thePhone: Option[String]) {_phone = thePhone.getOrElse(null)}
  
  @Column(allowsNull="true")
  private[this] var _alternatePhone : String = _
  def alternatePhone: Option[String] = Option(_alternatePhone)
  def alternatePhone_=(theAlternatePhone: Option[String]) {_alternatePhone = theAlternatePhone.getOrElse(null)}
  
  @Column(allowsNull="true")
  private[this] var _comment : String = _
  def comment: Option[String] = Option(_comment)
  def comment_=(theComment: Option[String]) {_comment = theComment.getOrElse(null)}
  
  def this(session: Session, teacher: Teacher, startTime: LocalTime, slotInterval: Int, 
      students: Set[Student], guardians: Set[Guardian], phone: Option[String], 
      alternatePhone: Option[String],  comment: Option[String]) = {
    this()
    session_=(session)
    teacher_=(teacher)
    startTime_=(startTime)
    slotInterval_=(slotInterval)
    students_=(students)
    guardians_=(guardians)
    phone_=(phone)
    alternatePhone_=(alternatePhone)
    comment_=(comment)
  }
  
  def this(session: Session, teacher: Teacher, startTime: LocalTime,
      students: Set[Student], guardians: Set[Guardian], phone: Option[String],
      alternatePhone: Option[String], comments: Option[String]) = {
    this(session, teacher, startTime, 10, students, guardians, phone, alternatePhone, comments)
  }
  
  def endTime: LocalTime = startTime.plusMinutes(slotInterval)
  
  //Checks if the slot lies within the session's start and end times
  def validateSession: Boolean = {
    val timeOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
    import timeOrdering._
    (this.startTime < this.session.startTime || this.startTime >= this.session.endTime) ||
    (this.endTime <= this.session.startTime || this.endTime > this.session.endTime)
  }
  
  def isValid(): ValidationError = TeacherActivation.get(teacher, session) match {
    case None => ValidationError("The teacher has not activated conferences for this session.")
    case Some(ta) => {
      def startTimeIsBad: Boolean = !ta.allOpenings().contains(startTime)
      def guardianConflicts: Boolean = {
        val guardianAppts = guardians.flatMap(g => Slot.getBySessionAndGuardian(session, g))
        !guardianAppts.exists(s => Slot.timesConflict(s.startTime, s.endTime, this.startTime, this.endTime))
      }
      def teacherConflicts: Boolean = {
        val teacherAppts = ta.appointments
        !teacherAppts.exists(s => Slot.timesConflict(s.startTime, s.endTime, this.startTime, this.endTime))
      }
      if (startTimeIsBad) ValidationError("The start time for this appointment is not valid.")
      else if (guardianConflicts) ValidationError(s"The guardian has an appointment that conflicts with this one.")
      else if (teacherConflicts) ValidationError(s"${teacher.displayName} has an appointment that conflicts with this one.")
      else ValidationError(Nil)
    }
  }
  
  //Checks if the slot overlaps another slot's time period
  def validateSlot: Boolean = {
    val startTime = this.startTime
    val endTime = this.endTime
    dataStore.execute { implicit pm =>
      val cand = QSlot.candidate
	  val slots = pm.query[Slot].filter(cand.teacher.eq(this.teacher).and(cand.session.eq(this.session))).executeList()
      !slots.exists(s => (startTime >= s.startTime && startTime <
      	s.endTime) || (endTime > s.startTime && endTime <= s.endTime))
    }
  }
}

object Slot extends UsesDataStore {
  val dateOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
  import dateOrdering._
  val cand = QSlot.candidate()
  
  def getBySessionAndGuardian(session: Session, guardian: Guardian): List[Slot] = {
    dataStore.execute(pm => {
      pm.query[Slot].filter(cand.session.eq(session).and(cand.guardians.contains(guardian))).orderBy(cand.startTime.asc).executeList()
    })
  }
  
  def timesConflict(start1: LocalTime, end1: LocalTime, start2: LocalTime, end2: LocalTime): Boolean = {
    start1 < end1 && start2 < end2 && (start1 < end2 && start2 < end1)
  }
}

trait QSlot extends PersistableExpression[Slot] {
  import java.util.Set
  
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _session: ObjectExpression[Session] = new ObjectExpressionImpl[Session](this, "_session")
  def session: ObjectExpression[Session] = _session
  
  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl(this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher
  
  private[this] lazy val _startTime: TimeExpression[java.util.Date] = new TimeExpressionImpl[java.sql.Time](this, "_startTime")
  def startTime: TimeExpression[java.util.Date] = _startTime
    
  private[this] lazy val _length: NumericExpression[Int] = new NumericExpressionImpl(this, "_length")
  def slotInterval: NumericExpression[Int] = _length

  private[this] lazy val _students: CollectionExpression[Set[Student], Student] = new CollectionExpressionImpl[Set[Student], Student](this, "_students")
  def students: CollectionExpression[Set[Student], Student] = _students
  
  private[this] lazy val _guardians: CollectionExpression[Set[Guardian], Guardian] = new CollectionExpressionImpl[Set[Guardian], Guardian](this, "_guardians")
  def guardians: CollectionExpression[Set[Guardian], Guardian] = _guardians
  
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