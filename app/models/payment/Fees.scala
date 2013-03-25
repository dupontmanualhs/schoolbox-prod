package models.payment

import models.users._
import java.sql.Date
import javax.jdo.annotations._
import models.courses._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.DataStore
import play.api.mvc.{RequestHeader, Session}
import util.ScalaPersistenceManager
import util.DbRequest

@PersistenceCapable(detachable="true")
class fees {
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
	private[this] var _id: Long = _
	
	private[this] var _cost: Int = _
	//COST IN CENTS -> LATER CONVERTED TO DOLLARS
	
	private[this] var _teacher: Teacher = _
	
	private[this] var _course: Course = _
	
	private[this] var _dateAssigned: Date = _
	
	private[this] var _datePayed: Date = _
	
	def this(cost: Int, teacher: Teacher, course: Course, dateAss: Date, datePay: Date = null) = {
	  this()
	  _cost = cost
	  _teacher = teacher
	  _course = course
	  _dateAssigned = dateAss
	  _datePayed = datePay
	}
	
	def cost: Int = _cost
	def cost_(theCost: Int) = {_cost = theCost}
	
	def teacher: Teacher = _teacher
	def teacher_(theTeacher: Teacher) = {_teacher = theTeacher}
	
	def course: Course = _course
	def course_(theCourse: Course) = {_course = theCourse}
	
	def dateAss: Date = _dateAssigned
	def dateAss_(theDate: Date) = {_dateAssigned = theDate}
	
	def datePay: Date = _datePayed
	def datePay_(theDate: Date) = {_datePayed = theDate}
	
	def id: Long = _id
	
	private[this] var _cost$: Double = (this.cost/100) + (this.cost%100)/100.0
}

trait QFees extends PersistableExpression[fees] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _cost: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_cost")
  def cost: NumericExpression[Int] = _cost
  
  private[this] lazy val _teacher: ObjectExpression[Teacher] = new ObjectExpressionImpl[Teacher](this, "_teacher")
  def teacher: ObjectExpression[Teacher] = _teacher
  
  private[this] lazy val _course: ObjectExpression[Course] = new ObjectExpressionImpl[Course](this, "_course")
  def course: ObjectExpression[Course] = _course
  
  private[this] lazy val _dateAss: ObjectExpression[Date] = new ObjectExpressionImpl[Date](this, "_dateAss")
  def dateAss: ObjectExpression[Date] = _dateAss
  
  private[this] lazy val _datePay: ObjectExpression[Date] = new ObjectExpressionImpl[Date](this, "_datePay")
  def datePay: ObjectExpression[Date] = _datePay
}