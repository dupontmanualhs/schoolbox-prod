package controllers

import scala.xml.{ NodeSeq, Text }
import models.users._
import models.courses._
import models.conferences._
import org.dupontmanual.forms.fields._
import org.dupontmanual.forms.validators.{ ValidationError, Validator }
import org.dupontmanual.forms.widgets.Textarea
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
import templates._
import Conferences._
import controllers.users.VisitRequest

@Singleton
class Conferences @Inject()(implicit config: Config) extends Controller with UsesDataStore {
  val dateOrdering = implicitly[Ordering[org.joda.time.ReadablePartial]]
  import dateOrdering._
  
  
  def displayStub() = VisitAction { implicit req =>
    Ok(templates.Stub())
  }

  def viewAsTeacher() = VisitAction { implicit req =>
    dataStore.execute { implicit pm =>
      val currUser: Option[User] = req.visit.user
      val events = pm.query[Event].executeList()
      val sessions = pm.query[models.conferences.Session].executeList()
      Redirect(routes.Conferences.teacherView())
    }
  }

  def index() = VisitAction { implicit req =>
    dataStore.execute { implicit pm =>
      val now = org.joda.time.DateTime.now()
      val ecand = QEvent.candidate
      val scand = QSession.candidate
      val events = pm.query[Event].filter(ecand.isActive).executeList()
      val sessions = events.map { event => 
        pm.query[Session].filter(scand.event.eq(event)).executeList
      }
      val eventsAndSessions = events.zip(sessions).toList
      Ok(templates.conferences.index(eventsAndSessions))
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
                case (_, None) => Redirect(routes.Conferences.index()).flashing(("error", "Session could not be found."))
                case (None, _) => Redirect(routes.Conferences.index()).flashing(("error", "Teacher could not be found."))
                case (Some(teacher), Some(session)) => f(guardian, student, teacher, session, req, pm)
              }
            }
            else Redirect(routes.Conferences.index).flashing(("error", "You are not a guardian of the indicated student."))
          }
          case (_, Some(student)) => { Redirect(routes.Conferences.index).flashing(("error", "You must be a guardian to use this feature."))}
          case (_, _) => { Redirect(routes.Conferences.index).flashing(("error", "Student could not be found.")) }
        }
     }
  }
  
  def activateTeacherSession(id: Long) = Authenticated { implicit req =>
    dataStore.execute { pm =>
      val maybeSession = pm.query[Session].filter(QSession.candidate.id.eq(id)).executeOption()
      (req.role, maybeSession) match {
        case (teacher: Teacher, Some(session)) => {
          val ta = new TeacherActivation(session, teacher, None)
          pm.makePersistent(ta)
          Redirect(routes.Conferences.index)
        }
        case (teacher:Teacher, _) => {
          Redirect(routes.Conferences.index).flashing("error" -> "Session not found")
        }
        case (_, _) => {
          Redirect(routes.Conferences.index).flashing("error" -> "You must be a teacher to activate this.")
        }
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
             case ib: InvalidBinding => println("This is an invalid binding"); Ok(templates.conferences.guardianSlotScheduler(ib))
             case vb: ValidBinding => {
                 val slot = vb.valueOf(slotForm.slots)
                 val phone = vb.valueOf(slotForm.phone)
                 val altphone = vb.valueOf(slotForm.altphone)
                 val comments = vb.valueOf(slotForm.comments)
                 slot.phone_=(phone)
                 slot.alternatePhone_=(altphone)
                 slot.comment_=(comments)
                 pm.makePersistent(slot)
                 Redirect("/itworked")
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
      println(ValidationError((super.validate(vb) ++ { if(vb.valueOf(slots).validateSlot) ValidationError(Nil) else ValidationError("Time already taken")}).toXML))
      ValidationError((super.validate(vb) ++ { if(vb.valueOf(slots).validateSlot) ValidationError(Nil) else ValidationError("Time already taken")}).toXML)
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
        case vb: ValidBinding => dataStore.execute { implicit pm =>
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
          Redirect(routes.Conferences.index()).flashing("message" -> message)
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
  
  def createSession(eventId: Long) = VisitAction { implicit req =>
    Ok(views.html.conferences.createSession(Binding(SessionForm), eventId))
  }
  
  def createSessionP(eventId: Long) = VisitAction { implicit req =>
      Binding(SessionForm, req) match {
        case ib: InvalidBinding => Ok(views.html.conferences.createSession(ib, eventId))
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
          Redirect(routes.Conferences.index()).flashing("message" -> "Session successfully created!")
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
          Redirect(routes.Conferences.index()).flashing("message" -> ("Session was deleted."))
        }
      }
    }
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
            Redirect(routes.Conferences.index).flashing("error" -> "You are not the guardian of the requested student.")
          }
        }
        case (guardian: Guardian, Some(_), _) => Redirect(routes.Conferences.index).flashing("error" -> "Could not find requested session")
        case (guardian: Guardian, _, _) => Redirect(routes.Conferences.index).flashing("error" -> "Could not find requested student.")
        case (_, _, _) => Redirect(routes.Conferences.index).flashing("error" -> "You must be a guardian to view a schedule in conferences.")
      }
    }
  }
  
  def studentScheduleForConferences(student: Student, session: Session)(implicit req: VisitRequest[_]) = {
    val (rows, hasEnrollments) = {
      val controller = new controllers.courses.App()
      import controller.{scheduleConstructor, intersperse}
      scheduleConstructor(student, Term.current) 
        { ls => td(intersperse(ls.map(s => a.href(s"/conferences/guardianRegister/${session.id}/${s.teachers.head.personId}/${student.studentNumber}")
        		                                               ("Schedule a Conference with this Teacher")), br())) }
    }
    Ok(templates.conferences.StudentScheduleForConferences(student, session, rows, hasEnrollments))
  }
  
  def myConferences(sessionId: Long) = Authenticated { implicit req =>
    dataStore.execute { implicit pm => 
      val maybeSession = pm.query[Session].filter(QSession.candidate.id.eq(sessionId)).executeOption()
      (req.role, maybeSession) match {
        case (_, None) => Redirect(routes.Conferences.index).flashing("error" -> "Could not find requested session")
        case (guardian: Guardian, Some(session)) => myConferencesForParents(guardian, session)
        case (teacher: Teacher, Some(session)) => Redirect(routes.Conferences.index).flashing("error" -> "Functionality not added yet")
        case (_, _) => Redirect(routes.Conferences.index).flashing("error" -> "You must be a teacher or guardian to access this page.")
      }
    }
  }
  
  def myConferencesForParents(guardian: Guardian, session: Session)
                             (implicit req: VisitRequest[_], pm: scalajdo.ScalaPersistenceManager) = {
    val slots = pm.query[Slot].filter(QSlot.candidate.guardians.contains(guardian).and(QSlot.candidate.session.eq(session))).executeList
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
  
  ////////////////////////////////////////////////////////Teacher View/////////////////////////////////////////////////////////////////

	def teacherView() = RoleMustPass((role: Role) => role.isInstanceOf[Teacher]) { implicit request =>
	  dataStore.execute { pm =>
	    val teacher = request.role.asInstanceOf[Teacher]
	  	val teacherAssignments = pm.query[TeacherAssignment].filter(QTeacherAssignment.candidate.teacher.eq(teacher)).executeList()
	  	val sections = teacherAssignments.map(tA => tA.section)
	  	val events = pm.query[Event].executeList()
	  	val sessions = pm.query[models.conferences.Session].executeList()
	  	val priorities = pm.query[PriorityScheduling].filter(QPriorityScheduling.candidate.teacher.eq(teacher)).executeList()
	  	Ok(views.html.conferences.teachers(teacher, events, sessions, sections, priorities))
	  }
	}
	

  def teacherSession(sessionId: Long) = RoleMustPass(_.isInstanceOf[Teacher]) { implicit request =>
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
  }

  object TeacherActivationForm extends Form {
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
  }

  def deactivateTeacherSession(sessionId: Long) = VisitAction { implicit request =>
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
  }
  
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
  			Ok(views.html.conferences.classList(sessionId, table, hasEnrollments, slots))
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
		    else Ok(views.html.conferences.multipleTeacherHandler(sessionId, teacherList))
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
		if (currentTime > session.cutoff) Ok(views.html.conferences.slotView(slots, session, student.id, teacherId, currentTime))
		//else if (slots.isEmpty) Redirect(routes.Conferences.createSlot(sessionId, student.id, teacherId))
		else Ok(views.html.conferences.slotView(slots, session, student.id, teacherId, currentTime))
		
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
  def timeReporter(time: LocalTime): String = {
    if(time.getHourOfDay() >= 12) time.toString("h:mm") + " PM"
    else if(time.getHourOfDay() == 0) "12:" + time.toString("mm") + " AM"
    else time.toString("h:mm") + " AM"
  }
  
  def getParameter(param: String, map: Map[String, Seq[String]]): String = {
    map.getOrElse(param, Nil) match {
      case x :: xs => x
      case Nil => ""
    }
  }
  
  def getParam(param: String)(implicit map: Map[String, Seq[String]]): String = getParameter(param, map)
}
