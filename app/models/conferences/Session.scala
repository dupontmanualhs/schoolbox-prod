package models.conferences

import javax.jdo.annotations._
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.QueryClass

@PersistenceCapable(detachable="true")
class Session {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Column(allowsNull="false")
  private[this] var _event : Event = _
  def event: Event = _event
  def event_=(theEvent: Event) {_event = theEvent}
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _date: java.sql.Date = _
  def date: java.sql.Date = _date
  def date_=(theDate: java.sql.Date) { _date = theDate }
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _cutoff: java.sql.Timestamp = _
  def cutoff: java.sql.Timestamp = _cutoff
  def cutoff_=(theCutoff: java.sql.Timestamp) {_cutoff = theCutoff}
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="true")
  private[this] var _priority: java.sql.Timestamp = _
  def priority: Option[java.sql.Timestamp] = if (_priority == null) None else Some(_priority)
  def priority_=(thePriority: Option[java.sql.Timestamp]) {_priority = thePriority.getOrElse(null)}
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _startTime: java.sql.Time = _
  def startTime: java.sql.Time = _startTime
  def startTime_=(theStartTime: java.sql.Time) {_startTime = theStartTime}
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _endTime: java.sql.Time = _
  def endTime: java.sql.Time = _endTime
  def endTime_=(theEndTime: java.sql.Time) {_endTime = theEndTime}
  
  def this(event: Event, date: java.sql.Date, cutoff: java.sql.Timestamp, priority: Option[java.sql.Timestamp], startTime: java.sql.Time, endTime: java.sql.Time) = {
    this()
    _event = event
    _date = date
    _cutoff = cutoff
    _priority = priority.getOrElse(null)
    _startTime = startTime
    _endTime = endTime
  }
}

trait QSession extends PersistableExpression[Session] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _event: ObjectExpression[Event] = new ObjectExpressionImpl[Event](this, "_event")
  def event: ObjectExpression[Event] = _event
  
  private[this] lazy val _date: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_date")
  def date: DateExpression[java.util.Date] = _date
  
  private[this] lazy val _cutoff: DateTimeExpression[java.util.Date] = new DateTimeExpressionImpl(this, "_cutoff")
  def cutoff: DateTimeExpression[java.util.Date] = _cutoff
  
  private[this] lazy val _priority: DateTimeExpression[java.util.Date] = new DateTimeExpressionImpl(this, "_priority")
  def priority: DateTimeExpression[java.util.Date] = _priority
  
  private[this] lazy val _startTime: TimeExpression[java.util.Date] = new TimeExpressionImpl(this, "_startTime")
  def startTime: TimeExpression[java.util.Date] = _startTime

  private[this] lazy val _endTime: TimeExpression[java.util.Date] = new TimeExpressionImpl(this, "_endTime")
  def endTime: TimeExpression[java.util.Date] = _endTime

  private[this] lazy val _slotInterval: NumericExpression[Int] = new NumericExpressionImpl(this, "_slotInterval")
  def slotInterval: NumericExpression[Int] = _slotInterval
}

object QSession extends QueryClass[Session, QSession] {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QSession = {
    new PersistableExpressionImpl[Session](parent, name) with QSession
  }
  
  def apply(cls: Class[Session], name: String, exprType: ExpressionType): QSession = {
    new PersistableExpressionImpl[Session](cls, name, exprType) with QSession
  }
  
  def myClass = classOf[Session]
}