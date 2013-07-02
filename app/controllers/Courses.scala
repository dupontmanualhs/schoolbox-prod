package controllers

import play.api.mvc.{ Action, Controller }
import models.courses._
import models.users._
import util.Helpers.mkNodeSeq
import scala.xml.NodeSeq
import play.api.mvc.PlainResult
import scala.xml.Text
import play.api.mvc.Flash._
import scalajdo.DataStore
import controllers.users.{ Authenticated, VisitAction }

object Courses extends Controller {
  def getMySchedule() = Authenticated { implicit req =>
    val currentUser: User = Visit.getFromRequest(req).user.get
    if (Teacher.getByUsername(currentUser.username).isDefined) {
      teacherSchedule(Teacher.getByUsername(currentUser.username), Some(Term.current))(req)
    } else {
      studentScheduleNoOpts(Student.getByUsername(currentUser.username).get, Term.current)(req)
    }
  }

  def getTeacherSchedule2(username: String, termSlug: String) = {
    teacherSchedule(Teacher.getByUsername(username), Term.getBySlug(termSlug))
  }

  def getTeacherSchedule1(username: String) = {
    teacherSchedule(Teacher.getByUsername(username), Some(Term.current))
  }

  def getStudentSchedule1(username: String) = VisitAction { implicit req =>
    studentSchedule(Student.getByUsername(username), Some(Term.current))(req)
  }

  def getStudentSchedule2(username: String, termSlug: String) = VisitAction { implicit req =>
    studentSchedule(Student.getByUsername(username), Term.getBySlug(termSlug))(req)
  }

  // TODO: only this student, his/her parent, or a teacher should be able to see this schedule
  def studentSchedule(maybeStudent: Option[Student], maybeTerm: Option[Term]) = VisitAction { implicit req =>
    if (!maybeStudent.isDefined) {
      NotFound(templates.NotFound(templates.Main, "No such student."))
    } else if (!maybeTerm.isDefined) {
      NotFound(templates.NotFound(templates.Main, "No such term."))
    } else {
      studentScheduleNoOpts(maybeStudent.get, maybeTerm.get)(req)
    }
  }

  def studentScheduleNoOpts(student: Student, term: Term) = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      val enrollments: List[StudentEnrollment] = {
        val sectVar = QSection.variable("sectVar")
        val cand = QStudentEnrollment.candidate()
        pm.query[StudentEnrollment].filter(cand.student.eq(student).and(cand.section.eq(sectVar)).and(sectVar.terms.contains(term))).executeList()
      }
      val hasEnrollments = enrollments.size != 0
      val sections: List[Section] = enrollments.map(_.section)
      val periods: List[Period] = pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
      val table: List[NodeSeq] = periods.map { p =>
        val sectionsThisPeriod = sections.filter(_.periods.contains(p))
        <tr>
          <td>{ p.name }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => scala.xml.Text(s.course.name)), <br/>) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => scala.xml.Text(s.teachers.map(_.user.shortName).mkString("; "))), <br/>) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => scala.xml.Text(s.room.name)), <br/>) }</td>
        </tr>
      }
      Ok(views.html.courses.studentSchedule(student.user, term, table, hasEnrollments))
    }
  }

  def teacherSchedule(maybeTeacher: Option[Teacher], maybeTerm: Option[Term]) = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      if (!maybeTeacher.isDefined) {
        NotFound(templates.NotFound(templates.Main, "No such teacher."))
      } else if (!maybeTerm.isDefined) {
        NotFound(templates.NotFound(templates.Main, "No such term."))
      } else {
        val teacher = maybeTeacher.get
        val term = maybeTerm.get
        val assignments: List[TeacherAssignment] = {
          val sectVar = QSection.variable("sectVar")
          val cand = QTeacherAssignment.candidate
          pm.query[TeacherAssignment].filter(cand.teacher.eq(teacher).and(cand.section.eq(sectVar)).and(sectVar.terms.contains(term))).executeList()
        }
        val hasAssignments = assignments.size != 0
        val sections: List[Section] = assignments.map(_.section)
        // TODO: do we need to have Periods be attached to a Term?
        val periods: List[Period] = pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
        val table: List[NodeSeq] = periods.map { p =>
          val sectionsThisPeriod = sections.filter(_.periods.contains(p))
          <tr>
            <td>{ p.name }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => linkToPage(s)), <br/>) }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => scala.xml.Text(s.room.name)), <br/>) }</td>
          </tr>
        }
        Ok(views.html.courses.teacherSchedule(teacher.user, term, table, hasAssignments))
      }
    }
  }

  def linkToPage(section: Section): NodeSeq = {
    val link = controllers.routes.Grades.home(section.id)
    <a href={ link.url }>{ section.course.name }</a>
  }

  def linkToRoster(section: Section): NodeSeq = {
    val link = controllers.routes.Courses.roster(section.id)
    <a href={ link.url }>{ section.course.name }</a>
  }

  // only the teacher of this section or an admin should be able to see the roster
  def roster(sectionId: Long) = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      val cand = QSection.candidate
      pm.query[Section].filter(cand.id.eq(sectionId)).executeOption() match {
        case None => NotFound(templates.NotFound(templates.Main, "No section with that id."))
        case Some(sect) => {
          Ok(views.html.courses.roster(sectionId, sect, sect.enrollments))
        }
      }
    }
  }

  def sectionTable(courseId: Long) = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      val cand = QCourse.candidate
      val cand2 = QSection.candidate
      val course = pm.query[Course].filter(cand.id.eq(courseId)).executeList.head
      val sections = pm.query[Section].filter(cand2.course.eq(course)).executeList
      Ok(views.html.courses.sections(course.name, sections))
    }
  }

  def classList() = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      val cand = QCourse.candidate
      val courses = pm.query[Course].orderBy(cand.name.asc).executeList()
      Ok(views.html.courses.classes(courses))
    }
  }
}
