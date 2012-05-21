package models.quizzes

import javax.jdo.annotations._

@PersistenceCapable(detachable="true")
class QuizTemplate {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _
  
  @Element(types=Array(classOf[Kind]))
  @Join
  private[this] var _questions: java.util.List[Kind] = _
}