package models.blogs

import javax.jdo.annotations._
import javax.jdo.listener.StoreCallback
import org.joda.time.LocalDateTime
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class Post extends StoreCallback {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  private[this] var _content: String = _

  @Persistent
  private[this] var _published: java.sql.Timestamp = _

  @Persistent
  private[this] var _edited: java.sql.Timestamp = _

  @Column(allowsNull="false")
  private[this] var _title: String = _

  private[this] var _blog: Blog = _
  
  def id: Long = _id
  
  def content: String = _content
  def content_=(theContent: String) { _content = theContent }
  
  def published: LocalDateTime = new LocalDateTime(_published.getTime)
  def published_=(thePublished: LocalDateTime) { _published = new java.sql.Timestamp(thePublished.toDate.getTime()) }

  def edited: LocalDateTime = new LocalDateTime(_edited.getTime)
  def edited_=(theEdited: LocalDateTime) { _edited = new java.sql.Timestamp(theEdited.toDate.getTime) }
  
  def title: String = _title
  def title_=(theTitle: String) { _title = theTitle }
  
  def blog: Blog = _blog
  def blog_=(theBlog: Blog) { _blog = theBlog }
  
  def this(title: String, content: String, blog: Blog) = {
    this()
    _title = title
    _content = content
    _blog = blog
  }
  
  def jdoPreStore() {
    // TODO: if it's not in the db yet, set published to now
    // TODO: if it's dirty, set edited to now
  }
}

trait QPost extends PersistableExpression[Post] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  // content
  private[this] lazy val _content: StringExpression = new StringExpressionImpl(this, "_content")
  def content: StringExpression = _content
  
  // published
  private[this] lazy val published: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_published")
  def published: DateExpression[java.util.Date] = _published
  
  // edited
  private[this] lazy val _edited: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_edited")
  def edited: DateExpression[java.util.Date] = _edited
  
  // title
  private[this] lazy val _title: StringExpression = new StringExpressionImpl(this, "_title")
  def title: StringExpression = _title
  
  // blog
  private[this] lazy val _blog: ObjectExpression[Blog] = new ObjectExpressionImpl[Blog](this, "_blog")
  def blog: ObjectExpression[Blog] = _blog
}

object QPost {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QPost = {
    new PersistableExpressionImpl[Post](parent, name) with QPost
  }
  
  def apply(cls: Class[Post], name: String, exprType: ExpressionType): QPost = {
    new PersistableExpressionImpl[Post](cls, name, exprType) with QPost
  }
  
  private[this] lazy val jdoCandidate: QPost = candidate("this")
  
  def candidate(name: String): QPost = QPost(null, name, 5)
  
  def candidate: QPost = jdoCandidate
  
  def parameter(name: String): QPost = QPost(classOf[Post], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QPost = QPost(classOf[Post], name, ExpressionType.VARIABLE)
}
