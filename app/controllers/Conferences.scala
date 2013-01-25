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
		Ok(views.html.conferences.index())
	}
}