package controllers

import play.api.mvc.Controller
import models.courses._
import models.users.{Student,Teacher}
import util.DbAction
import util.Helpers.mkNodeSeq
import views.html
import scala.xml.NodeSeq
import play.api.mvc.Result
import util.DbRequest
import util.ScalaPersistenceManager


object Courses extends Controller {  
  def getTeacherSchedule2(username: String, termSlug: String) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    teacherSchedule(Teacher.getByUsername(username), Term.getBySlug(termSlug))
  }
  
  def getTeacherSchedule1(username: String) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    teacherSchedule(Teacher.getByUsername(username), Some(Term.current))
  }
  
  def getStudentSchedule1(username: String) = DbAction { implicit req => 
    implicit val pm: ScalaPersistenceManager = req.pm
    studentSchedule(Student.getByUsername(username), Some(Term.current))
  }
  
  def getStudentSchedule2(username: String, termSlug: String) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    studentSchedule(Student.getByUsername(username), Term.getBySlug(termSlug))
  }
  
  def studentSchedule(maybeStudent: Option[Student], maybeTerm: Option[Term])(implicit req: DbRequest[_]): Result = {
    implicit val pm = req.pm
    if (!maybeStudent.isDefined) {
      NotFound("No such student.")
    } else if (!maybeTerm.isDefined) {
      NotFound("No such term.")
    } else {
      val student = maybeStudent.get
      val term = maybeTerm.get
      val enrollments: List[StudentEnrollment] = {
        val cand = QStudentEnrollment.candidate()
        req.pm.query[StudentEnrollment].filter(cand.student.eq(student).and(cand.term.eq(term))).executeList()
      }
      val sections: List[Section] = enrollments.map(_.section)
	  val periods: List[Period] = req.pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
	  val table: List[NodeSeq] = periods.map { p =>
	    val sectionsThisPeriod = sections.filter(_.periods.contains(p))
	    <tr>
          <td>{ p.name }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(_.course.name), <br/>) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(_.teachers.map(_.user.formalName).mkString("; ")), <br/>) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(_.room.name), <br/>) }</td>
        </tr>
	  }
	  Ok(html.courses.studentSchedule(student.user, term, table))
    }
  }
  
  def teacherSchedule(maybeTeacher: Option[Teacher], maybeTerm: Option[Term])(implicit req: DbRequest[_]): Result = {
    if (!maybeTeacher.isDefined) {
      NotFound("No such teacher.")
    } else if (!maybeTerm.isDefined) {
      NotFound("No such term.")
    } else {
      val teacher = maybeTeacher.get
      val term = maybeTerm.get
	  val assignments: List[TeacherAssignment] = {
	    val cand = QTeacherAssignment.candidate
	    req.pm.query[TeacherAssignment].filter(cand.teacher.eq(teacher).and(cand.term.eq(term))).executeList()
	  }
	  val sections: List[Section] = assignments.map(_.section)
	  // TODO: do we need to have Periods be attached to a Term?
	  val periods: List[Period] = req.pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
	  val table: List[NodeSeq] = periods.map { p =>
	    val sectionsThisPeriod = sections.filter(_.periods.contains(p))
	    <tr>
          <td>{ p.name }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(_.course.name), <br/>) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(_.room.name), <br/>) }</td>
        </tr>
	  }
	  Ok(html.courses.teacherSchedule(teacher.user, term, table))
    }
  }

}