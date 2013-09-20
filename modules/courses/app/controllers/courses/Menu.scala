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
  
  def myStudents(guardian: Guardian) = {
    val students = guardian.children.toList
    val sublist: List[MenuItem] = if(students.size == 0) List(new MenuItem("You have no students.", "menu_nostudents", None, Nil))
                  else students.map(s => { 
                    new MenuItem(s.formalName, "menu_"+s.formalName, 
                        Option(controllers.courses.routes.App.studentScheduleForUsername(s.user.username).toString), Nil)
                    })
    new MenuItem("My Students' Schedules", "menu_mystudents", None, Nil, sublist)
  }
  
  def forRole(maybeRole: Option[Role]) = {
    val items = { maybeRole match {
        case Some(t: Teacher) => List(mySchedule, findTeacherSchedule, findStudentSchedule)
        case Some(s: Student) => List(mySchedule, findTeacherSchedule)
        case Some(g: Guardian) => List(myStudents(g), findTeacherSchedule)
        case _ => Nil
      }
    }
    new MenuItem("Courses", "menu_courses", None, items) 
  }
}