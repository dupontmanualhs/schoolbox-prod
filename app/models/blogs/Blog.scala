package models.blogs

import javax.jdo.annotations._

@PersistenceCapable(detachable="true")
@Unique(members=@Array(_owner,_title))
@Unique(members=@Array(_owner,_slug))
class Blog {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  @Column(allowsNull="false")
  private[this] var _slug: String = _

  @Column(allowsNull="false")
  private[this] var _title: String = _

  @Column(allowsNull="false")
  private[this] var _owner: User = _

  def this(slug: String, title: String, owner: User) = {
    this()
    _slug = slug
    _title = title
    _owner = owner
  }

  def id: Long = _id

  def title: String = _title
  def title_=(theTitle: String) { _title = theTitle }

  def slug: String = _slug
  def slug_=(theSlug: string) { _slug = theSlug }

  def owner: User = _owner
}
