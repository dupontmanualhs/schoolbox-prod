package models.grades

import java.sql.Date
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import scalajdo.DataStore
import util.PersistableFile
import models.courses.Section
import models.users.Role
import org.joda.time.LocalDateTime

@PersistenceCapable(detachable="true")
class Announcement {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _author: Role = _
  def author: Role = _author
  def author_=(theAuthor: Role) { _author = theAuthor}
    
  // TODO: should this be a Set[Section]
  @Persistent(defaultFetchGroup="true")
  private[this] var _section: Section = _
  def section: Section = _section
  def section_=(theSection: Section) { _section = theSection }
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _timestamp: java.sql.Timestamp = _
  def timestamp: LocalDateTime = LocalDateTime.fromDateFields(_timestamp)
  def timestamp_=(theTimestamp: LocalDateTime) { _timestamp = new java.sql.Timestamp(theTimestamp.toDate.getTime) }
  
  @Column(jdbcType="CLOB")
  private[this] var _text: String = _
  def text: String = _text
  def text_=(theText: String) { _text = theText }
  
  // TODO: attachments?
    
  def this(theAuthor: Role, theSection: Section, theTimestamp: LocalDateTime, theText: String) {
    this()
    author_=(theAuthor)
    section_=(theSection)
    timestamp_=(theTimestamp)
    text_=(theText)    
  }  
}

trait QAnnouncement extends PersistableExpression[Announcement] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _author: ObjectExpression[Role] = new ObjectExpressionImpl[Role](this, "_author")
  def author: ObjectExpression[Role] = _author

  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section  

  private[this] lazy val _timestamp: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Timestamp](this, "_timestamp")
  def date: DateExpression[java.util.Date] = _timestamp
  
  private[this] lazy val _text: StringExpression = new StringExpressionImpl(this, "_text")
  def text: StringExpression = _text
}

object Announcement {
  def getAnnouncements(section: Section): List[Announcement] = {
      val cand = QAnnouncement.candidate
      DataStore.pm.query[Announcement].filter(cand.section.eq(section)).executeList
  }
}

object QAnnouncement {
  def apply(parent: PersistableExpression[Announcement], name: String, depth: Int): QAnnouncement = {
    new PersistableExpressionImpl[Announcement](parent, name) with QAnnouncement
  }
  
  def apply(cls: Class[Announcement], name: String, exprType: ExpressionType): QAnnouncement = {
    new PersistableExpressionImpl[Announcement](cls, name, exprType) with QAnnouncement
  }
  
  private[this] lazy val jdoCandidate: QAnnouncement = candidate("this")
  
  def candidate(name: String): QAnnouncement = QAnnouncement(null, name, 5)
  
  def candidate(): QAnnouncement = jdoCandidate
  
  def parameter(name: String): QAnnouncement = QAnnouncement(classOf[Announcement], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QAnnouncement = QAnnouncement(classOf[Announcement], name, ExpressionType.VARIABLE)
}
