package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import models.lockers._
import models.users._
import models.courses._
import forms._
import forms.fields._
import xml._
import views.html
import forms.validators.Validator
import forms.validators.ValidationError
import util.Helpers._

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
		val currUser: Option[User] = User.current
		currUser match {
			case None => NotFound(views.html.notFound("You are not logged in."))
			case Some(x) => {if(currUser.get.username == "736052") {  
			  		Ok(views.html.conferences.admin())
			  	} else if (Teacher.getByUsername(currUser.get.username)(pm).isDefined){ 
			  	  Ok(views.html.conferences.teachers(Teacher.getByUsername(currUser.get.username)))
			  	}else{ 
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
}