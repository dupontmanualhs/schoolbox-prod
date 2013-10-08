package templates

import scala.collection.JavaConverters._
import scala.language.implicitConversions
import scalatags._
import scalatags.a
import play.api.templates.Html
import org.dupontmanual.forms.{ Binding, FormCall }
import _root_.config.users.{ Config, ProvidesInjector }
import controllers.users.VisitRequest
import models.users.User
import models.courses.{Teacher, Guardian, Student, Section}
import models.conferences.{Event, Session, Slot, TeacherActivation, ScheduleRow, Opening, Appointment}
import play.api.Play
import com.google.inject.Inject
import java.io.File
import scala.xml.Unparsed
import controllers.Conferences.timeReporter
import scala.xml.NodeSeq
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalTime
import org.joda.time.LocalDateTime
import org.dupontmanual.forms.Form
import models.courses.Term

package object conferences {
  val dateOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]

  private[conferences] class ConfigProvider @Inject() (val config: Config)
  private[conferences] val injector = Play.current.global.asInstanceOf[ProvidesInjector].provideInjector()
  private[conferences] implicit lazy val config: Config = injector.getInstance(classOf[ConfigProvider]).config
  
  private[conferences] val focusFirstTextField = script(Unparsed("""$(document).ready(function() { $("input:text:first").focus(); })"""))
  
  private[conferences] val guardianSchedulerScript = script.attr("type" -> "text/javascript"
		  												   ).src("/assets/javascripts/gSchedulerScript.js")
		  												   
  val longDateFmt = DateTimeFormat.forPattern("MMMM d, y")
  val timeFmt = DateTimeFormat.forPattern("h:mm a")
  val dateTimeFmt = DateTimeFormat.forPattern("h:mm a 'on' MMMM d, y")
  val shortDateTimeFmt = DateTimeFormat.forPattern("h:mm a 'on' MMM d")
  
  def teacherDisplay(teacher: Teacher, event: Event)(implicit req: VisitRequest[_], config: Config) = {
    config.main(event.name)(
      h1(s"Conferences: ${event.name}") ::
      event.sessions.map(s =>
        sessionInfoForTeacher(teacher, s)  
      )
    )
  }
  
  def sessionInfoForTeacher(teacher: Teacher, session: Session)(implicit req: VisitRequest[_], config: Config) = {
    div(
      h2(longDateFmt.print(session.date)),
      h3("Time: ", timeFmt.print(session.startTime), " - ", timeFmt.print(session.endTime)),
      h3("Scheduling Closes: ", dateTimeFmt.print(session.cutoff)),
      schedule(teacher, session)
    )
  }
  
  def schedule(teacher: Teacher, session: Session): STag = {
    TeacherActivation.get(teacher, session) match {
      case Some(ta) => {
        val rows: List[STag] = thead(th("Start Time"), th("Guardian"), th("Student(s)"), th("Phone Number(s)"), th("Comment"), td("Edit")) ::
            ta.scheduleRows().map(_ match {
              case op: Opening => tr(td.attr("colspan" -> "6")(a.cls("btn").href(controllers.routes.Conferences.reserveSlot(ta.id, op.start.getMillisOfDay()))(timeFmt.print(op.startTime))))
              case appt: Appointment => tr(td(timeFmt.print(appt.startTime)), td(appt.slot.guardians.toList.map(_.displayName).mkString(", ")),
                  td(appt.slot.students.toList.map(_.displayName).mkString(", ")),
                  td(List(appt.slot.phone, appt.slot.alternatePhone).flatten.mkString(", ")),
                  td(appt.slot.comment.getOrElse[String](""), 
                  td(a.href(controllers.routes.Conferences.teacherDelete(appt.slot.id).toString).cls("btn", "btn-danger", "btn-mini").title("Delete")(i.cls("icon-white", "icon-remove")),
                     a.href("#").cls("btn", "btn-mini").title("Edit")(i.cls("icon-edit")))))
              }
            )
        div(<p>You have activated conferences. Parents will be able to schedule a conference
            with you until scheduling closes at the time listed above.</p>,
          table.cls("table", "table-striped", "table-condensed")(rows: _*)
        )
      }
      case None => {
        <p>You have not activated conferences. When parents try to schedule conferences,
        they will be given the option of emailing you. To allow parents to schedule
        conferences using the system, click the button below.<br/>
        <a class="btn" href={ controllers.routes.Conferences.activateTeacher(session.id, teacher.id).toString }>Activate Scheduling</a></p>
      }
    }
  }
  
  object confirmDelete {    
    def apply(slot: Slot, form: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Delete Conference Appointment")(
        h1("Really Delete Conference Appointment?"),
        p("This should magically appear."),
        form.render()
      )
    }
  }
  
  object reserveSlot {
    def apply(form: Binding, start: LocalTime)(implicit req: VisitRequest[_], config: Config) = {
      config.main(s"Reserve the Slot at ${timeFmt.print(start)}")(
        h1(s"Reserve the Slot at ${timeFmt.print(start)}"),
        form.render()
      )
    }    
  }
  
  object guardianDisplay{
    def apply(guardian: Guardian, event: Event)(implicit req: VisitRequest[_], config: Config) = {
      config.main(event.name)(
        h1(s"Conferences: ${event.name}") ::
        event.sessions.map(s =>
          sessionInfoForGuardian(guardian, s)
        )
      )
    }
  }
  
  object chooseTeacherAndEvent {
    def apply(form: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Choose Teacher and Event")(
        h1("Choose Teacher and Event"),
        form.render()
      )
    }
  }
  
  object chooseGuardianAndEvent {
    def apply(form: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Choose Guardian and Event")(
        h1("Choose Guardian and Event"),
        form.render()
      )
    }
  }
  
  def sessionInfoForGuardian(guardian: Guardian, session: Session)(implicit req: VisitRequest[_], config: Config) = {
    import dateOrdering._

    val appts = Slot.getBySessionAndGuardian(session, guardian)
    val apptTeachers = appts.map(_.teacher)
    val unrealDepartments = List("", "Elective")
    val realSections = guardian.children.flatMap(s => s.activeEnrollments(Term.current)).map(_.section).filterNot(s => unrealDepartments.contains(s.course.department.name))
    val realTeachers = realSections.flatMap(_.teachers.toSet).toList.sortBy(_.formalName)
    val (confTeachers, nonConfTeachers) = realTeachers.partition(t => TeacherActivation.get(t, session).isDefined)
    def schedule() = {
      if (appts.isEmpty) {
        p("You don't have any appointments scheduled for this session.")
      } else {
        p("You have the following appointments:",
        ul(appts.map(a => {
          val time = s"${timeFmt.print(a.startTime)}-${timeFmt.print(a.endTime)}"
          val teacher = s"${a.teacher.displayName}"
          val students = a.students.toList.map(_.displayName).mkString(", ")
          li(s"$time with $teacher about $students")
        }): _* ))
      }
    }
    div(
      h2(longDateFmt.print(session.date)),
      schedule,
      if (!nonConfTeachers.isEmpty) {
        div(p("These teachers have not activated conference scheduling. Contact them to schedule a conference."),
          table.cls("table", "table-striped", "table-condensed")(nonConfTeachers.map(t => 
            tr(td(t.displayName), td(a.href(s"mailto:${t.user.email.getOrElse("")}")(t.user.email.getOrElse("no email available"): String)))))).toXML()
      } else {
        NodeSeq.Empty
      },
      if (LocalDateTime.now() < session.cutoff) {
        div(p("Click the button next to each teacher to schedule a conference."),
          table.cls("table", "table-striped", "table-condensed")(confTeachers.filterNot(apptTeachers.contains(_)).map(t =>
            tr(td(t.displayName), td(a.cls("btn").href(controllers.routes.Conferences.scheduleAppt(session.id, guardian.id, t.id))("Schedule Conference"))))))
      } else NodeSeq.Empty
    )
  }
  
  def guardianSignUp(teacher: Teacher, form: Binding)(implicit req: VisitRequest[_], config: Config) = {
    config.main(s"Schedule an Appointment with ${teacher.displayName}")(form.render())
  }
      
  def collapseMaker(ens: (Event, List[Session]), first: Boolean = false)
  				   (implicit req: VisitRequest[_], config: Config) = {
    div.cls("accordion-group")(
      div.cls("accordion-heading")(
        a.cls("accordion-toggle").attr("data-toggle" -> "collapse"
        ).attr("data-parent" -> "#confaccordion").href("#collapse" + ens._1.id)(
          ens._1.name
        )
      ),
      div.id("collapse" + ens._1.id).cls(if(first) "accordion-body collapse in" else "accordion-body collapse")(
        div.cls("accordion-inner")(
          if(ens._2.isEmpty) p("No sessions scheduled")
          else {
            for(session <- ens._2) yield {
              div(
                h2(session.date.toString()),
                p("Times Open: " + timeReporter(session.startTime) + " - " + timeReporter(session.endTime)),
                p("Deadline to Schedule a Conference: " + session.cutoff.toLocalTime.toString("h:mm") + " on " + session.cutoff.toLocalDate.toString()),
                teacherOrGuardianChoices(session.id)
              ).attr("id" -> session.id)
            }
          }
        )    
      )
    ).attr("id" -> ens._1.id)
  }
  
  def teacherOrGuardianChoices(sessionId: Long)(implicit req: VisitRequest[_], config: Config): STag = {
    Session.getById(sessionId) match {
      case Some(session) => { 
        req.visit.role match {
          case Some(t: Teacher) => {
            teacherChoices(t, session)
          }
          case Some(g: Guardian) => {
            guardianChoices(g, session)
          }
          case _ => {
            p("You must be logged in to schedule conferences.")
          }
        }
      }
      case _ => p("Could not identify this session.")
    }
  }
  
  object StudentScheduleForConferences {
    def apply(student: Student, session: Session,
              rows: Seq[STag], hasEnrollments: Boolean)
              (implicit req: VisitRequest[_], config: Config) = {
      config.main(s"${student.displayName}'s Schedule")(
        div.cls("page-header")(h2(student.displayName, small(session.event.name))),
        if (hasEnrollments) {
          table.cls("table", "table-striped", "table-condensed")(
            header +: rows)
        } else {
          p("This student is not enrolled in any courses for this term.")
        })
    }
  }
  
  def teacherChoices(teacher: Teacher, session: Session): STag = {
    if(TeacherActivation.get(teacher, session).isDefined) {
      a.href("/conferences/myConferences/" + session.id).cls("Button")("Manage My Conferences")
    } else {
      Seq(
        p("You have not yet activated scheduling for this conference session").cls("warning-text"),
        form.action(s"/conferences/activateTeacher/${session.id}").attr(("method" -> "get"))(
          input.attr(("type" -> "submit")).value("Activate for Conferences").cls("btn")
        )
      )
    }
  }
  
  def guardianChoices(guardian: Guardian, session: Session): STag = {
    for(stu <- guardian.children.toList) yield (a("Schedule a conference for " + stu.formalName
    									   ).href("/conferences/studentClasses/" + session.id + "/" + {if(stu.stateId != null) stu.stateId else stu.studentNumber}))
  }
  
  object eventForm {
    def apply(binding: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Add Conference Event")(
        h1("Add Conference Event"),
        binding.render())
    }
  }
  
  object listEvents {
    def apply(events: List[Event])(implicit req: VisitRequest[_], config: Config) = {
      config.main("List Conferences")(
        h1("List Conferences"),
        if (events.isEmpty) {
          p("There are no conference events created, yet.") 
        } else {
          ul(events.flatMap(e => infoForEvent(e)))
        },
        p(a.href(controllers.routes.Conferences.addEvent)("Add a new conference event"))
      ) 
    }

    def infoForEvent(event: Event): NodeSeq = {
      val sessions = event.sessions
      li(event.name, " - ", if (event.isActive) "ACTIVE" else "not active", 
          " - ", a.href(controllers.routes.Conferences.deleteEvent(event.id)).cls("confirmLink")("Delete"),
          if (sessions.isEmpty) {
            p("There are no sessions for this conference event, yet.")
          } else {
            ul(sessions.flatMap(s => infoForSession(s)))
          },
          p(a.href(controllers.routes.Conferences.addSession(event.id))("Add a new session for this event."))).toXML
    }
    
    def infoForSession(session: Session): NodeSeq = {
      li(session.toString).toXML
    }
  }
  
  object guardianSlotScheduler {
    def apply(table: STag, teacher: Teacher, student: Student, session: Session, event: Event)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Schedule a Conference", guardianSchedulerScript)(
        h1(teacher.formalName + " - Schedule a Conference for " + student.formalName).id("info"
            ).attr(("teacher" -> teacher.personId), ("event" -> event.id), ("session" -> session.id), ("student" -> student.studentNumber)),
        div.cls("span4")(h3("Hours"), table),
        div.cls("span4")(
          slotForm
        )
      )
      
    }

    def apply(form: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Schedule a Conference")(
        form.render(None, None)
      )
    }
  }
  
  object myConferencesParents {
    def apply(guardian: Guardian, session: Session, rows: Seq[STag])(implicit req: VisitRequest[_], config: Config) = {
      config.main(s"Conferences for ${guardian.shortName}")(
        h1(s"My Conferences for ${session.event.name} on ${session.date.formatted("mm/dd/yy")}"),
        table.cls("table table-striped span12")(
          tr(th("Time"), th("Name"), th("Student"), th("Room"), th("Cancel")),
          rows
        )
      )
    }
  }
  
  def slotForm = 
    div.cls("form-horizontal")(
      fieldset(
        legend("Schedule This Slot"),
        div.cls("control-group")(
           label("Phone Number").cls("control-label").attr("for" -> "phone"),
           div.cls("controls")(
             input.id("phone").attr("type" -> "text").placeholder("XXX-XXX-XXXX")
           )
        ),
        div.cls("control-group")(
          label("Alt Phone Number").cls("control-label").attr("for" -> "altphone"),
          div.cls("controls")(
            input.id("altphone").attr("type" -> "text").placeholder("XXX-XXX-XXXX")
          )
        ),
        div.cls("control-group")(
          label("Comments (Phone Conf. Request)").cls("control-label").attr("for" -> "comments"),
          div.cls("controls")(
            textarea.id("comments").placeholder("Insert any comments")
          )
        ),
        button.id("submit")("Submit")
       )
     )
}
