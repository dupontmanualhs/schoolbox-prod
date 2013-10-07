package controllers

import scala.xml.{ Attribute, NodeSeq, Null, Text }
import controllers.users.{MenuItem, MenuBar}
import models.users._
import models.courses._
import models.conferences._
import models.conferences.Conferences.Permissions
import org.dupontmanual.forms.fields._
import org.dupontmanual.forms.validators.{ ValidationError, Validator }
import org.dupontmanual.forms.widgets.{ Textarea, Widget }
import org.dupontmanual.forms.FormCall
import util.Helpers._
import java.sql.Date
import java.sql.Time
import java.util.Date
import java.sql.Timestamp
import org.dupontmanual.forms.{ Form, Binding, ValidBinding, InvalidBinding }
import play.api.mvc.Controller
import config.Config
import com.google.inject.{ Inject, Singleton }
import controllers.users.{ Authenticated, RoleMustPass, VisitAction }
import org.joda.time.{LocalDate, LocalTime, LocalDateTime}
import config.users.UsesDataStore
import scalatags._
import templates.conferences
import Conferences._
import controllers.users.VisitRequest
import controllers.users.PermissionRequired
import play.api.templates.Html
import org.joda.time.format.DateTimeFormat

@Singleton
class Conferences @Inject()(implicit config: Config) extends Controller with UsesDataStore {
  val dateOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
  import dateOrdering._

  class TeacherSlotForm(ta: TeacherActivation, startTime: LocalTime) extends Form {
    val guardian = Guardian.ChooseGuardianField(ta.teacher.studentsTaught(Term.current()))
    val phoneNumber = new PhoneFieldOptional("phoneNumber")
    val altPhone = new PhoneFieldOptional("alternatePhone")
    val comment = new TextFieldOptional("comment")
    
    val fields = List(guardian, phoneNumber, altPhone, comment)
    
    override def validate(vb: ValidBinding): ValidationError = {
      val guard = vb.valueOf(guardian)
      val phone = vb.valueOf(phoneNumber)
      val alt = vb.valueOf(altPhone)
      val comm = vb.valueOf(comment)
      if (!(guard.isDefined || phone.isDefined || alt.isDefined || comm.isDefined)) {
        ValidationError("You must enter at least one value to reserve the slot.")
      } else {
        val slot = new Slot(ta.session, ta.teacher, startTime, ta.slotInterval, Set(), Set(guard).flatten, phone, alt, comm)
        slot.isValid()
      }
    }
  }
  
  def listEvents() = PermissionRequired(Permissions.Manage) { implicit req => 
    dataStore.execute { pm =>
      // TODO: this should be ordered most recent to least
      val events: List[Event] = pm.query[Event].executeList()
      Ok(conferences.listEvents(events))
    }
  }
  
  def eventHelper(eventId: Long, template: (Event => Html))(implicit req: VisitRequest[_]) = {
    Event.getById(eventId) match {
      case None => NotFound("There is no ConferenceEvent with the given id.")
      case Some(event) => Ok(template(event))
    }
  }
  
  def eventForTeacher(eventId: Long) = RoleMustPass(_.isInstanceOf[Teacher]) { implicit req =>
    eventHelper(eventId, (event: Event) => conferences.teacherDisplay(req.role.asInstanceOf[Teacher], event))
  }
  
  object TeacherEventForm extends Form {
    val teacher = new Teacher.ChooseActiveTeacherField("teacher")
    val event = new ChoiceField("event", Event.getActive().map(e => (e.name, e)))
    
    def fields = List(teacher, event)
  }
  
  def viewTeacher() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(conferences.chooseTeacherAndEvent(Binding(TeacherEventForm)))
  }
  
  def viewTeacherP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(TeacherEventForm, req) match {
      case ib: InvalidBinding => Ok(conferences.chooseTeacherAndEvent(ib))
      case vb: ValidBinding => {
        val teacherId = vb.valueOf(TeacherEventForm.teacher).id
        val eventId = vb.valueOf(TeacherEventForm.event).id
        Redirect(routes.Conferences.viewTeacherSchedule(eventId, teacherId))
      }
    }  
  }
  
  def viewTeacherSchedule(eventId: Long, teacherId: Long) = PermissionRequired(Permissions.Manage) { implicit req =>
    Teacher.getById(teacherId) match {
      case None => NotFound("There is no teacher with the given id.")
      case Some(teacher) => eventHelper(eventId, (event: Event) => conferences.teacherDisplay(teacher, event))
    }  
  }
  
  class InfoField(name: String, value: String) extends TextField(name) {
    override def initialVal = Some(value)
    override def widgetAttrs(widget: Widget) = Attribute("disabled", Text("disabled"), super.widgetAttrs(widget))
  }
  
  class SlotDelete(slot: Slot, role: Role) extends Form {
    val time = new InfoField("time", conferences.timeFmt.print(slot.startTime))
    val teacher = new InfoField("teacher", slot.teacher.displayName)
    val guardians = new InfoField("guardian(s)", slot.guardians.map(_.displayName).mkString(", "))
    val students = new InfoField("student(s)", slot.students.map(_.displayName).mkString(", "))
    val comment = new InfoField("comment", slot.comment.getOrElse(""))
    val confirm = new BooleanField("confirm", "Check to Confirm Deletion") {
      override def validate(value: Boolean) = {
        if (value) Right(true) else Left(ValidationError("You must check the box to confirm deletion or cancel the form."))
      }
    }
    
    def fields = List(Some(time),
        if (role.id != slot.teacher.id) Some(teacher) else None, 
        if (!slot.guardians.map(_.id).contains(role.id)) Some(guardians) else None, 
        Some(students), 
        Some(comment), 
        Some(confirm)).flatten
  }
  
  def teacherDelete(slotId: Long) = Slot.getById(slotId) match {
    case None => VisitAction { implicit req => NotFound("No conference slot with the given id.") }
    case Some(slot) => RoleMustPass(r => r.hasPermission(Permissions.Manage) || 
        (r.isInstanceOf[Teacher] && r.id == slot.teacher.id)) { implicit req =>
      val form = Binding(new SlotDelete(slot, req.role))
      Ok(conferences.confirmDelete(slot, form))      
    }
  }
  
  def teacherDeleteP(slotId: Long) = Slot.getById(slotId) match {
    case None => VisitAction { implicit req => NotFound("No conference slot with the given id.") }
    case Some(slot) => RoleMustPass(r => r.hasPermission(Permissions.Manage) || 
        (r.isInstanceOf[Teacher] && r.id == slot.teacher.id)) { implicit req =>
      val form = Binding(new SlotDelete(slot, req.role), req)    
      form match {
        case ib: InvalidBinding => Ok(conferences.confirmDelete(slot, form))
        case vb: ValidBinding => {
          val redirectUrl = if (req.role.id == slot.teacher.id) controllers.routes.Conferences.eventForTeacher(slot.session.event.id)
            else controllers.routes.Conferences.viewTeacherSchedule(slot.session.event.id, slot.teacher.id)
          dataStore.execute{ pm => 
            val msg = Slot.getById(slotId).map { s => 
              pm.deletePersistent(s)
              "message" -> "The conference appointment was successfully deleted."
            }.getOrElse("alert" -> "The appointment was not deleted.")
            Redirect(redirectUrl).flashing(msg)
          }
        }
      }
    }
  }
  
  def eventForGuardian(eventId: Long) = RoleMustPass(_.isInstanceOf[Guardian]) { implicit req =>
    eventHelper(eventId, (event: Event) => conferences.guardianDisplay(req.role.asInstanceOf[Guardian], event))
  }
  
  def eventForGuardianP(eventId: Long) = RoleMustPass(_.isInstanceOf[Guardian]) { implicit req =>
    NotFound("Not implemented yet.")  
  }
  
  object GuardianEventForm extends Form {
    val guardian = new Guardian.ChooseActiveGuardianField("guardian")
    val event = new ChoiceField("event", Event.getActive().map(e => (e.name, e)))
    
    def fields = List(guardian, event)
  }
  
  def viewGuardian() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(conferences.chooseGuardianAndEvent(Binding(GuardianEventForm)))
  }
  
  def viewGuardianP() = PermissionRequired(Permissions.Manage) { implicit req =>
    Binding(GuardianEventForm, req) match {
      case ib: InvalidBinding => Ok(conferences.chooseGuardianAndEvent(ib))
      case vb: ValidBinding => {
        val guardianId = vb.valueOf(GuardianEventForm.guardian).id
        val eventId = vb.valueOf(GuardianEventForm.event).id
        Redirect(routes.Conferences.viewGuardianSchedule(eventId, guardianId))
      }
    }  
  }
  
  def viewGuardianSchedule(eventId: Long, guardianId: Long) = PermissionRequired(Permissions.Manage) { implicit req =>
    Guardian.getById(guardianId) match {
      case None => NotFound("There is no guardian with the given id.")
      case Some(guardian) => eventHelper(eventId, (event: Event) => conferences.guardianDisplay(guardian, event))
    }  
  }
  
  class GuardianSignUp(session: Session, teacher: Teacher, guardian: Guardian, times: List[LocalTime]) extends Form {
    val timeFmt = DateTimeFormat.forPattern("h:mm a")
    val time = new ChoiceField("time", times.map(t => (timeFmt.print(t), t)))
    val phoneNumber = new PhoneFieldOptional("phoneNumber")
    val altPhone = new PhoneFieldOptional("alternatePhone")
    val comment = new TextFieldOptional("comment")
    
    def fields = List(time, phoneNumber, altPhone, comment)
    
    override def validate(vb: ValidBinding): ValidationError = {
      Session.getById(session.id) match {
        case None => ValidationError("No session with the given id.")
        case Some(session) => Teacher.getById(teacher.id) match {
          case None => ValidationError("No teacher with the given id.")
          case Some(teacher) => Guardian.getById(guardian.id) match {
            case None => ValidationError("No guardian with the given id.")
            case Some(guardian) => {
              val slot = new Slot(
                  session, teacher, vb.valueOf(time), Set(), Set(guardian),
                  vb.valueOf(phoneNumber), vb.valueOf(altPhone), vb.valueOf(comment))
              slot.isValid()
            }
          }
        }
      }
    }
  }
  
  def scheduleAppt(sessionId: Long, guardianId: Long, teacherId: Long) = 
      RoleMustPass(r => r.permissions().contains(Permissions.Manage) || (r.isInstanceOf[Guardian] && r.id == guardianId)) { implicit req =>
    Session.getById(sessionId) match {
      case None => NotFound("No session with the given id.")
      case Some(session) => Teacher.getById(teacherId) match {
        case None => NotFound("No teacher with the given id.")
        case Some(teacher) => Guardian.getById(guardianId) match {
          case None => NotFound("No guardian with the given id.")
          case Some(guardian) => TeacherActivation.get(teacher, session) match {
            case None => NotFound("This teacher hasn't activated conference scheduling.")
            case Some(ta) => {
              val teacherApptTimes = ta.appointments().map(_.startTime)
              val guardianApptTimes = Slot.getBySessionAndGuardian(session, guardian).map(_.startTime)
              val apptTimes = teacherApptTimes.toSet ++ guardianApptTimes.toSet
              val availableTimes = ta.allOpenings().map(
                  _.startTime).filterNot(time => apptTimes.contains(time))
              Ok(conferences.guardianSignUp(teacher, Binding(new GuardianSignUp(session, teacher, guardian, availableTimes))))
            }
          }
        }
      }
    }
  }

  def scheduleApptP(sessionId: Long, guardianId: Long, teacherId: Long) =
    RoleMustPass(r => r.permissions().contains(Permissions.Manage) || (r.isInstanceOf[Guardian] && r.id == guardianId)) { implicit req =>
      dataStore.execute { pm =>
        Session.getById(sessionId) match {
          case None => NotFound("No session with the given id.")
          case Some(session) => Teacher.getById(teacherId) match {
            case None => NotFound("No teacher with the given id.")
            case Some(teacher) => Guardian.getById(guardianId) match {
              case None => NotFound("No guardian with the given id.")
              case Some(guardian) => TeacherActivation.get(teacher, session) match {
                case None => NotFound("This teacher hasn't activated conference scheduling.")
                case Some(ta) => {
                  val teacherApptTimes = ta.appointments().map(_.startTime)
                  val guardianApptTimes = Slot.getBySessionAndGuardian(session, guardian).map(_.startTime)
                  val apptTimes = teacherApptTimes.toSet ++ guardianApptTimes.toSet
                  val availableTimes = ta.allOpenings().map(
                    _.startTime).filterNot(time => apptTimes.contains(time))
                  val form = new GuardianSignUp(session, teacher, guardian, availableTimes)
                  Binding(form, req) match {
                    case ib: InvalidBinding => Ok(conferences.guardianSignUp(teacher, ib))
                    case vb: ValidBinding => {
                      val slot = new Slot(session, teacher, vb.valueOf(form.time), Set(), Set(guardian),
                        vb.valueOf(form.phoneNumber), vb.valueOf(form.altPhone), vb.valueOf(form.comment))
                      pm.makePersistent(slot)
                      Redirect(routes.Conferences.eventForGuardian(session.event.id))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  
  def guardianDelete(slotId: Long) = Slot.getById(slotId) match {
    case None => VisitAction { implicit req => NotFound("No conference slot with the given id.") }
    case Some(slot) => RoleMustPass(r => r.hasPermission(Permissions.Manage) ||
        (r.isInstanceOf[Guardian] && slot.guardians.map(_.id).contains(r.id))) { implicit req =>
      val form = Binding(new SlotDelete(slot, req.role))
      Ok(conferences.confirmDelete(slot, form))
    }
  }
  
  def guardianDeleteP(slotId: Long) = Slot.getById(slotId) match {
    case None => VisitAction { implicit req => NotFound("No conference slot with the given id.") }
    case Some(slot) => RoleMustPass(r => r.hasPermission(Permissions.Manage) ||
        (r.isInstanceOf[Guardian] && slot.guardians.map(_.id).contains(r.id))) { implicit req =>
      val form = Binding(new SlotDelete(slot, req.role), req)
      form match {
        case ib: InvalidBinding => Ok(conferences.confirmDelete(slot, form))
        case vb: ValidBinding => {
          val redirectUrl = if (slot.guardians.map(_.id).contains(req.role.id)) controllers.routes.Conferences.eventForGuardian(slot.session.event.id)
            else if (slot.guardians.size == 1) controllers.routes.Conferences.viewGuardianSchedule(slot.session.event.id, slot.guardians.toList(0).id)
            else controllers.routes.Conferences.viewGuardian()
          dataStore.execute{ pm => 
            val msg = Slot.getById(slotId).map { s => 
              pm.deletePersistent(s)
              "message" -> "The conference appointment was successfully deleted."
            }.getOrElse("alert" -> "The appointment was not deleted.")
            Redirect(redirectUrl).flashing(msg)
          }
        }
      }
    }
  }
  
  def viewAsTeacher() = VisitAction { implicit req =>
    dataStore.execute { implicit pm =>
      val currUser: Option[User] = req.visit.user
      val events = pm.query[Event].executeList()
      val sessions = pm.query[models.conferences.Session].executeList()
      Redirect(routes.Conferences.teacherView())
    }
  }
  
  def reserveSlot(teacherActivationId: Long, startAsMillis: Int) = {
    val ta = TeacherActivation.getById(teacherActivationId)
    RoleMustPass(r => ta.isDefined && (r.permissions().contains(Permissions.Manage) || ta.get.teacher == r)) { implicit req =>
      val start: LocalTime = LocalTime.fromMillisOfDay(startAsMillis)
      val form = new TeacherSlotForm(ta.get, start)
      Ok(conferences.reserveSlot(Binding(form), start))
    }
  }
  
  def reserveSlotP(teacherActivationId: Long, startAsMillis: Int) = {
    dataStore.execute { pm =>
      val ta = TeacherActivation.getById(teacherActivationId)
      RoleMustPass(r => ta.isDefined && (r.permissions().contains(Permissions.Manage) || ta.get.teacher == r)) { implicit req =>
        val start: LocalTime = LocalTime.fromMillisOfDay(startAsMillis)
        val form = new TeacherSlotForm(ta.get, start)
        Binding(form, req) match {
          case ib: InvalidBinding => Ok(conferences.reserveSlot(ib, start))
          case vb: ValidBinding => {
            val maybeGuardian = vb.valueOf(form.guardian).flatMap(g => pm.query[Guardian].filter(QGuardian.candidate().id.eq(g.id)).executeOption())
            val phone = vb.valueOf(form.phoneNumber)
            val alt = vb.valueOf(form.altPhone)
            val comment = vb.valueOf(form.comment)
            val slot = new Slot(ta.get.session, ta.get.teacher, start, ta.get.slotInterval, Set(), Set(),
                phone, alt, comment)
            maybeGuardian match {
              case None => // do nothing
              case Some(guardian) => slot.guardians = Set(guardian)
            }
            pm.makePersistent(slot)
            Redirect(routes.Conferences.eventForTeacher(ta.get.session.event.id))
          }
        }  
      }
    }
  }
      

  def guardianSlotScheduler(sessionId: Long, teacherId: String, studentId: String) = {
    guardianSlotRequestValidator(sessionId, teacherId, studentId) { guardianSlotSchedulerBlock }
  }
  
  
  def guardianSlotSchedulerP(sessionId:Long, teacherId: String, studentId:String) = {
    guardianSlotRequestValidator(sessionId, teacherId, studentId) { guardianSlotSchedulerPBlock }
  }
  
  def guardianSlotRequestValidator(sessionId: Long, teacherId: String, studentId: String)
    (f: (Guardian, Student, Teacher, Session, VisitRequest[_], scalajdo.ScalaPersistenceManager) => play.api.mvc.Result) = Authenticated { implicit req => 
     dataStore.execute { implicit pm => 
       val maybeStudent = Student.getByStateId(studentId).orElse(Student.getByStudentNumber(studentId))
       (req.role, maybeStudent) match {
          case (guardian: Guardian, Some(student)) => {
            if(guardian.children.contains(student)) {
              val maybeTeacher = Teacher.getByPersonId(teacherId).orElse(Teacher.getByStateId(teacherId))
              val maybeSession = pm.query[Session].filter(QSession.candidate.id.eq(sessionId)).executeOption
              (maybeTeacher, maybeSession) match {
                case (_, None) => Redirect(routes.Conferences.listEvents()).flashing(("error", "Session could not be found."))
                case (None, _) => Redirect(routes.Conferences.listEvents()).flashing(("error", "Teacher could not be found."))
                case (Some(teacher), Some(session)) => f(guardian, student, teacher, session, req, pm)
              }
            }
            else Redirect(routes.Conferences.listEvents).flashing(("error", "You are not a guardian of the indicated student."))
          }
          case (_, Some(student)) => { Redirect(routes.Conferences.listEvents).flashing(("error", "You must be a guardian to use this feature."))}
          case (_, _) => { Redirect(routes.Conferences.listEvents).flashing(("error", "Student could not be found.")) }
        }
     }
  }
  
  def activateTeacher(sessionId: Long, teacherId: Long) = 
        RoleMustPass(r => r.permissions().contains(Permissions.Manage) || r.id == teacherId) { implicit req =>
    dataStore.execute { pm =>
      (Session.getById(sessionId), Teacher.getById(teacherId)) match {
        case (Some(session), Some(teacher)) => {
          val ta = new TeacherActivation(session, teacher, None)
          pm.makePersistent(ta)
          Redirect(routes.Conferences.eventForTeacher(session.event.id)).flashing("message" -> "Conference scheduling was activated.")
        }
        case (None, Some(teacher)) => NotFound("No conference session with that id.")
        case (Some(session), None) => NotFound("No teacher with that id.")
        case (None, None) => NotFound("Both the conference session and the teacher couldn't be found.")
      }
    }
  }
  
  def guardianSlotSchedulerBlock(guardian: Guardian, student: Student, teacher: Teacher, session: Session, req: VisitRequest[_], pm: scalajdo.ScalaPersistenceManager) = {
      implicit val impReq = req
      implicit val impPm = pm
      val form = new SlotForm(guardian, student, teacher, session)
      Ok(templates.conferences.guardianSlotScheduler(Binding(form)))
   }
  
   def guardianSlotSchedulerPBlock(guardian: Guardian, student: Student, teacher: Teacher, session: Session, req: VisitRequest[_], pm: scalajdo.ScalaPersistenceManager) = {
     implicit val impReq = req
     implicit val impPm = pm      
     val slotForm = new SlotForm(guardian, student, teacher, session)
           Binding(slotForm, req) match  {
             case ib: InvalidBinding => println(ib.fieldErrors + "" + ib.formErrors); Ok(templates.conferences.guardianSlotScheduler(ib))
             case vb: ValidBinding => {
                 val slot = vb.valueOf(slotForm.slots)
                 val phone = vb.valueOf(slotForm.phone)
                 val altphone = vb.valueOf(slotForm.altphone)
                 val comments = vb.valueOf(slotForm.comments)
                 slot.phone_=(phone)
                 slot.alternatePhone_=(altphone)
                 slot.comment_=(comments)
                 pm.makePersistent(slot)
                 Redirect(s"/conferences/myConferences/${session.id}")
             }
           }
   }
   
  class SlotForm(val guardian: Guardian, val student: Student, val teacher: Teacher, val session: Session) extends Form {
    val start = session.startTime
    val end = session.endTime
    val minuteDifference = ((end.getMillisOfDay() - start.getMillisOfDay()) / 1000) / 60
    val numOfConferences = minuteDifference / 10
    val allIntervals = for(i <- 0 to numOfConferences - 1) yield start.plusMinutes(i * 10)
    val slotsSorted = allIntervals.map(new Slot(session, teacher, _, 10, Set(student), Set(guardian), None,
    											None, None))
    val slotsFormatted = slotsSorted.map((slot: Slot) => (Conferences.timeReporter(slot.startTime), slot)).toList
    val slotsFormattedIndexed = slotsFormatted.zipWithIndex
    val slotsIndexedByHour = slotsFormattedIndexed.groupBy(_._1._2.startTime.getHourOfDay()).toList.sortBy(_._1)
    
    val phone = new PhoneFieldOptional("phone")
    val altphone = new PhoneFieldOptional("altphone")
    val comments = new TextFieldOptional("comments") { override def widget = new Textarea(false)}
    val slots = new RadioField("slots", slotsFormatted)
    val fields = List(slots, phone, altphone, comments)
    
    override def render(bound: Binding, overrideSubmit: Option[FormCall] = None, 
                        legend: Option[String] = None): NodeSeq = {
      h1(s"Schedule a Conference with ${teacher.shortName}").toXML ++
      h2(s"For student ${student.shortName}").toXML ++ 
      {
      form.id(s"form ${bound.hasErrors}").attr(("method" -> "POST"))(
        div.cls("span4")(
          h3("Scheduled Times"),
          slotTable(slotsIndexedByHour, session, teacher)
        ),
        div.cls("span4")(
          h3("Optional Info"),
          phone.render(bound),
          altphone.render(bound),
          comments.render(bound),
          button.attr(("type" -> "submit")).cls("btn btn-primary")("Submit!")
        )
      )
      }.toXML
    }
    
    override def validate(vb: ValidBinding): ValidationError = {
      if(vb.valueOf(slots).validateSlot) ValidationError(Nil) else ValidationError("Time already taken")
    }
  }
  
  def slotTable(indexedSlotsByHour: List[(Int, List[((String, Slot), Int)])], session: Session, teacher: Teacher): STag = {
    val collapses = for((hour, slotInfos) <- indexedSlotsByHour) yield {
      div.cls("accordion-group")(
        div.cls("accordion-heading")(
          a.cls("accordion-toggle").attr("data-toggle" -> "collapse"
          ).attr("data-parent" -> "#confaccordion").href("#collapse" + hour)(
            Conferences.timeReporter(new LocalTime(hour, 0, 0)) + " - " + Conferences.timeReporter(new LocalTime(hour + 1, 0, 0))
          )  
        ),
        div.id("collapse" + hour).cls("accordion-body collapse")(
          div.cls("accordion-inner")(
            table(tr(th("Times"), th("Select")),
              for(slotInfo <- slotInfos) yield {
                val slcand = QSlot.candidate
                val sampleSlot = slotInfo._1._2
                val isValid = sampleSlot.validateSlot
                if(isValid) { 
                  tr.cls("open")(
                	td(slotInfo._1._1), 
                    td(input.attr("type" -> "radio").name("slots").value(slotInfo._2))
                  ) 
                }
                else { 
                  tr.cls("error")(
                	td(slotInfo._1._1), 
                    td(input.attr("type" -> "radio").name("slots").value(slotInfo._2).attr("disabled" -> ""), 
                	   div("Taken").display("inline").attr("style" -> "font-size: 12px;").padding_left("5px"))
                  )
                }
              }    
            ).cls("table table-striped")
          )
        )
      )
    }
    div(collapses.toList)
  }
  
  def studentClasses(sessionId: Long, studentId: String) = Authenticated { implicit req =>
    dataStore.execute { implicit pm =>
      val maybeStudent = Student.getByStateId(studentId).orElse(Student.getByStudentNumber(studentId))
      val maybeSession = pm.query[Session].filter(QSession.candidate.id.eq(sessionId)).executeOption
      (req.role, maybeStudent, maybeSession) match {
        case (guardian: Guardian, Some(student), Some(session)) => {
          if(guardian.children.contains(student)) {
            studentScheduleForConferences(student, session)
          } else {
            Redirect(routes.Conferences.listEvents).flashing("error" -> "You are not the guardian of the requested student.")
          }
        }
        case (guardian: Guardian, Some(_), _) => Redirect(routes.Conferences.listEvents).flashing("error" -> "Could not find requested session")
        case (guardian: Guardian, _, _) => Redirect(routes.Conferences.listEvents).flashing("error" -> "Could not find requested student.")
        case (_, _, _) => Redirect(routes.Conferences.listEvents).flashing("error" -> "You must be a guardian to view a schedule in conferences.")
      }
    }
  }
  
  def studentScheduleForConferences(student: Student, session: Session)
    (implicit req: VisitRequest[_], pm: scalajdo.ScalaPersistenceManager) = {
    val (rows, hasEnrollments) = {
      val controller = new controllers.courses.App()
      import controller.{scheduleConstructor, intersperse}
      
      def linkOrNot(section: Section): STag = {
        pm.query[TeacherActivation].filter(QTeacherActivation.candidate.teacher.eq(section.teachers.head)).executeOption match {
          case Some(ta) => 
            a.href(s"/conferences/guardianRegister/${session.id}/${section.teachers.head.personId}/${student.studentNumber}")
        		  ("Schedule a Conference with this Teacher")
          case None => p("This teacher has not activated online scheduling")
        		                                              
        }
      }
      
      scheduleConstructor(student, Term.current) 
        { ls => td(intersperse(ls.map(s => linkOrNot(s)), br())) }
    }
    Ok(templates.conferences.StudentScheduleForConferences(student, session, rows, hasEnrollments))
  }
  
  def myConferences(sessionId: Long) = Authenticated { implicit req =>
    dataStore.execute { implicit pm => 
      val maybeSession = pm.query[Session].filter(QSession.candidate.id.eq(sessionId)).executeOption()
      (req.role, maybeSession) match {
        case (_, None) => Redirect(routes.Conferences.listEvents).flashing("error" -> "Could not find requested session")
        case (guardian: Guardian, Some(session)) => myConferencesForParents(guardian, session)
        case (teacher: Teacher, Some(session)) => Redirect(routes.Conferences.listEvents).flashing("error" -> "Functionality not added yet")
        case (_, _) => Redirect(routes.Conferences.listEvents).flashing("error" -> "You must be a teacher or guardian to access this page.")
      }
    }
  }
  
  def myConferencesForParents(guardian: Guardian, session: Session)
                             (implicit req: VisitRequest[_], pm: scalajdo.ScalaPersistenceManager) = {
    val slots = pm.query[Slot].filter(QSlot.candidate.session.eq(session)).executeList.filter(_.guardians.contains(guardian))
    println(slots)
    println(pm.query[Slot].executeList.map(s => s"\n${s.teacher.formalName}--${s.guardians}"))
    val slotsSorted = slots.sortBy(_.startTime)
    def likelyRoom(student: Student, teacher: Teacher) = {
      val enrollments: List[StudentEnrollment] = {
        val sectVar = QSection.variable("sectVar")
        val cand = QStudentEnrollment.candidate()
        pm.query[StudentEnrollment].filter(cand.student.eq(student).and(cand.section.eq(sectVar)).and(sectVar.terms.contains(Term.current))).executeList()
      }
      val sections = enrollments.map(_.section)
      if(sections.size == 0) "No room found." else sections.filter(_.teachers.contains(teacher)).head.room.name
    }
    val rows = slots.map(s => tr(td(timeReporter(s.startTime)), td(s.teacher.shortName), 
    							 td(s.students.head.shortName), td(likelyRoom(s.students.head, s.teacher)), 
    							 td(form.attr("method" -> "post").action(s"/conferences/cancelSlot/${s.id}")
    									 (input.attr("type" -> "submit").value("Cancel").cls("btn btn-danger")))))
    Ok(templates.conferences.myConferencesParents(guardian, session, rows))									 
  }
  
  def cancelSlot(slotId: Long) = Authenticated { implicit req =>
    dataStore.execute { pm =>
      val maybeSlot = pm.query[Slot].filter(QSlot.candidate.id.eq(slotId)).executeOption
      (req.role, maybeSlot) match {
        case (guardian: Guardian, Some(slot)) => {
          if(slot.guardians.contains(guardian)) {
            pm.deletePersistent(slot)
            Redirect("/conferences/myConferences/" + slot.session.id).flashing("success" -> "Conference deleted.")
          } else {
            Redirect("/conferences/myConferences/" + slot.session.id).flashing("error" -> "You are not a guardian for this conference.")
          }
        }
        case (teacher: Teacher, Some(slot)) => {
          if(slot.teacher.equals(teacher)) {
            pm.deletePersistent(slot)
            Redirect("/conferences/myConferences/" + slot.session.id).flashing("success" -> "Conference deleted.")
          } else {
            Redirect("/conferences/myConferences/" + slot.session.id).flashing("error" -> "You are not a teacher for this conference.")
          }
        }
        case (_, None) => Redirect("/conferences").flashing("error" -> "Could not find that scheduled conference.")
        case (_, _) => Redirect("/conferences").flashing("error" -> "You must be a parent or teacher to delete a conference.")
      }
    }
  }
  
  
  
  //////////////////////////////////////////////////////////////Admin View////////////////////////////////////////////////////////////////////	

  class EventForm(maybeEvent: Option[Event]) extends Form {
    val name = new TextField("name") {
      override val maxLength = Some(50)
      override def initialVal = maybeEvent.map(_.name)
    }
    val isActive = new BooleanField("isActive") {
      override def initialVal = maybeEvent.map(_.isActive)
    }

    val fields = List(name, isActive)
  }

  def addEvent() = PermissionRequired(Permissions.Manage) { implicit req =>
    Ok(conferences.eventForm(Binding(new EventForm(None))))
  }

  def addEventP() = PermissionRequired(Permissions.Manage) { implicit req =>
    val form = new EventForm(None)
    Binding(form, req) match {
      case ib: InvalidBinding => Ok(conferences.eventForm(ib))
      case vb: ValidBinding => dataStore.execute { implicit pm =>
        val theName = vb.valueOf(form.name)
        val theActivation = vb.valueOf(form.isActive)
        val e = new Event(theName, theActivation)
        pm.makePersistent(e)
        Redirect(routes.Conferences.listEvents()).flashing("message" -> "Event successfully created!")
      }
    }
  }
    
  //TODO: Make sure this works
  //TODO: This should be POST
  def deleteEvent(eventId: Long) = VisitAction { implicit req =>
    dataStore.execute { implicit pm =>
      pm.query[Event].filter(QEvent.candidate.id.eq(eventId)).executeOption() match {
        case None => NotFound(templates.NotFound("No event could be found"))
        case Some(event) => {
          val sessions = pm.query[models.conferences.Session].filter(QSession.candidate.event.eq(event)).executeList()
          for (session <- sessions) {
            val slots = pm.query[Slot].filter(QSlot.candidate.session.eq(session)).executeList()
            val teacherActivations = pm.query[TeacherActivation].filter(QTeacherActivation.candidate.session.eq(session)).executeList()
            pm.deletePersistentAll(slots)
            pm.deletePersistentAll(teacherActivations)
          }
          pm.deletePersistentAll(sessions)
          val message = "Conference event '%s' was deleted.".format(event.name)
          pm.deletePersistent(event)
          Redirect(routes.Conferences.listEvents()).flashing("message" -> message)
        }
      }
    }
  }

  object SessionForm extends Form {
    val date = new DateField("date")
    val cutoff = new DateTimeField("cutoff")
    val priority = new DateTimeFieldOptional("priority")
    val startTime = new TimeField("start time")
    val endTime = new TimeField("end time")
    val fields = List(date, cutoff, priority, startTime, endTime)
  }
  
  def addSession(eventId: Long) = VisitAction { implicit req =>
    // TODO
    Ok("TODO")
  }
  
  def addSessionP(eventId: Long) = VisitAction { implicit req =>
      Binding(SessionForm, req) match {
        case ib: InvalidBinding => Ok("TODO")
        case vb: ValidBinding => dataStore.execute { implicit pm =>
          val theEvent = pm.query[Event].filter(QEvent.candidate.id.eq(eventId)).executeList()
          val theDate = vb.valueOf(SessionForm.date)
          val theCutoff = vb.valueOf(SessionForm.cutoff)
          val thePriority = vb.valueOf(SessionForm.priority)
          val theStartTime = vb.valueOf(SessionForm.startTime)
          val theEndTime = vb.valueOf(SessionForm.endTime)
          println(theDate + " " + theStartTime + " " + theEndTime)
          val s = new models.conferences.Session(theEvent(0), theDate, theCutoff, thePriority, theStartTime, theEndTime)
          pm.makePersistent(s)
          Redirect(routes.Conferences.listEvents()).flashing("message" -> "Session successfully created!")
        }
      }
  }

  def deleteSession(sessionId: Long) = VisitAction { implicit request =>
    dataStore.execute { implicit pm =>
      pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption() match {
        case None => NotFound(templates.NotFound("No session could be found"))
        case Some(session) => {
          val slots = pm.query[Slot].filter(QSlot.candidate.session.eq(session)).executeList()
          val teacherActivations = pm.query[TeacherActivation].filter(QTeacherActivation.candidate.session.eq(session)).executeList()
          pm.deletePersistentAll(slots)
          pm.deletePersistentAll(teacherActivations)
          pm.deletePersistent(session)
          Redirect(routes.Conferences.listEvents()).flashing("message" -> ("Session was deleted."))
        }
      }
    }
  }
  ////////////////////////////////////////////////////////Teacher View/////////////////////////////////////////////////////////////////

	def teacherView() = RoleMustPass((role: Role) => role.isInstanceOf[Teacher]) { implicit request =>
	  dataStore.execute { pm =>
	    val teacher = request.role.asInstanceOf[Teacher]
	  	val teacherAssignments = pm.query[TeacherAssignment].filter(QTeacherAssignment.candidate.teacher.eq(teacher)).executeList()
	  	val sections = teacherAssignments.map(tA => tA.section)
	  	val events = pm.query[Event].executeList()
	  	val sessions = pm.query[models.conferences.Session].executeList()
	  	val priorities = pm.query[PriorityScheduling].filter(QPriorityScheduling.candidate.teacher.eq(teacher)).executeList()
	  	Ok("TODO")
	  	//Ok(views.html.conferences.teachers(teacher, events, sessions, sections, priorities))
	  }
	}
	

  /*def teacherSession(sessionId: Long) = RoleMustPass(_.isInstanceOf[Teacher]) { implicit request =>
    dataStore.execute { pm =>
      val session = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption().get
      val slots = pm.query[Slot].executeList()
      val teacher = request.role.asInstanceOf[Teacher]
      val teacherAssignments = pm.query[TeacherAssignment].filter(QTeacherAssignment.candidate.teacher.eq(teacher)).executeList()
	  val sections = teacherAssignments.map(tA => tA.section)
      val cand = QTeacherActivation.candidate
      //TODO: sort slots by time
      pm.query[TeacherActivation].filter(cand.teacher.eq(teacher).and(cand.session.eq(session))).executeOption match {
        case Some(teacherActivation) => Ok(views.html.conferences.teacherSession(slots, session, sections, Some(teacherActivation)))
        case None => Ok(views.html.conferences.teacherSession(slots, session, sections, None))
      }
    }
  }*/

  /*object TeacherActivationForm extends Form {
    val slotInterval = new NumericField[Int]("Default slot interval")
    val note = new TextFieldOptional("note")

    val fields = List(slotInterval, note)
  }
  
  def activateTeacherSessionP(sessionId: Long) = VisitAction { implicit request =>  
    dataStore.execute { pm => 
        Binding(TeacherActivationForm, request) match {
          case ib: InvalidBinding => Ok(views.html.conferences.activateSession(ib, sessionId))
          case vb: ValidBinding => {
            val session = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption().get
            val currUser = request.visit.user
            val teacher = Teacher.getByUsername(currUser.get.username).get
            val slotInterval = vb.valueOf(TeacherActivationForm.slotInterval)
            val note = vb.valueOf(TeacherActivationForm.note)

            val t = new TeacherActivation(session, teacher, slotInterval, note)
            pm.makePersistent(t)
            Redirect(routes.Conferences.teacherSession(sessionId)).flashing("message" -> "Session activated")
          }
        }
    }
  }*/

  /*def deactivateTeacherSession(sessionId: Long) = VisitAction { implicit request =>
    dataStore.execute { pm =>
      val cand = QTeacherActivation.candidate()
      val session = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption().get
      val currUser = request.visit.user
      val teacher = Teacher.getByUsername(currUser.get.username).get
      pm.query[TeacherActivation].filter(cand.teacher.eq(teacher).and(cand.session.eq(session))).executeOption() match {
        case None => NotFound("Session not activated yet")
        case Some(activated) => pm.deletePersistent(activated)
      }
      Redirect(routes.Conferences.teacherSession(sessionId)).flashing("message" -> "Session deactivated")
    }
  }*/
  
  def enablePriority(studentId: Long) = RoleMustPass(_.isInstanceOf[Teacher]) { implicit request =>
  	dataStore.execute { pm =>
      val teacher = request.role.asInstanceOf[Teacher]
      // TODO: this .get could cause a problem
      val student = pm.query[Student].filter(QStudent.candidate.id.eq(studentId)).executeOption().get
      val priority = new PriorityScheduling(student, teacher)
  	  pm.makePersistent(priority)
  	  Redirect(routes.Conferences.teacherView()).flashing("message" -> ("You have enabled priority scheduling"))
  	}
  }
  
  def disablePriority(studentId: Long) = RoleMustPass(_.isInstanceOf[Teacher]) { implicit request =>
    dataStore.execute { pm =>
      val teacher = request.role.asInstanceOf[Teacher]
      // TODO: what if the studentId is invalid?
      val student = pm.query[Student].filter(QStudent.candidate.id.eq(studentId)).executeOption().get
      val cand = QPriorityScheduling.candidate
      val priority = pm.query[PriorityScheduling].filter(cand.student.eq(student).and(cand.teacher.eq(teacher))).executeOption()
      priority match {
        case None => NotFound("This student does not have priority scheduling enabled")
        case Some(priority) => pm.deletePersistent(priority)
      }
      Redirect(routes.Conferences.teacherView()).flashing("message" -> ("You have disabled priority scheduling"))
    }
  }

  /////////////////////////////////////////////////////Student View////////////////////////////////////////////////////////////////

  def classList(sessionId: Long) = VisitAction { implicit request =>
    dataStore.execute { pm =>
	  val currentUser = request.visit.user
	  val student = Student.getByUsername(currentUser.get.username).get
	  val session = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption().get
	  val slots = pm.query[Slot].filter(QSlot.candidate.session.eq(session).and(QSlot.candidate.students.contains(student))).executeList()
	  val term = Term.current
		val enrollments: List[StudentEnrollment] = {
			val sectVar = QSection.variable("sectVar")
			val cand = QStudentEnrollment.candidate()
			pm.query[StudentEnrollment].filter(cand.student.eq(student).and(cand.section.eq(sectVar)).and(sectVar.terms.contains(term))).executeList()
		}
  			val hasEnrollments = enrollments.size != 0
  			val sections: List[Section] = enrollments.map(_.section)
  			val periods: List[Period] = pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
  			val table: List[NodeSeq] = periods.map { p =>
                              val sectionThisPeriod = sections.filter(_.periods.contains(p))(0)
                              val linkNode: NodeSeq = {<a class ="btn" href={routes.Conferences.multipleTeacherHandler(sessionId, sectionThisPeriod.id).url }>Get This Conference</a>}
                              <tr>
                              <td>{ p.name }</td>
                              <td>{ Text(sectionThisPeriod.course.name) }</td>
                              <td>{ Text(sectionThisPeriod.teachers.map(_.user.shortName).mkString("; ")) }</td>
                              <td>{ linkNode }</td>
                              </tr>
                          }
  			Ok("TODO")
  			//Ok(views.html.conferences.classList(sessionId, table, hasEnrollments, slots))
    }
  }
  
  def multipleTeacherHandler(sessionId: Long, sectionId: Long)= VisitAction { implicit request =>
	dataStore.execute { pm =>
		val section = pm.query[Section].filter(QSection.candidate.id.eq(sectionId)).executeOption()
		section match {
		  case None => NotFound("Incorrect Section Id")
		  case Some(section) =>
		    val teachers = section.teachers
		    val teacherList = teachers map { teacher =>
		      						(teacher, 
		      						pm.query[TeacherActivation].filter(QTeacherActivation.candidate.teacher.eq(teacher)).executeOption())
		    }
		    if (teacherList.length == 1 && teacherList(0)._2 != None) Redirect(routes.Conferences.slotHandler(sessionId, teacherList(0)._1.id))
		    else Ok("TODO") //Ok(views.html.conferences.multipleTeacherHandler(sessionId, teacherList))
		}
	}
  }
  
  def slotHandler(sessionId: Long, teacherId: Long)= RoleMustPass(_.isInstanceOf[Student]) { implicit request =>
	dataStore.execute  {  pm =>
	    val today = LocalDate.now()
	  	val currentTime = LocalDateTime.now()
	    val student = request.role.asInstanceOf[Student]
		val session = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption().get
		val teacher = pm.query[Teacher].filter(QTeacher.candidate.id.eq(teacherId)).executeOption().get
		val cand = QSlot.candidate
		val slots = pm.query[Slot].filter(cand.students.contains(student).and(cand.session.eq(session)).and(cand.teacher.eq(teacher))).executeList()
		if (currentTime > session.cutoff) Ok("TODO") //Ok(views.html.conferences.slotView(slots, session, student.id, teacherId, currentTime))
		//else if (slots.isEmpty) Redirect(routes.Conferences.createSlot(sessionId, student.id, teacherId))
		else Ok("TODO") //Ok(views.html.conferences.slotView(slots, session, student.id, teacherId, currentTime))
		
	}
  }

  /*object SlotForm extends Form {
    val startTime = new TimeField("StartTime")
    val parentName = new TextField("Parent Name")
    val email = new EmailField("E-mail")
    //TODO Replace with phonefield
    val phone = new TextField("Phone")
    val alternatePhone = new TextFieldOptional("Alternate Phone")
    val comment = new TextFieldOptional("Comments")

    val fields = List(startTime, parentName, email, phone, alternatePhone, comment)
  }

  def createSlot(sessionId: Long, studentId: Long, teacherId: Long) = VisitAction { implicit request =>
    Ok(views.html.conferences.createSlot(Binding(SlotForm), sessionId, studentId, teacherId))
  }
  
  def createSlotP(sessionId: Long, studentId: Long, teacherId: Long) = Authenticated { implicit request =>
      Binding(SlotForm, request) match {
        case ib: InvalidBinding => Ok(views.html.conferences.createSlot(ib, sessionId, studentId, teacherId))
        case vb: ValidBinding => dataStore.execute { implicit pm =>
          val theSession = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption()
          val theTeacher = pm.query[Teacher].filter(QTeacher.candidate.id.eq(teacherId)).executeOption()
          if (theSession == None) NotFound(templates.NotFound("Invalid session ID"))
          if (theTeacher == None) NotFound(templates.NotFound("Invalid teacher ID"))
          val theStudent: Option[Student] = pm.query[Student].filter(QStudent.candidate.id.eq(studentId)).executeOption()
          if (theStudent == None) NotFound(templates.NotFound("Invalid student"))

          val theStartTime = vb.valueOf(SlotForm.startTime)
          val theParent = vb.valueOf(SlotForm.parentName)
          val theEmail = vb.valueOf(SlotForm.email)
          val thePhone = vb.valueOf(SlotForm.phone)
          val theAlternatePhone = vb.valueOf(SlotForm.alternatePhone)
          val theComment = vb.valueOf(SlotForm.comment)
          //Get slotinterval from teacher activations
          val theTeacherActivation = pm.query[TeacherActivation].filter(QTeacherActivation.candidate.teacher.eq(theTeacher.get)).executeList()
          val s = new Slot(theSession.get, theTeacher.get, theStudent.get, theStartTime, theParent, theEmail, thePhone, theAlternatePhone, theComment, theTeacherActivation(0).slotInterval)
          //Slot validating has not been tested yet
          if (s.validateSession && request.role == theStudent.get) {
            Redirect(routes.Conferences.createSlot(sessionId, studentId, teacherId)).flashing("message" -> ("You must choose a time between " + toAmericanString(theSession.get.startTime) + " and " + toAmericanString(theSession.get.endTime) + "."))
          }
          else if (s.validateSlot) {
            Redirect(routes.Conferences.createSlot(sessionId, studentId, teacherId)).flashing("message" -> "Time slot not available. Please choose another time.")
          } else {
        	pm.makePersistent(s)
        	if (request.role == theStudent) Redirect(routes.Conferences.slotHandler(sessionId, teacherId)).flashing("message" -> "Successfully created slot!")
        	else Redirect(routes.Conferences.teacherSession(sessionId)).flashing("message" -> "Successfully created slot!")
          }
        }
      }
  }*/

  def deleteSlot(slotId: Long) = VisitAction { implicit request =>
    dataStore.execute { implicit pm =>
      pm.query[Slot].filter(QSlot.candidate.id.eq(slotId)).executeOption() match {
        case None => NotFound(templates.NotFound("No slot could be found"))
        case Some(slot) => {
          val session = slot.session
          pm.deletePersistent(slot)
          Redirect(routes.Conferences.classList(session.id)).flashing("message" -> ("Slot was deleted."))
        }
      }
    }
  }
}

object Conferences {
  def timeReporter(localTime: LocalTime): String = {
    import util.Helpers.time
    time.print(localTime)
  }
  
  def getParameter(param: String, map: Map[String, Seq[String]]): String = {
    map.getOrElse(param, Nil) match {
      case x :: xs => x
      case Nil => ""
    }
  }
  
  def getParam(param: String)(implicit map: Map[String, Seq[String]]): String = getParameter(param, map)
}

/*object ConferencesMenu {
  val conferencesDefault = new MenuItem("Conferences", "menu_conferences", Some(controllers.routes.Conferences.index.toString), Nil)
  val sessions = new MenuItem("Current Sessions", "menu_sessions", Some(controllers.routes.Conferences.index.toString), Nil)
  def myConferences(session: Session) = new MenuItem("My Conferences", "menu_conferences", Some(controllers.routes.Conferences.myConferences(session.id).toString), Nil)
  def myStudents(guardian: Guardian, session: Session) = {
    val students = guardian.children.toList
    val sublist: List[MenuItem] = if(students.size == 0) List(new MenuItem("You have no students.", "menu_nostudents", None, Nil))
                  else students.map(s => { 
                    new MenuItem(s.formalName, "menu_conf"+s.formalName, 
                        Option(controllers.routes.Conferences.studentClasses(session.id, s.studentNumber).toString), Nil)
                    })
    new MenuItem("Schedule for a Student", "menu_schedulestudents", None, Nil, sublist)
  }
  
  def guardianForActiveSession(guardian: Guardian) = Session.mostCurrentSession match {
    case None => conferencesDefault
    case Some(session) => new MenuItem("Conferences", "menu_conferences", None, List(sessions, myConferences(session), myStudents(guardian, session)))
  }
  
  def teacherForActiveSession = Session.mostCurrentSession match {
    case None => conferencesDefault
    case Some(session) => new MenuItem("Conferences", "menu_conferences", None, List(sessions, myConferences(session)))
  }
  
  def forRole(maybeRole: Option[Role]): MenuItem = {
    maybeRole match {
      case Some(_: Teacher) => teacherForActiveSession
      case Some(g: Guardian) => guardianForActiveSession(g)
      case _ => conferencesDefault 
    }
  }
}*/