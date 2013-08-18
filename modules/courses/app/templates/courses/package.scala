package templates

import scalatags._
import play.api.Play
import play.api.templates.Html
import controllers.users.{ Theme, VisitRequest, AuthenticatedRequest }
import config.users.Config
import models.courses.{ Course, Section, Student, StudentEnrollment, Teacher, Term }
import org.dupontmanual.forms.{ Call, Method }
import Call._
import org.joda.time.format.DateTimeFormat
import com.google.inject.Inject
import config.users.ProvidesInjector

package object courses {  
  private[courses] class ConfigProvider @Inject()(val config: Config)
  private[courses] val injector = Play.current.global.asInstanceOf[ProvidesInjector].provideInjector()
  private[courses] implicit lazy val config: Config = injector.getInstance(classOf[ConfigProvider]).config

  object TeacherSchedule {
    def apply(teacher: Teacher, term: Term,
      rows: Seq[STag], hasAssignments: Boolean)(implicit req: VisitRequest[_]) = {
      config.main(s"${teacher.displayName}'s Schedule")(
        div.cls("page-header")(h2(teacher.displayName, " ",small(term.name))),
        if (hasAssignments) {
          table.cls("table", "table-striped", "table-condensed")(
            thead(th("Period"), th("Course(s)"), th("Room(s)"), th("Students")) +: rows)
        } else {
          p("This teacher is not assigned to any courses during this term.")
        })
    }
  }

  object StudentSchedule {
    def apply(student: Student, term: Term,
      rows: Seq[STag], hasEnrollments: Boolean)(implicit req: VisitRequest[_], config: Config) = {
      config.main(s"${student.displayName}'s Schedule")(
        div.cls("page-header")(h2(student.displayName, small(term.name))),
        if (hasEnrollments) {
          table.cls("table", "table-striped", "table-condensed")(
            thead(th("Period"), th("Course(s)"), th("Teacher(s)"), th("Room(s)")) +: rows)
        } else {
          p("This student is not enrolled in any courses for this term.")
        })
    }
  }

  object NavLink extends Enumeration {
    type NavLink = Value
    val Home = Value(1, "Home")
    val Announcements = Value(2, "Announcements")
    val Files = Value(3, "Files")
    val Assignments = Value(4, "Assignments")
    val Grades = Value(5, "Grades")
    val Roster = Value(6, "Roster")

    lazy val link: Map[Value, Call] = { //TODO: put in actual links
      values.toSeq.map(v => (v -> Call(Method.GET, "#"))).toMap
    }
  }

  object SectionNav {
    def apply(active: NavLink.NavLink, parentTitle: String) = Seq(linkList(active), breadCrumb(active, parentTitle))

    def linkList(active: NavLink.NavLink) = {
      div.cls("span3")(
        div.cls("well", "sidebar-nav")(
          ul.cls("nav", "nav-list")(
            NavLink.values.toSeq.map { v =>
              if (active != v) li(a.href(NavLink.link(v))(v.toString))
              else li.cls("active")(a(v.toString))
            })))
    }

    def breadCrumb(active: NavLink.NavLink, parentTitle: String) = {
      val divider = span.cls("divider")("/")
      div.cls("span8")(
        ul.cls("breadcrumb")(
          li(a.href("#")("Courses"), divider),
          li(a.href("#")(parentTitle), divider),
          li.cls("active")(active.toString)))
    }
  }

  object Roster {
    def apply(section: Section)(implicit req: VisitRequest[_], config: Config) = {
      val df = DateTimeFormat.shortDate()
      val active = NavLink.Roster
      config.main(s"${active} for ${section.displayName}")(
        SectionNav(active, section.displayName),
        div.cls("span8")(
          table.cls("table", "table-striped", "table-condensed")(
            thead(th("Student"), th("Start Date"), th("End Date")),
            tbody(section.enrollments.map { e =>
              tr(td(e.student.formalName), td(e.start.map(d => StringSTag(df.print(d))).getOrElse("")), td(e.end.map(d => StringSTag(df.print(d))).getOrElse("")))
            }))))
    }
  }

  object ListAll {
    def apply(courses: List[Course], term: Option[Term] = None)(implicit req: VisitRequest[_], config: Config) = {
      val title = s"List of All Courses${term.map(t => s" Offered in ${t.name}").getOrElse("")}"
      config.main(title)(
        div.cls("page-header")(
          h2(title)),
        ul(courses.map(course =>
          li(a.href(controllers.courses.routes.App.sectionList(course.masterNumber))(s"${course.name} - ${course.masterNumber}")))))
    }
  }

  object SectionList {
    def apply(course: Course, sections: List[Section])(implicit req: VisitRequest[_], config: Config) = {
      val title = s"Sections of: ${course.name}"
      config.main(title)(
        div.cls("page-header")(
          h2(title)),
        if (sections.isEmpty) {
          p("There are currently no sections for this course.")
        } else {
          table.cls("table", "table-striped", "table-condensed")(
            thead(th("Teacher(s)"), th("Block"), th("Term")),
            tbody(sections.map(s =>
              s.teachers.map(t =>
                tr(td(t.displayName), td(s.periodNames), td(s.terms.toList.map(_.name).mkString(", ")))))))
        })
    }
  }

  object MasterList {
    def apply(sections: List[Section])(implicit req: VisitRequest[_], config: Config) = {
      val title = "Section Master List"
      config.main(title, javascript(config.webjars("jquery.dataTables.js")),
        script)(
          div.cls("page-header")(
            h2(title)),
          table.cls("table", "table-striped", "table-condensed").id("sections")(
            thead(
              th("Department"),
              th("Course"),
              th("Course Number"),
              th("Id"),
              th("Teacher(s)"),
              th("Room"),
              th("Period(s)"),
              th("Term(s)"),
              th("Num Students")),
            tbody(sections.map(section =>
              tr(
                td(section.course.department.name),
                td(section.course.name),
                td(section.course.masterNumber),
                td(section.sectionId),
                td(section.teachers.map(_.displayName).mkString(", ")),
                td(section.room.name),
                td(section.periodNames),
                td(section.terms.map(_.name).mkString(", ")),
                td(section.numStudents.toString))))))
    }
  }
}