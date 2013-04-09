package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import models.lockers._
import models.users._
import models.courses._
import models.conferences._
import forms._
import forms.fields._
import xml._
import views.html
import forms.validators.Validator
import forms.validators.ValidationError
import util.Helpers._
import java.sql.Date
import java.sql.Time

object Conferences extends Controller {
	def displayStub() = DbAction {implicit req =>
	  Ok(views.html.stub())	  
	}
	
	def viewAsTeacher() = DbAction { implicit req =>
	  implicit val pm: ScalaPersistenceManager = req.pm
	  val currUser: Option[User] = User.current
	  Ok(views.html.conferences.teachers(Teacher.getByUsername(currUser.get.username)))
	}
	
	def index() = DbAction { implicit req =>
	    implicit val pm: ScalaPersistenceManager = req.pm
	    val events = pm.query[Event].executeList()
	    val sessions = pm.query[models.conferences.Session].executeList()
		val currUser: Option[User] = User.current
		currUser match {
			case None => Redirect(routes.Users.login).flashing("error" -> "You are not logged in.")
			case Some(x) => {if(currUser.get.username == "736052" || currUser.get.username == "todd") {  
			  		Ok(views.html.conferences.admin(events, sessions))
			  	} else if (Teacher.getByUsername(currUser.get.username)(pm).isDefined){ 
			  	  Ok(views.html.conferences.teachers(Teacher.getByUsername(currUser.get.username)))
			  	}else { 
			  		val currentUser = User.current
			  		val isStudent = currentUser.isDefined && Student.getByUsername(currentUser.get.username)(pm).isDefined
			  		if(!isStudent) {
			  			NotFound(views.html.notFound("Must be logged-in to get a conference"))
			  		} else {
			  			val Some(student) = Student.getByUsername(currentUser.get.username)(pm)
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
			  				val sectionsThisPeriod = sections.filter(_.periods.contains(p))
			  				val linkNode: NodeSeq = {<a class ="btn" href={ controllers.routes.Application.stub().url }>Get This Conference</a>}
			  				<tr>
			  				<td>{ p.name }</td>
			  				<td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.course.name)), <br />) }</td>
			  				<td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.teachers.map(_.user.shortName).mkString("; "))), <br />) }</td>
			  				<td>{ linkNode }</td>
			  				</tr>
			  			}
			  			Ok(views.html.conferences.index(student, table, hasEnrollments))
			  		} 
			  	}
			}
		}
	}
	
	object EventForm extends Form {
	  val name = new TextField("name"){
	    override val maxLength = Some(50)
	  }
	  //Doesn't work, change it
	  val isActive = new ChoiceField[Boolean]("active", List(("Yes", true), ("No", true)))
	  
	  val fields = List(name, isActive)
	}
	
	def createEvent = DbAction	{ implicit request =>
	  implicit val pm: ScalaPersistenceManager = request.pm
	  if (request.method == "GET") Ok(views.html.conferences.createEvent(Binding(EventForm)))
	  else {
	    Binding(EventForm, request) match {
	      case ib: InvalidBinding => Ok(views.html.conferences.createEvent(ib))
	      case vb: ValidBinding => {
	        val theName = vb.valueOf(EventForm.name)
	        val theActivation = vb.valueOf(EventForm.isActive)
	        val e = new Event(theName, theActivation)
	        pm.makePersistent(e)
	        Redirect(routes.Conferences.index()).flashing("message" -> "Event successfully created!")
	      }
	    }
	  }
	}
	
	def deleteEvent(eventId: Long) = DbAction	{ implicit request =>
	  implicit val pm: ScalaPersistenceManager = request.pm
	  pm.query[Event].filter(QEvent.candidate.id.eq(eventId)).executeOption() match {
	    case None => NotFound(views.html.notFound("No event could be found"))
	    case Some(event) => {
	      val sessions = pm.query[models.conferences.Session].filter(QSession.candidate.event.eq(event)).executeList()
	      for (session <- sessions) deleteSession(session.id)
	      pm.deletePersistent(event)
	      Redirect(routes.Conferences.index()).flashing("message" -> ("\"" + event.name + "\" was deleted."))
	    } 
	  }
	}
	
	object SessionForm extends Form  {
	  val date = new DateField("date")
	  val cutoff = new TimestampField("cutoff")
	  //val priority = new TimestampFieldOptional("priority")
	  val startTime = new TimeField("start time")
	  val endTime = new TimeField("end time")
	  val slotInterval = new ChoiceField[Int]("slot intervals", List(("5", 5), ("10", 10), ("15", 15), ("20", 20), ("25", 25), ("30", 30)))
	  val fields = List(date, cutoff, /*priority,*/ startTime, endTime, slotInterval)
	}
	
	def createSession(eventId: Long) = DbAction { implicit request =>
	  implicit val pm: ScalaPersistenceManager = request.pm
	  if (request.method == "GET") Ok(views.html.conferences.createSession(Binding(SessionForm), eventId))
	  else {
	    Binding(SessionForm, request) match {
	      case ib: InvalidBinding => Ok(views.html.conferences.createSession(ib, eventId))
	      case vb: ValidBinding => {
	        val theEvent =	pm.query[Event].filter(QEvent.candidate.id.eq(eventId)).executeList()
	        val theDate = vb.valueOf(SessionForm.date)
	        val theCutoff = vb.valueOf(SessionForm.cutoff)
	        val thePriority = None
	        //val thePriority = vb.valueOf(SessionForm.priority)
	        val theStartTime = vb.valueOf(SessionForm.startTime)
	        val theEndTime = vb.valueOf(SessionForm.endTime)
	        val theSlotInterval = vb.valueOf(SessionForm.slotInterval)
	        val s = new models.conferences.Session(theEvent(0) , theDate, theCutoff, thePriority, theStartTime, theEndTime, theSlotInterval)
	        pm.makePersistent(s)
	        Redirect(routes.Conferences.index()).flashing("message" -> "Session successfully created!")
	      }
	    }
	  }
	}
	
	def deleteSession(sessionId: Long) = DbAction { implicit request =>
	  implicit val pm: ScalaPersistenceManager = request.pm
	  pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption() match {
	    case None => NotFound(views.html.notFound("No session could be found"))
	    case Some(session) => {
	      val slots = pm.query[Slot].filter(QSlot.candidate.session.eq(session)).executeList()
	      for (slot <- slots) deleteSlot(slot.id)
	      pm.deletePersistent(session)
	      Redirect(routes.Conferences.index()).flashing("message" -> ("Session was deleted."))
	    } 
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
	
	def createSlot(sessionId: Long, teacherId: Long) = DbAction { implicit request =>
	  implicit val pm: ScalaPersistenceManager = request.pm
	  if (request.method == "GET") Ok(views.html.conferences.createSlot(Binding(SlotForm), sessionId, teacherId))
	  else {
	    Binding(SlotForm, request) match {
	      case ib: InvalidBinding => Ok(views.html.conferences.createSlot(ib, sessionId, teacherId))
	      case vb: ValidBinding => {
	        val theSession = pm.query[models.conferences.Session].filter(QSession.candidate.id.eq(sessionId)).executeOption()
	        val theTeacher = pm.query[Teacher].filter(QTeacher.candidate.id.eq(teacherId)).executeOption()
	        if (theSession == None) NotFound(views.html.notFound("Invalid session ID"))
	        if (theTeacher == None) NotFound(views.html.notFound("Invalid teacher ID"))
	        val theStudent = Student.getByUsername(User.current.get.username)(pm)
	        if (theStudent == None) NotFound(views.html.notFound("Invalid student"))
	        
	        val theStartTime = vb.valueOf(SlotForm.startTime)
	        val theParent = vb.valueOf(SlotForm.parentName)
	        val theEmail = vb.valueOf(SlotForm.email)
	        val thePhone = vb.valueOf(SlotForm.phone)
	        val theAlternatePhone = vb.valueOf(SlotForm.alternatePhone)
	        val theComment = vb.valueOf(SlotForm.comment)
	        val s = new Slot(theSession.get, theTeacher.get, theStudent.get, theStartTime, theParent, theEmail, thePhone, theAlternatePhone, theComment)
	        pm.makePersistent(s)
	        Redirect(routes.Conferences.index()).flashing("message" -> "Successfully created slot!")
	      }
	    }
	  }
	}
	
	def deleteSlot(slotId: Long) = DbAction { implicit request =>
	  implicit val pm: ScalaPersistenceManager = request.pm
	  pm.query[Slot].filter(QSlot.candidate.id.eq(slotId)).executeOption() match {
	    case None => NotFound(views.html.notFound("No slot could be found"))
	    case Some(slot) => {
	      pm.deletePersistent(slot)
	      Redirect(routes.Conferences.index()).flashing("message" -> ("Slot was deleted."))
	    } 
	  }
	}
	
	
	  //TODO: Write a method that checks if there already exists a slot within the same time-period
	
}