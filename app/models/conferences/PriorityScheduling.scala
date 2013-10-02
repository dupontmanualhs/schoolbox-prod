package models.conferences

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.QueryClass
import models.courses.{ Student, Teacher }
import models.users.DbEquality

@PersistenceCapable(detachable="true")
class PriorityScheduling extends DbEquality[PriorityScheduling] {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Column(allowsNull="false")
  private[this] var _student: Student = _
  def student: Student = _student
  def student_=(theStudent: Student) {_student = theStudent}
  
  @Column(allowsNull="false")
  private[this] var _teacher: Teacher = _
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) {_teacher= theTeacher}
  
  def this(student: Student, teacher: Teacher) = {
    this()
    _student = student
    _teacher = teacher
  }
}

trait QPriorityScheduling extends PersistableExpression[PriorityScheduling] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student
  
  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl[Teacher](this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher
}

object QPriorityScheduling extends QueryClass[PriorityScheduling, QPriorityScheduling] {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QPriorityScheduling = {
    new PersistableExpressionImpl[PriorityScheduling](parent, name) with QPriorityScheduling
  }
  
  def apply(cls: Class[PriorityScheduling], name: String, exprType: ExpressionType): QPriorityScheduling = {
    new PersistableExpressionImpl[PriorityScheduling](cls, name, exprType) with QPriorityScheduling
  }
  
  def myClass = classOf[PriorityScheduling]
}