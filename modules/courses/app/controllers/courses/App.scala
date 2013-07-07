package controllers.courses

import scalatags._
import play.api.mvc.Controller
import models.courses._
import models.users._
import scala.xml.NodeSeq
import play.api.mvc.Flash._
import scalajdo.DataStore
import controllers.users.{Authenticated, VisitAction, VisitRequest}
import com.google.inject.{ Inject, Singleton }
import config.users.Config
import controllers.users.MustPassTest
import javax.jdo.annotations.Inheritance
import javax.jdo.annotations.PersistenceCapable
import scalatags.stringToNodeable.apply
import controllers.users.AuthenticatedRequest

@Singleton
class App @Inject()(implicit config: Config) extends Controller {
  // functions that return Results given the right input
  // these are not Actions, and should be called by an Action once the right info has been found
  private[this] def roleSchedule(maybeRole: Option[Role], maybeTerm: Option[Term])(implicit req: VisitRequest[_]) = {
    maybeTerm match {
      case None => NotFound(config.notFound("You are looking for a schedule in a term that does'nt exist."))
      case Some(term) => maybeRole match {
        case None => NotFound(config.notFound("There is no schedule for that user."))
        case Some(role) => role match {
          case teacher: Teacher => teacherSchedule(teacher, term)
          case student: Student => studentSchedule(student, term)
          case _ => NotFound(config.notFound("Only teachers and students have schedules."))
        }
      }
    }
  }
  
  private[this] def teacherSchedule(teacher: Teacher, term: Term)(implicit req: VisitRequest[_]) = {
    DataStore.execute { pm => 
      val assignments: List[TeacherAssignment] = {
        val sectVar = QSection.variable("sectVar")
        val cand = QTeacherAssignment.candidate
        pm.query[TeacherAssignment].filter(cand.teacher.eq(teacher).and(cand.section.eq(sectVar)).and(sectVar.terms.contains(term))).executeList()
      }
      val hasAssignments = assignments.size != 0
      val sections: List[Section] = assignments.map(_.section)
      // TODO: do we need to have Periods be attached to a Term?
      val periods: List[Period] = pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
      val table = periods.map { p =>
        val sectionsThisPeriod = sections.filter(_.periods.contains(p))
        tr(td(p.name),
           td(intersperse(sectionsThisPeriod.map(s => linkToPage(s)), br())),
           td(intersperse(sectionsThisPeriod.map(s => StringSTag(s.room.name)), br())))
      }
      Ok(templates.courses.TeacherSchedule(teacher, term, table, hasAssignments))
    }
  }

  def studentSchedule(student: Student, term: Term)(implicit req: VisitRequest[_]) = {
    DataStore.execute { implicit pm =>
      val enrollments: List[StudentEnrollment] = {
        val sectVar = QSection.variable("sectVar")
        val cand = QStudentEnrollment.candidate()
        pm.query[StudentEnrollment].filter(cand.student.eq(student).and(cand.section.eq(sectVar)).and(sectVar.terms.contains(term))).executeList()
      }
      val hasEnrollments = enrollments.size != 0
      val sections: List[Section] = enrollments.map(_.section)
      val periods: List[Period] = pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
      val rows: List[STag] = periods.map { p =>
        val sectionsThisPeriod = sections.filter(_.periods.contains(p))
        tr(
          td(p.name),
          td(intersperse(sectionsThisPeriod.map(s => StringSTag(s.course.name)), br())),
          td(intersperse(sectionsThisPeriod.map(s => StringSTag(s.teachers.map(_.shortName).mkString("; "))), br())),
          td(intersperse(sectionsThisPeriod.map(s => StringSTag(s.room.name)), br())))
      }
      Ok(templates.courses.StudentSchedule(student, term, rows, hasEnrollments))
    }
  }
  
  // Actions - should be the result of routing and should call the functions above
  def mySchedule() = Authenticated { implicit req =>
    roleSchedule(Some(req.role), Some(Term.current))
  }
  
  def myScheduleForTerm(termSlug: String) = Authenticated { implicit req =>
    roleSchedule(Some(req.role), Term.getBySlug(termSlug))  
  }
  
  // must be logged in, but any user should be able to see a teacher's schedule
  def teacherScheduleForUsername(username: String) = Authenticated { implicit req =>
    roleSchedule(Teacher.getByUsername(username), Some(Term.current))
  }
  
  def teacherScheduleForUsernameAndTerm(username: String, termSlug: String) = Authenticated { implicit req => 
    roleSchedule(Teacher.getByUsername(username), Term.getBySlug(termSlug))
  }
  
  def studentScheduleForUsername(username: String) = StudentCheck.canViewSchedule(username) { 
    implicit req => roleSchedule(Some(req.student), Some(Term.current))  
  }
  
  // TODO: permission for student schedules
  def studentScheduleForUsernameAndTerm(username: String, termSlug: String) = StudentCheck.canViewSchedule(username) { 
    implicit req => roleSchedule(Some(req.student), Term.getBySlug(termSlug))  
  }
  
  private[this] def roleTeachesSection[A](sectionId: String): MustPassTest.Test[A] = {
    implicit req: AuthenticatedRequest[A] => Section.getBySectionId(sectionId).map(_.teachers.contains(req.role)).getOrElse(false)
  }
  
  // only the teacher of this section or an admin should be able to see the roster
  def roster(sectionId: String) = MustPassTest(roleTeachesSection(sectionId)) { implicit req =>
    val cand = QSection.candidate
    DataStore.pm.query[Section].filter(cand.sectionId.eq(sectionId)).executeOption() match {
      case None => NotFound(config.notFound("No section with that id."))
      case Some(sect) => {
        Ok(templates.courses.Roster(sect))
      }
    }
  }

  def linkToPage(section: Section) = {
    //val link = controllers.routes.Grades.home(section.id)
    a.href("#")(section.course.name)
  }

  /*def linkToRoster(section: Section): NodeSeq = {
    val link = controllers.routes.Courses.roster(section.id)
    <a href={ link.url }>{ section.course.name }</a>
  }*/

  //TODO: permissions
  def sectionList(masterNumber: String) = VisitAction { implicit req =>
    Course.getByMasterNumber(masterNumber) match {
      case None => NotFound(config.notFound(s"There is no course with the master number '${masterNumber}'."))
      case Some(course) => {
        val cand = QSection.candidate
        val sections = DataStore.pm.query[Section].filter(cand.course.eq(course)).executeList()
        Ok(templates.courses.SectionList(course, sections))
      }
    }
  }
  
  //TODO: permissions
  def sectionMasterList() = VisitAction { implicit req =>
    Ok(templates.courses.MasterList(DataStore.pm.query[Section].executeList()))  
  }

  //TODO: permissions
  def classList() = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      val cand = QCourse.candidate
      val courses = pm.query[Course].orderBy(cand.name.asc).executeList()
      Ok(templates.courses.ListAll(courses))
    }
  }
  
  //TODO: permissions
  def classListForTerm(termSlug: String) = VisitAction { implicit req =>
    Term.getBySlug(termSlug) match {
      case None => NotFound(config.notFound(s"There is no term '${termSlug}'."))
      case Some(term) => {
        val cand = QCourse.candidate
        val sectVar = QSection.variable("sect")
        val courses = DataStore.pm.query[Course].filter(sectVar.course.eq(cand).and(sectVar.terms.contains(term))).orderBy(cand.name.asc).executeList()
        Ok(templates.courses.ListAll(courses, Some(term)))
      }
    }
  }
 
  def intersperse(els: List[STag], sep: STag): List[STag] = {
      els match {
        case Nil => Nil
        case h :: t => h :: sep :: intersperse(t, sep)
      }
    }

}
