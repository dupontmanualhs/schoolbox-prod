package controllers

import play.api.mvc.Controller
import forms._
import forms.fields._
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

  class DropMenu(catsMap: List[(String, Category)]) extends Form {
    val category = new ChoiceField("Category", catsMap)
    val title = new TextField("Title")
    val numPoints = new forms.fields.NumericField[Int]("Points Possible")
    val dueDate = new DateField("DueDate")
    val locked = new DateField("Hidden Until")
    
    override val cancelTo = "url"
    
    val fields = List(category, title, numPoints, dueDate, locked)
  }

  def assignments(sectionId: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    val cand = QSection.candidate
    pm.query[Section].filter(cand.id.eq(sectionId)).executeOption() match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        val cats = Category.forSection(sect)
        val catsMap = Category.forSection(sect).map(c => (c.name, c))
        val dropMenu = new DropMenu(catsMap)
        if (req.method == "GET") {
          Ok(html.grades.assignments(sect, cats, Binding(dropMenu), sectionId))
        } else Binding(dropMenu, req) match {
          case ib: InvalidBinding => Ok(views.html.grades.assignments(sect, cats, ib, sectionId))
          case vb: ValidBinding => {
            val TheCat: Category = vb.valueOf(dropMenu.category)
            val TheTitle: String = vb.valueOf(dropMenu.title)
            val ThePoints: Int = vb.valueOf(dropMenu.numPoints)
            val TheDueDate: java.sql.Date = vb.valueOf(dropMenu.dueDate)
            val TheLocked: java.sql.Date = vb.valueOf(dropMenu.locked)
            val assignment = new Assignment(TheTitle, ThePoints, TheDueDate, TheLocked, TheCat)
            pm.makePersistent(assignment)
            Redirect(routes.Grades.assignments(sectionId))
          }
        }
      }
    }
  }
  
  def deleteAssignment(sectionId: Long, assignmentId: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    pm.query[Assignment].filter(QAssignment.candidate.id.eq(assignmentId)).executeOption() match {
      case None => NotFound(views.html.notFound("No assignment with that id."))
      case Some(assign) => {
        pm.deletePersistent(assign)
         Redirect(routes.Grades.assignments(sectionId))
        //TODO
      }
    }
  }

  def home(sectionId: Long) = DbAction { implicit req => 
    implicit val pm: ScalaPersistenceManager = req.pm
    val cand = QSection.candidate
    pm.query[Section].filter(cand.id.eq(sectionId)).executeOption() match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
    	 Ok(views.html.grades.home(sectionId, sect))
      }
    }
  }

  def announcements(sectionId: Long) = DbAction { implicit req => 
    implicit val pm: ScalaPersistenceManager = req.pm
    val cand = QSection.candidate
    pm.query[Section].filter(cand.id.eq(sectionId)).executeOption() match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
    	 Ok(views.html.grades.announcements(sectionId, sect))
      }
    }
  }

}