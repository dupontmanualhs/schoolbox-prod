package models.blogs

import javax.jdo.annotations._

@PersistenceCapable(detachable="true")
class Category {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  @Column(allowsNull="false")
  private[this] var _title: String = _

  private[this] var _posts: List[Post] = _

  def this(title: String) {
    this()
    _title = title
  }
}
