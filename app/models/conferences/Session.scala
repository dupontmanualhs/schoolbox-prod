package models.conferences

import javax.jdo.annotations._
import java.sql.{ Date, Time, Timestamp }
import org.joda.time.{ LocalDate, LocalTime, LocalDateTime }
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.QueryClass
import config.users.UsesDataStore

@PersistenceCapable(detachable="true")
class Session {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Persistent
  @Column(allowsNull="false")
  private[this] var _event : Event = _
  def event: Event = _event
  def event_=(theEvent: Event) {_event = theEvent}
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _date: Date = _
  def date: LocalDate = LocalDate.fromDateFields(_date)
  def date_=(theDate: LocalDate) { _date = new Date(theDate.toDateTimeAtStartOfDay.getMillis) }
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _cutoff: Timestamp = _
  def cutoff: LocalDateTime = LocalDateTime.fromDateFields(_cutoff)
  def cutoff_=(theCutoff: LocalDateTime) { _cutoff = new Timestamp(theCutoff.toDate.getTime) }
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="true")
  private[this] var _priority: java.sql.Timestamp = _
  def priority: Option[LocalDateTime] = if (_priority == null) None else Some(LocalDateTime.fromDateFields(_priority))
  def priority_=(thePriority: Option[LocalDateTime]) { _priority = thePriority.map(p => new Timestamp(p.toDate.getTime)).getOrElse(null) }
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _startTime: java.sql.Time = _
  def startTime: LocalTime = LocalTime.fromDateFields(_startTime)
  def startTime_=(theStartTime: LocalTime) { _startTime = new Time(theStartTime.getMillisOfDay) }
  
  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _endTime: java.sql.Time = _
  def endTime: LocalTime = LocalTime.fromDateFields(_endTime)
  def endTime_=(theEndTime: LocalTime) { _endTime = new Time(theEndTime.getMillisOfDay) }
  
  def this(theEvent: Event, theDate: LocalDate, theCutoff: LocalDateTime, thePriority: Option[LocalDateTime], theStartTime: LocalTime, theEndTime: LocalTime) = {
    this()
    event_=(theEvent)
    date_=(theDate)
    cutoff_=(theCutoff)
    priority_=(thePriority)
    startTime_=(theStartTime)
    endTime_=(theEndTime)
  }
}

object Session extends UsesDataStore {
  def getById(id: Long): Option[Session] = {
    dataStore.execute { pm => 
      val cand = QSession.candidate
      pm.query[Session].filter(cand.id.eq(id)).executeOption
    }
  }
  
  def getById(id: String): Option[Session] = {
    val maybeLong = try { Some(id.toLong) } catch { case e: Exception => None}
    maybeLong match { case Some(l) => getById(id); case None => None}
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