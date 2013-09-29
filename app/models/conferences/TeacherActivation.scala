package models.conferences

import javax.jdo.annotations._
import models.courses.{ Student, Teacher }
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import util.QueryClass
import config.users.UsesDataStore
import models.users.DbEquality
import org.joda.time.LocalTime

@PersistenceCapable(detachable="true")
class TeacherActivation extends UsesDataStore with DbEquality[TeacherActivation] {
  @Persistent(persistenceModifier=PersistenceModifier.NONE)
  val dateOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
  import dateOrdering._

  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Persistent
  @Column(allowsNull="false")
  private[this] var _session: Session = _
  def session: Session = _session
  def session_=(theSession: Session) {_session = theSession}
  
  @Persistent
  @Column(allowsNull="false")
  private[this] var _teacher: Teacher = _
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) {_teacher = theTeacher}
  
  @Column(allowsNull="false")
  private[this] var _slotInterval: Int = _
  def slotInterval: Int = _slotInterval
  def slotInterval_=(theSlotInterval: Int) {_slotInterval = theSlotInterval}
  
  @Column(allowsNull="true")
  private[this] var _note: String = _
  def note: Option[String] = if (_note == null) None else Some(_note)
  def note_=(theNote: Option[String]) {_note = theNote.getOrElse(null)}
  
  def this(session: Session, teacher: Teacher, slotInterval: Int, note: Option[String]) = {
    this()
    _session = session
    _teacher = teacher
    _slotInterval = slotInterval
    _note = note.getOrElse(null)
  }
  
  def this(session: Session, teacher: Teacher, note: Option[String]) = {
    this(session, teacher, 10, note)
  }
  
  def appointments(): List[Slot] = {
    val slotCand = QSlot.candidate()
    dataStore.pm.query[Slot].filter(slotCand.teacher.eq(this.teacher).and(
        slotCand.session.eq(this.session))).orderBy(slotCand.startTime.asc).executeList()
  }
  
  def allOpenings(): List[Opening] = {
    def openingsFromTo(start: LocalTime, end: LocalTime): List[Opening] = {
      if (start.plusMinutes(this.slotInterval) > end) {
        Nil
      } else {
        Opening(start) :: openingsFromTo(start.plusMinutes(this.slotInterval), end)
      }
    }
    openingsFromTo(this.session.startTime, this.session.endTime)
  }
  
  def scheduleRows(): List[ScheduleRow] = {
    // both must be in ascending order by start time
    def merge(openings: List[Opening], appointments: List[Slot]): List[ScheduleRow] = (openings, appointments) match {
      case (ops, Nil) => ops
      case (Nil, appts) => throw new Exception("Something strange happened. Openings and appointments should coincide.")
      case (op1 :: ops, appt1 :: appts) => {
        if (op1.start == appt1.startTime) Appointment(appt1) :: merge(ops, appts)
        else op1 :: merge(ops, appointments)
      }
    }
    merge(this.allOpenings(), this.appointments())
  }
}

object TeacherActivation extends UsesDataStore {
  val cand = QTeacherActivation.candidate

  def get(teacher: Teacher, session: Session): Option[TeacherActivation] = {
    dataStore.pm.query[TeacherActivation].filter(cand.teacher.eq(teacher).and(cand.session.eq(session))).executeOption()
  }
  
  def getById(id: Long): Option[TeacherActivation] = {
    dataStore.pm.query[TeacherActivation].filter(cand.id.eq(id)).executeOption()
  }
}

sealed abstract class ScheduleRow {
  def startTime(): LocalTime
}
case class Appointment(slot: Slot) extends ScheduleRow {
  def startTime(): LocalTime = this.slot.startTime
}
case class Opening(start: LocalTime) extends ScheduleRow {
  def startTime(): LocalTime = this.start
}

trait QTeacherActivation extends PersistableExpression[TeacherActivation] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _session: ObjectExpression[Session] = new ObjectExpressionImpl[Session](this, "_session")
  def session: ObjectExpression[Session] = _session
  
  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl[Teacher](this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher
  
  private[this] lazy val _note: StringExpression = new StringExpressionImpl(this, "_note")
  def note: StringExpression = _note
}

object QTeacherActivation extends QueryClass[TeacherActivation, QTeacherActivation] {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QTeacherActivation = {
    new PersistableExpressionImpl[TeacherActivation](parent, name) with QTeacherActivation
  }
  
  def apply(cls: Class[TeacherActivation], name: String, exprType: ExpressionType): QTeacherActivation = {
    new PersistableExpressionImpl[TeacherActivation](cls, name, exprType) with QTeacherActivation
  }
  
  def myClass = classOf[TeacherActivation]
}