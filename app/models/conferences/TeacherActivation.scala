package models.conferences

import javax.jdo.annotations._
import models.users._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import util.QueryClass

@PersistenceCapable(detachable="true")
class TeacherActivation {
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