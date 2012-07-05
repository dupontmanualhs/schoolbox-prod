package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.Perspective

@PersistenceCapable(detachable="true")
class LabelQueueSet {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _perspective: Perspective = _
  private[this] var _title: Title = _
  private[this] var _copyRange: String = _

  def this(perspective: Perspective, title: Title, copyRange: String) = {
    this()
    _perspective = perspective
    _title = title
    _copyRange = copyRange
  }

  def id: Long = _id

  def perspective: Perspective = _perspective
  def perspective_=(thePerspective: Perspective) { _perspective = thePerspective }

  def title: Title = _title
  def title_=(theTitle: Title) { _title = theTitle }

  def copyRange: String = _copyRange
  def copyRange_=(theCopyRange: String) { _copyRange = copyRange }
}
