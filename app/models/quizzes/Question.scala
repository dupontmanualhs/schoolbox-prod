package models.quizzes

import javax.jdo.annotations._

@PersistenceCapable(detachable="true")
class Question {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Column(allowsNull="false")
  private[this] var _text: String = _
  
  @Column(allowsNull="false")
  private[this] var _answer: String = _
  
  @Column(allowsNull="false")
  private[this] var _kind: Kind = _
}