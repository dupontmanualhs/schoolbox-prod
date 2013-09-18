package templates

import scala.language.implicitConversions
import scalatags._
import scalatags.a
import play.api.templates.Html
import org.dupontmanual.forms.{ Binding, FormCall }
import _root_.config.users.{ Config, ProvidesInjector }
import controllers.users.VisitRequest
import models.users.User
import models.courses.{Teacher, Guardian, Student, Section}
import models.conferences.{Event, Session, Slot, TeacherActivation}
import play.api.Play
import com.google.inject.Inject
import java.io.File
import scala.xml.Unparsed
import controllers.Conferences.timeReporter


package object conferences {
  private[conferences] class ConfigProvider @Inject() (val config: Config)
  private[conferences] val injector = Play.current.global.asInstanceOf[ProvidesInjector].provideInjector()
  private[conferences] implicit lazy val config: Config = injector.getInstance(classOf[ConfigProvider]).config
  
  private[conferences] val focusFirstTextField = script(Unparsed("""$(document).ready(function() { $("input:text:first").focus(); })"""))
  
  private[conferences] val guardianSchedulerScript = script.attr("type" -> "text/javascript"
		  												   ).src("/assets/javascripts/gSchedulerScript.js")
  
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
  
  def teacherChoices(teacher: Teacher, session: Session): STag = {
    if(TeacherActivation.isActivated(teacher, session)) {
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
  
  object index {
    def apply(eventsAndSessions: List[(Event, List[Session])])(implicit req: VisitRequest[_], config: Config) = {
      config.main("Conferences")(
        h1("Active Conferences"),
        div.cls("accordion").id("confaccordion")(
          collapseMaker(eventsAndSessions.head, true) :: eventsAndSessions.tail.map(collapseMaker(_))   
        )
      )
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
