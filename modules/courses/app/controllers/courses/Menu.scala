package controllers.courses

import scala.xml.{ Elem, NodeSeq }
import controllers.users.{ MenuBar, MenuItem }
import models.users.Role
import models.courses.{ Guardian, Teacher, Student}

object Menu {
  val mySchedule = new MenuItem("My Schedule", "menu_myschedule", 
		  		                 Option(controllers.courses.routes.App.mySchedule.toString), Nil)
  val findTeacherSchedule = new MenuItem("Find Teacher Schedule", "menu_teacherschedule",
		  								  Option(controllers.courses.routes.App.findTeacher.toString), Nil)
  val findStudentSchedule = new MenuItem("Find Student Schedule", "menu_studentschedule",
		  								  Option(controllers.courses.routes.App.findStudent.toString), Nil)
  
  def myStudents(guardian: Guardian): List[MenuItem] = {
    val students = guardian.children.filter(_.user.isActive).toList
    if(students.size == 0) List(new MenuItem("You have no current students.", "menu_nostudents", None, Nil))
    else students.map(s => { 
      new MenuItem(s"Schedule for ${s.formalName}", "menu_"+s.formalName, 
          Option(controllers.courses.routes.App.studentScheduleForUsername(s.user.username).toString), Nil)
      })
  }
  
  def forRole(maybeRole: Option[Role]): Option[MenuItem] = {
    val items: Option[List[MenuItem]] = maybeRole.map((role: Role) =>
      role match {
        case t: Teacher => List(mySchedule, findTeacherSchedule, findStudentSchedule)
        case s: Student => List(mySchedule)
        case g: Guardian => myStudents(g)
      }
    )
    items.map(ms => new MenuItem("Courses", "menu_courses", None, ms)) 
  }
}