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
	def index() = DbAction { implicit req =>
	    implicit val pm: ScalaPersistenceManager = req.pm
		val currUser: Option[User] = User.current
		currUser match {
			case None => NotFound(views.html.notFound("You are not logged in."))
			case Some(x) => {if(currUser.get.username == "736052") {  
			  		Ok(views.html.conferences.admin())
			  	} else if (Teacher.getByUsername(currUser.get.username)(pm).isDefined){ 
			  	  Ok(views.html.conferences.teachers())
			  	}else{ Ok(views.html.conferences.index()) }
			}
		}
	}
}