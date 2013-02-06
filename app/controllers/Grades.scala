package controllers

import play.api.mvc.Controller
import models.grades._
import models.courses._
import models.users._
import util.DbAction
import util.Helpers.mkNodeSeq
import views.html
import scala.xml.NodeSeq
import play.api.mvc.PlainResult
import util.DbRequest
import util.ScalaPersistenceManager
import scala.xml.Text

object Grades extends Controller {
  def assignments(sectionId: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    val cand = QSection.candidate
    pm.query[Section].filter(cand.id.eq(sectionId)).executeOption() match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        val cats = Category.forSection(sect)
        Ok(html.grades.assignments(sect, cats))
      }
    }
  }
  
  
    
  def home = DbAction { implicit req => 
    Ok(views.html.grades.home())}
  
  def announcements = DbAction { implicit req => 
    Ok(views.html.grades.announcements())}
    
  
  
 }