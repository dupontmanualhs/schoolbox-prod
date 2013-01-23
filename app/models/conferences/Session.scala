package models.conferences

import javax.jdo.annotations._
import java.sql.Date

@PersistenceCapable(detachable="true")
class Session {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _event : Event = _
  private[this] var _date: java.sql.Date = _
  private[this] var _cutoff: java.sql.Timestamp = _
  private[this] var _priority: java.sql.Timestamp = _
  private[this] var _startTime: java.sql.Time = _
  private[this] var _endTime: java.sql.Time = _
  private[this] var _slotInterval: Int = _
  
  def this(event: Event, date: java.sql.Date, cutoff: java.sql.Timestamp, priority: java.sql.Timestamp, startTime: java.sql.Time, endTime: java.sql.Time, slotInterval: Int) = {
    this()
    _event = event
    _date = date
    _cutoff = cutoff
    _priority = priority
    _startTime = startTime
    _endTime = endTime
    _slotInterval = slotInterval
  }
  
  def event: Event = _event
  def event_=(theEvent: Event) {_event = theEvent}
  
  def date: java.sql.Date = _date
  def date_=(theDate: java.sql.Date) {_date = theDate}
  
  def cutoff: java.sql.Timestamp = _cutoff
  def cutoff_=(theCutoff: java.sql.Timestamp) {_cutoff = theCutoff}
  
  def priority: java.sql.Timestamp = _priority
  def priority_=(thePriority: java.sql.Timestamp) {_priority = thePriority}
  
  def startTime: java.sql.Time = _startTime
  def startTime_=(theStartTime: java.sql.Time) {_startTime = theStartTime}
  
  def endTime: java.sql.Time = _endTime
  def endTime_=(theEndTime: java.sql.Time) {_endTime = theEndTime}
  
  def slotInterval: Int = _slotInterval
  def slotInterval_=(theSlotInterval: Int) {_slotInterval = theSlotInterval}
  
}

//TODO: OB stuff