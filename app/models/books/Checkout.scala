package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.Perspective

@PersistenceCapable(detachable="true")
class PurchaseGroup {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _perspective: Perspective = _
  private[this] var _copy: Copy = _
  private[this] var _startDate: java.sql.Date = _
  private[this] var _endDate: java.sql.Date = _

  def this(perspective: Perspective, copy: Copy, startDate: java.sql.Date, endDate: java.sql.Date) = {
    this()
    _perspective = perspective
    _copy = copy
    _startDate = startDate
    _endDate
  }

  def id: Long = _id

  def perspective: Perspective = _perspective
  def perspective_=(thePerspective: Perspective) { _perspective = thePerspective }

  def copy: Copy = _copy
  def copy_=(theCopy: Copy) { _copy = theCopy }

  def startDate: java.sql.Date = _startDate
  def startDate_=(theStartDate: java.sql.Date) { _startDate = theStartDate }

  def endDate: java.sql.Date = _endDate
  def endDate_=(theEndDate: java.sql.Date) { _endDate = theEndDate }
}
