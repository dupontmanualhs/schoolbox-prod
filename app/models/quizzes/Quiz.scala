package models.quizzes

import javax.jdo.annotations._
import models.users.Student

@PersistenceCapable(detachable="true")
class Quiz {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  @Persistent(defaultFetchGroup="true")
  private[this] var _student: Student = _
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _date: org.joda.time.DateTime = _
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _questions: java.util.List[Question] = _
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _answers: java.util.List[String] = _
}