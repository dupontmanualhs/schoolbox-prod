package models.payment

import models.users._
import java.sql.Date
import javax.jdo.annotations._
import models.courses._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import play.api.mvc.{RequestHeader, Session}

@PersistenceCapable(detachable="true")
class Payment {
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
	private[this] var _id: Long = _
	
	private[this] var _cost: Int = _
	//COST IN CENTS -> LATER CONVERTED TO DOLLARS
	
	private[this] var _teacher: Teacher = _
	
	private[this] var _student: Student = _
	
	private[this] var _course: Course = _
	
	private[this] var _datePaid: Date = _
	
	def this(cost: Int, teacher: Teacher, student: Student, course: Course, datePay: Date = null) = {
	  this()
	  _cost = cost
	  _teacher = teacher
	  _course = course
	  _datePaid = datePay
	}
	
	def cost: Int = _cost
	def cost_(theCost: Int) = {_cost = theCost}
	
	def teacher: Teacher = _teacher
	def teacher_(theTeacher: Teacher) = {_teacher = theTeacher}
	
	def student: Student = _student
	def studentr_(theStudent: Student) = {_student = theStudent}
	
	def course: Course = _course
	def course_(theCourse: Course) = {_course = theCourse}
	
	def datePay: Date = _datePaid
	def datePay_(theDate: Date) = {_datePaid = theDate}
	
	def id: Long = _id
	
	private[this] var _cost$: Double = (this.cost/100) + (this.cost%100)/100.0
}

trait QPayment extends PersistableExpression[Payment] {
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
  
  private[this] lazy val _datePay: ObjectExpression[Date] = new ObjectExpressionImpl[Date](this, "_datePay")
  def datePay: ObjectExpression[Date] = _datePay
}

object QPayment {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QPayment = {
    new PersistableExpressionImpl[Payment](parent, name) with QPayment
  }
  
  def apply(cls: Class[Payment], name: String, exprType: ExpressionType): QPayment = {
    new PersistableExpressionImpl[Payment](cls, name, exprType) with QPayment
  }
  
  private[this] lazy val jdoCandidate: QPayment = candidate("this")
  
  def candidate(name: String): QPayment = QPayment(null, name, 5)
  
  def candidate(): QPayment = jdoCandidate
  
  def parameter(name: String): QPayment = QPayment(classOf[Payment], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QPayment = QPayment(classOf[Payment], name, ExpressionType.VARIABLE)
}