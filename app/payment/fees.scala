package payment

import models.users._
import models.courses._

class fees {
	private[this] var _cost: Int = _
	//COST IN CENTS -> LATER CONVERTED TO DOLLARS
	private[this] var _teacher: Teacher = _
	private[this] var _course: Course = _
	
	def this(cost: Int, teacher: Teacher, course: Course) = {
	  this()
	  _cost = cost
	  _teacher = teacher
	  _course = course
	}
	
	def cost: Int = _cost
	def cost_(theCost: Int) = {_cost = theCost}
	
	def teacher: Teacher = _teacher
	def teacher_(theTeacher: Teacher) = {_teacher = theTeacher}
	
	def course: Course = _course
	def course_(theCourse: Course) = {_course = theCourse}
	
	private[this] var _cost$: Double = (this.cost/100) + (this.cost%100)/100.0
}