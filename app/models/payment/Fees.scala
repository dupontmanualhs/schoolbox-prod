package models.payment

import models.users._
import java.sql.Date
import javax.jdo.annotations._
import models.courses._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import play.api.mvc.{ RequestHeader, Session }

@PersistenceCapable(detachable = "true")
class Fees {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  private[this] var _cost: Int = _
  def cost: Int = _cost
  def cost_=(theCost: Int) { _cost = theCost }

  @Persistent
  private[this] var _teacher: Teacher = _
  def teacher: Teacher = _teacher
  def teacher_=(theTeacher: Teacher) { _teacher = theTeacher }

  @Persistent
  private[this] var _student: Student = _
  def student: Student = _student
  def student_=(theStudent: Student) { _student = theStudent }

  @Persistent
  private[this] var _course: Course = _
  def course: Course = _course
  def course_=(theCourse: Course) { _course = theCourse }

  @Persistent
  private[this] var _dateAssigned: Date = _
  def dateAssigned: Date = _dateAssigned
  def dateAssigned_=(theDate: Date) { _dateAssigned = theDate }

  @Persistent
  private[this] var _datePaid: Date = _
  def datePaid: Date = _datePaid
  def datePaid_=(theDate: Date) { _datePaid = theDate }

  def this(cost: Int, teacher: Teacher, student: Student, course: Course, dateAss: Date, datePay: Date = null) = {
    this()
    _cost = cost
    _teacher = teacher
    _course = course
    _dateAssigned = dateAss
    _datePaid = datePay
  }
}

trait QFees extends PersistableExpression[Fees] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _cost: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_cost")
  def cost: NumericExpression[Int] = _cost

  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl[Teacher](this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher

  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student

  private[this] lazy val _course: ObjectExpression[Course] = new ObjectExpressionImpl[Course](this, "_course")
  def course: ObjectExpression[Course] = _course

  private[this] lazy val _dateAssigned: DateExpression[java.util.Date] = new DateExpressionImpl[Date](this, "_dateAssigned")
  def dateAssigned: DateExpression[java.util.Date] = _dateAssigned

  private[this] lazy val _datePaid: DateExpression[java.util.Date] = new DateExpressionImpl[Date](this, "_datePay")
  def datePaid: DateExpression[java.util.Date] = _datePaid
}

object QFees {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QFees = {
    new PersistableExpressionImpl[Fees](parent, name) with QFees
  }

  def apply(cls: Class[Fees], name: String, exprType: ExpressionType): QFees = {
    new PersistableExpressionImpl[Fees](cls, name, exprType) with QFees
  }

  private[this] lazy val jdoCandidate: QFees = candidate("this")

  def candidate(name: String): QFees = QFees(null, name, 5)

  def candidate(): QFees = jdoCandidate

  def parameter(name: String): QFees = QFees(classOf[Fees], name, ExpressionType.PARAMETER)

  def variable(name: String): QFees = QFees(classOf[Fees], name, ExpressionType.VARIABLE)
}
