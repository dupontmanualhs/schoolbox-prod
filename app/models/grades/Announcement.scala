package models.grades

import java.sql.Date
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile
import util.DataStore
import models.courses.Section
import models.users.User

class Announcement {

  private[this] var _id: Long = _
  private[this] var _section: Section = _
  @Persistent(defaultFetchGroup="true")
  private[this] var _date: java.sql.Timestamp = _
  private[this] var _text: String = _
  private[this] var _author: User = _
  //TODO: private[this] var _attachments: java.util.List[String] = _
  //TODO: make this be scala and convert correctly
  
  def this(text: String, date: java.sql.Timestamp, section: Section, author: User/*, attachments: Option[java.util.List[String]]*/) {
    this()
    _text = text
    _section = section
    _date = date
    _author = author
//  _attachments = TODO
  }
  
  def id: Long = _id
  
  def date: java.sql.Timestamp = _date
  def date_=(theDate: java.sql.Timestamp) { _date = theDate }
  
  def text: String = _text
  def text_=(theText: String) { _text = theText }
  
  def section: Section = _section
  def section_=(theSection: Section) { _section = theSection }
  
  def author: User = _author
  def author_=(theAuthor: User) { _author = theAuthor}
  
}

trait QAnnouncement extends PersistableExpression[Announcement] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _text: StringExpression = new StringExpressionImpl(this, "_text")
  def text: StringExpression = _text
  
  private[this] lazy val _date: ObjectExpression[java.sql.Timestamp] = new ObjectExpressionImpl[java.sql.Timestamp](this, "_date")
  def date: ObjectExpression[java.sql.Timestamp] = _date
  
  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section
  
  private[this] lazy val _author: ObjectExpression[User] = new ObjectExpressionImpl[User](this, "_author")
  def author: ObjectExpression[User] = _author
  
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
