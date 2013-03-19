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
import util.Authenticated
import play.api.mvc.Result

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

  def assignments(sectionId: String) = Authenticated { implicit req =>
    val persp = req.visit.perspective.get
    persp match {
      case teacher: Teacher => assignmentsForTeachers(sectionId, teacher)(req).asInstanceOf[PlainResult] //TODO fix this cast
      //case _:Student => assignmentsForStudents(sectionId)
    }

  }

  def assignmentsForTeachers(sectionId: String, teacher: Teacher) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    Section.getBySectionId(sectionId) match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        if (!sect.teachers.contains(teacher)) {
          println(sect.teachers.head.toString)
          println()
          println(req.visit.perspective.get)
          NotFound(views.html.notFound("You do not have permisson to view this course."))
        } else {
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
  }

  def deleteAssignment(sectionId: String, assignmentId: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    pm.query[Assignment].filter(QAssignment.candidate.id.eq(assignmentId)).executeOption() match {
      case None => NotFound(views.html.notFound("No assignment with that id."))
      case Some(assign) => {
        pm.deletePersistent(assign)
        Redirect(routes.Grades.assignments(sectionId))
      }
    }
  }

  def home(sectionId: String) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    Section.getBySectionId(sectionId) match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        Ok(views.html.grades.home(sectionId, sect))
      }
    }
  }

  def announcements(sectionId: String) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    Section.getBySectionId(sectionId) match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        Ok(views.html.grades.announcements(sectionId, sect))
      }
    }
  }

}