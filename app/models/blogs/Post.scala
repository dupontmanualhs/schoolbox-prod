package models.blogs

import javax.jdo.annotations._

@PersistenceCapable(detachable="true")
class Post {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  private[this] var _content: String = _

  @Persistent
  private[this] var _published: java.sql.Date = _

  @Persistent
  private[this] var _edited: java.sql.Date = _

  @Column(allowsNull="false")
  private[this] var _title: String = _

  def this(title: String, content: String) = {
    this()
    _title = title
    _content = content
