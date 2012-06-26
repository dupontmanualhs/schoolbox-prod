package controllers

import play.api.mvc.Controller
import models.courses._
import models.users._
import util.DbAction
import util.Helpers.mkNodeSeq
import views.html
import scala.xml.NodeSeq
import play.api.mvc.Result
import util.DbRequest
import util.ScalaPersistenceManager
import scala.xml.Text

object Courses extends Controller {
  def getMySchedule() = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
  	val currentUser: Option[User] = User.current
    if(currentUser.isDefined) {
      if (Teacher.getByUsername(currentUser.get.username)(pm).isDefined) {
        teacherSchedule(Teacher.getByUsername(currentUser.get.username)(pm), Some(Term.current(pm)))
      } else {
        studentSchedule(Student.getByUsername(currentUser.get.username)(pm).get, Term.current(pm))
      }
    } else {
      NotFound("You are not logged in.")
    }
  }
  
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

  // only this student, his/her parent, or a teacher should be able to see this schedule
  def studentSchedule(maybeStudent: Option[Student], maybeTerm: Option[Term])(implicit req: DbRequest[_]): Result = {
    implicit val pm = req.pm
    if (!maybeStudent.isDefined) {
      NotFound("No such student.")
    } else if (!maybeTerm.isDefined) {
      NotFound("No such term.")
    } else {
      studentSchedule(maybeStudent.get, maybeTerm.get)
    }
  }

  def studentSchedule(student: Student, term: Term)(implicit req: DbRequest[_]): Result = {
    implicit val pm = req.pm
    val enrollments: List[StudentEnrollment] = {
      val cand = QStudentEnrollment.candidate()
      pm.query[StudentEnrollment].filter(cand.student.eq(student).and(cand.term.eq(term))).executeList()
    }
    val sections: List[Section] = enrollments.map(_.section)
    val periods: List[Period] = pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
    val table: List[NodeSeq] = periods.map { p =>
      val sectionsThisPeriod = sections.filter(_.periods.contains(p))
      <tr>
        <td>{ p.name }</td>
        <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.course.name)), <br/>) }</td>
        <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.teachers.map(_.user.formalName).mkString("; "))), <br/>) }</td>
        <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.room.name)), <br/>) }</td>
      </tr>
    }
    Ok(html.courses.studentSchedule(student.user, term, table))
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
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => linkToRoster(s)), <br/>) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.room.name)), <br/>) }</td>
        </tr>
      }
      Ok(html.courses.teacherSchedule(teacher.user, term, table))
    }
  }

  def linkToRoster(section: Section): NodeSeq = {
    val link = controllers.routes.Courses.roster(section.id)
    <a href={ link.url }>{ section.course.name }</a>
  }

  // only the teacher of this section or an admin should be able to see the roster
  def roster(sectionId: Long) = DbAction { implicit req =>
    implicit val pm = req.pm
    val cand = QSection.candidate
    pm.query[Section].filter(cand.id.eq(sectionId)).executeOption() match {
      case None => NotFound("No section with that id.")
      case Some(sect) => {
        val course = sect.course.name
        val terms = sect.terms.toList.map(_.name).mkString(", ")
        val periods = sect.periods.toList.map(_.name).mkString(", ")
        val teachers = sect.teachers.sortWith(_ < _).map(_.displayName).mkString(", ")
        val enrollments = sect.enrollments.sortWith(_.student < _.student)
        Ok(html.courses.roster(course, terms, periods, teachers, enrollments))
      }
    }
  }
  
  def sectionTable(courseId: Long) = DbAction { implicit req =>
    val pm = req.pm
    val cand = QCourse.candidate
    val cand2 = QSection.candidate
    val course = pm.query[Course].filter(cand.id.eq(courseId)).executeList.head
    val sections = pm.query[Section].filter(cand2.course.eq(course)).executeList
    Ok(views.html.courses.sections(course.name, sections))
  }
  
  
  def classList() = DbAction { implicit req =>
    val pm = req.pm
    val cand = QCourse.candidate
    val courses = pm.query[Course].orderBy(cand.name.asc).executeList()
    Ok(views.html.courses.classes(courses))
  }
  

}
