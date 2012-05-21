package models.quizzes

import javax.jdo.annotations._

@PersistenceCapable(detachable="true")
class Kind {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _

}