package controllers

import users.VisitAction
import models.users._
import models.courses._
import models.conferences._
import forms.fields._
import forms.validators.{ ValidationError, Validator }
import util.Helpers._
import java.sql.Date
import java.sql.Time
import scalajdo.DataStore
import forms.{ Form, Binding, ValidBinding, InvalidBinding }
import play.api.mvc.Controller
import config.Config
import com.google.inject.{ Inject, Singleton }


@Singleton
class Conferences @Inject()(implicit config: Config) extends Controller {
  def displayStub() = VisitAction { implicit req =>
    Ok(templates.Stub(templates.Main))
  }

  def viewAsTeacher() = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      val currUser: Option[User] = req.visit.user
      val events = pm.query[Event].executeList()
      val sessions = pm.query[models.conferences.Session].executeList()
      Ok(views.html.conferences.teachers(Teacher.getByUsername(currUser.get.username), events, sessions))
    }
  }

  def index() = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      val events = pm.query[Event].executeList()
      val sessions = pm.query[models.conferences.Session].executeList()
      val currUser: Option[User] = req.visit.user
      currUser match {
        case None => {
          req.visit.redirectUrl = routes.Conferences.index
          pm.makePersistent(req.visit)
          Redirect(controllers.users.routes.App.login()).flashing("error" -> "You are not logged in.")
        }
        case Some(x) => {
          if (currUser.get.username == "736052" || currUser.get.username == "todd") {
            Ok(views.html.conferences.admin(events, sessions))
          } else if (Teacher.getByUsername(currUser.get.username).isDefined) {
            Ok(views.html.conferences.teachers(Teacher.getByUsername(currUser.get.username), events, sessions))
          } else {
            val currentUser = req.visit.user
            val isStudent = currentUser.isDefined && Student.getByUsername(currentUser.get.username).isDefined
            if (!isStudent) {
              NotFound(templates.NotFound(templates.Main, "Must be logged-in to get a conference"))
            } else {
              Ok(views.html.conferences.index(events, sessions))
            }
          }
        }
      }
    }
  }

  //////////////////////////////////////////////////////////////Admin View////////////////////////////////////////////////////////////////////	

  object EventForm extends Form {
    val name = new TextField("name") {
      override val maxLength = Some(50)
    }
    val isActive = new ChoiceField[Boolean]("active", List(("Yes", true), ("No", true)))

    val fields = List(name, isActive)
  }

  def createEvent() = VisitAction { implicit req =>
    Ok(views.html.conferences.createEvent(Binding(EventForm)))
  }
  
  def createEventP() = VisitAction { implicit req =>
      Binding(EventForm, req) match {
        case ib: InvalidBinding => Ok(views.html.conferences.createEvent(ib))
        case vb: ValidBinding => DataStore.execute { implicit pm =>
          val theName = vb.valueOf(EventForm.name)
          val theActivation = vb.valueOf(EventForm.isActive)
          val e = new Event(theName, theActivation)
          pm.makePersistent(e)
          Redirect(routes.Conferences.index()).flashing("message" -> "Event successfully created!")
        }
      }
  }
  //TODO: Make sure this works
  //TODO: This should be POST
  def deleteEvent(eventId: Long) = VisitAction { implicit req =>
    DataStore.execute { implicit pm =>
      pm.query[Event].filter(QEvent.candidate.id.eq(eventId)).executeOption() match {
        case None => NotFound(templates.NotFound(templates.Main, "No event could be found"))
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
          Redirect(routes.Conferences.index()).flashing("message" -> message)
        }
      }
    }
  }

  object SessionForm extends Form {
    val date = new DateField("date")
    val cutoff = new TimestampField("cutoff")
    //val priority = new TimestampFieldOptional("priority")
    val startTime = new TimeField("start time")
    val endTime = new TimeField("end time")
    val fields = List(date, cutoff, /*priority,*/ startTime, endTime)
  }

  def createSession(eventId: Long) = VisitAction { implicit req =>
    Ok(views.html.conferences.createSession(Binding(SessionForm), eventId))
  }
  
  def createSessionP(eventId: Long) = VisitAction { implicit req =>
      Binding(SessionForm, req) match {
        case ib: InvalidBinding => Ok(views.html.conferences.createSession(ib, eventId))
        case vb: ValidBinding => DataStore.execute { implicit pm =>
          val theEvent = pm.query[Event].filter(QEvent.candidate.id.eq(eventId)).executeList()
          val theDate = vb.valueOf(SessionForm.date)
          val theCutoff = vb.valueOf(SessionForm.cutoff)
          val thePriority = None
          //val thePriority = vb.valueOf(SessionForm.priority)
          val theStartTime = vb.valueOf(SessionForm.startTime)
          val theEndTime = vb.valueOf(SessionForm.endTime)
          println(theDate + " " + theStartTime + " " + theEndTime)
          val s = new models.conferences.Session(theEvent(0), theDate, theCutoff, thePriority, theStartTime, theEndTime)
          pm.makePersistent(s)
          Redirect(routes.Conferences.index()).flashing("message" -> "Session successfully created!")
        }
      }
  }

  def deleteSession(sessionId: Long) = VisitAction { implicit request =>
    DataStore.execute { implicit pm =>
      pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption() match {
        case None => NotFound(templates.NotFound(templates.Main, "No session could be found"))
        case Some(session) => {
          val slots = pm.query[Slot].filter(QSlot.candidate.session.eq(session)).executeList()
          for (slot <- slots) pm.deletePersistent(slot)
          pm.deletePersistent(session)
          Redirect(routes.Conferences.index()).flashing("message" -> ("Session was deleted."))
        }
      }
    }
  }

  ////////////////////////////////////////////////////////Teacher View////////////////////////////////////////////////////////////////

  /* TODO: Get a list of students that a teacher has
	def teacherView(events: List[Event], sessions: List[Session]): DbAction { implicit request =>
	  implicit val pm: ScalaPersistenceManager = request.pm
	  val currUser = User.current
	  val teacher = Teacher.getByUsername(currUser.get.username).get
	}
	*/

  def teacherSession(sessionId: Long) = VisitAction { implicit request =>
    DataStore.execute { pm =>
      val session = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption().get
      val slots = pm.query[Slot].executeList()
      val currUser = request.visit.user
      val teacher = Teacher.getByUsername(currUser.get.username).get
      val cand = QTeacherActivation.candidate
      pm.query[TeacherActivation].filter(cand.teacher.eq(teacher).and(cand.session.eq(session))).executeOption match {
        case Some(teacherActivation) => Ok(views.html.conferences.teacherSession(slots, session, Some(teacherActivation)))
        case None => Ok(views.html.conferences.teacherSession(slots, session, None))
      }
    }
  }

  object TeacherActivationForm extends Form {
    val slotInterval = new NumericField[Int]("Default slot interval")
    val note = new TextFieldOptional("note")

    val fields = List(slotInterval, note)
  }

  def activateTeacherSession(sessionId: Long) = VisitAction { implicit request =>
    Ok(views.html.conferences.activateSession((Binding(TeacherActivationForm)), sessionId))
  }
  
  def activateTeacherSessionP(sessionId: Long) = VisitAction { implicit request =>  
    DataStore.execute { pm => 
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
  }

  def deactivateTeacherSession(sessionId: Long) = VisitAction { implicit request =>
    DataStore.execute { pm =>
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
  }

  /////////////////////////////////////////////////////Student View////////////////////////////////////////////////////////////////

  def classList(sessionId: Long) = VisitAction { implicit request =>
    DataStore.execute { pm =>
      val currentUser = request.visit.user
      val Some(student) = Student.getByUsername(currentUser.get.username)
      val term = Term.current
      val enrollments: List[StudentEnrollment] = {
        val sectVar = QSection.variable("sectVar")
        val cand = QStudentEnrollment.candidate()
        pm.query[StudentEnrollment].filter(cand.student.eq(student).and(cand.section.eq(sectVar)).and(sectVar.terms.contains(term))).executeList()
      }
      val hasEnrollments = enrollments.size != 0
      val sections: List[Section] = enrollments.map(_.section)
      val periods: List[Period] = pm.query[Period].orderBy(QPeriod.candidate.order.asc).executeList()
      Ok(views.html.conferences.classList(sessionId, periods, sections, hasEnrollments))
    }
  }

  object SlotForm extends Form {
    val startTime = new TimeField("StartTime")
    val parentName = new TextField("Parent Name")
    val email = new EmailField("E-mail")
    //TODO Replace with phonefield
    val phone = new TextField("Phone")
    val alternatePhone = new TextFieldOptional("Alternate Phone")
    val comment = new TextFieldOptional("Comments")

    val fields = List(startTime, parentName, email, phone, alternatePhone, comment)
  }

  def createSlot(sessionId: Long, teacherId: Long) = VisitAction { implicit request =>
    Ok(views.html.conferences.createSlot(Binding(SlotForm), sessionId, teacherId))
  }
  
  def createSlotP(sessionId: Long, teacherId: Long) = VisitAction { implicit request =>
      Binding(SlotForm, request) match {
        case ib: InvalidBinding => Ok(views.html.conferences.createSlot(ib, sessionId, teacherId))
        case vb: ValidBinding => DataStore.execute { implicit pm =>
          val theSession = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption()
          val theTeacher = pm.query[Teacher].filter(QTeacher.candidate.id.eq(teacherId)).executeOption()
          if (theSession == None) NotFound(templates.NotFound(templates.Main, "Invalid session ID"))
          if (theTeacher == None) NotFound(templates.NotFound(templates.Main, "Invalid teacher ID"))
          val theStudent: Option[Student] = request.visit.user.flatMap((u: User) => Student.getByUsername(u.username))
          if (theStudent == None) NotFound(templates.NotFound(templates.Main, "Invalid student"))

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
          if (validateSlot(s)) {
            Redirect(routes.Conferences.createSlot(sessionId, teacherId)).flashing("message" -> "Time slot not available. Please choose another time.")
          }
          pm.makePersistent(s)
          Redirect(routes.Conferences.index()).flashing("message" -> "Successfully created slot!")
        }
      }
  }

  def deleteSlot(slotId: Long) = VisitAction { implicit request =>
    DataStore.execute { implicit pm =>
      pm.query[Slot].filter(QSlot.candidate.id.eq(slotId)).executeOption() match {
        case None => NotFound(templates.NotFound(templates.Main, "No slot could be found"))
        case Some(slot) => {
          pm.deletePersistent(slot)
          Redirect(routes.Conferences.index()).flashing("message" -> ("Slot was deleted."))
        }
      }
    }
  }

  //TODO: Write a method that checks if there already exists a slot within the same time-period
  //Not tested yet
  def validateSlot(slot: Slot): Boolean = {
    val startTime = slot.startTime
    val endTime = slot.endTime
    DataStore.execute { implicit pm =>
      val slots = pm.query[Slot].executeList()
      for (slot <- slots) {
        if ((startTime.compareTo(slot.startTime) >= 0) && (startTime.compareTo(slot.endTime) < 0)) true
        if ((endTime.compareTo(slot.startTime) > 0) && (endTime.compareTo(slot.endTime) <= 0)) true
      }
      false
    }
  }
}