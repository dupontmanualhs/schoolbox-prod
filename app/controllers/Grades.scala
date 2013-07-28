package controllers

import play.api.mvc.Controller
import org.dupontmanual.forms.{ Form, Binding, ValidBinding, InvalidBinding }
import org.dupontmanual.forms.fields._
import models.grades._
import models.courses._
import models.users._
import util.Helpers.mkNodeSeq
import scala.xml.NodeSeq
import play.api.mvc.PlainResult
import scala.xml.Text
import play.api.mvc.Result
import play.api.templates.Html
import models.grades.Turnin
import scalajdo.DataStore

import controllers.users.{ Authenticated, AuthenticatedRequest, VisitAction, VisitRequest }
import config.Config
import com.google.inject.{ Inject, Singleton }

@Singleton
class Grades @Inject()(implicit config: Config) extends Controller {
  class DropMenu(catsMap: List[(String, Category)]) extends Form {
    val category = new ChoiceField("Category", catsMap)
    val title = new TextField("Title")
    val numPoints = new NumericField[Int]("Points Possible")
    val dueDate = new DateField("DueDate")
    val locked = new DateField("Hidden Until")

    val fields = List(category, title, numPoints, dueDate, locked)
  }

  def assignments(sectionId: Long) = Authenticated { implicit req =>
    req.role match {
      case teacher: Teacher => assignmentsForTeachers(sectionId, teacher)
      //case _:Student => assignmentsForStudents(id)
    }

  }

  def assignmentsForTeachers(sectionId: Long, teacher: Teacher)(implicit req: AuthenticatedRequest[_]): Result = {
    Section.getById(sectionId) match {
      case None => NotFound(templates.NotFound("No section with that id.")(req))
      case Some(sect) => {
        if (req.role.id != teacher.id || !sect.teachers.contains(teacher)) {
          NotFound(templates.NotFound("You do not have permisson to view this course."))
        } else {
          val cats = Category.forSection(sect)
          val catsMap = Category.forSection(sect).map(c => (c.name, c))
          val dropMenu = new DropMenu(catsMap)
          Ok(views.html.grades.assignments(sect, cats, Binding(dropMenu), sectionId))
        }
      }
    }
  }
  
  /*
   * TODO: add and delete assignment should be AJAX calls that don't redraw
   * the whole page
   def addAssignment(else 
    
    Binding(dropMenu, req) match {
            case ib: InvalidBinding => Ok(views.html.grades.assignments(sect, cats, ib, id))
            case vb: ValidBinding => {
              val TheCat: Category = vb.valueOf(dropMenu.category)
              val TheTitle: String = vb.valueOf(dropMenu.title)
              val ThePoints: Int = vb.valueOf(dropMenu.numPoints)
              val TheDueDate: java.sql.Date = vb.valueOf(dropMenu.dueDate)
              val TheLocked: java.sql.Date = vb.valueOf(dropMenu.locked)
              val assignment = new Assignment(TheTitle, ThePoints, TheDueDate, TheLocked, TheCat)
              DataStore.pm.makePersistent(assignment)
              Redirect(routes.Grades.assignments(id))
            }
          }
        }
      }
    }
  }

  def deleteAssignment(id: Long, assignmentId: Long) = VisitAction { implicit req =>
    DataStore.execute { pm => 
    pm.query[Assignment].filter(QAssignment.candidate.id.eq(assignmentId)).executeOption() match {
      case None => NotFound(views.html.notFound("No assignment with that id."))
      case Some(assign) => {
        pm.deletePersistent(assign)
        Redirect(routes.Grades.assignments(id))
      }
    }
    }
  }
  * 
  */

  def home(id: Long) = VisitAction { implicit req =>
    Section.getById(id) match {
      case None => NotFound(templates.NotFound("No section with that id."))
      case Some(sect) => {
        Ok(views.html.grades.home(id, sect))
      }
    }
  }

  def announcements(id: Long) = VisitAction { implicit req =>
    Section.getById(id) match {
      case None => NotFound(templates.NotFound("No section with that id."))
      case Some(sect) => {
        val announcements = Announcement.getAnnouncements(sect)
        Ok(views.html.grades.announcements(id, sect, announcements))
      }
    }
  }
  
  def gradebook(id: Long) = VisitAction { implicit req =>
    Section.getById(id) match {
      case None => NotFound(templates.NotFound("No section with that id."))
      case Some(sect) => {
        /*val assignments = Assignment.getAssignments(sect)
        val students = sect.students
        val colHeaders = assignments.map(_.name.substring(0, 5))
        val rowHeaders = students.map(_.user.displayName)
        val grades: List[List[Double]] = {
          for (s <- students) yield assignments.map(_.getTurnin(s).map(_.points).getOrElse(0.0))
        }
        val grid = (colHeaders, rowHeaders, grades)*/
        Ok(views.html.grades.gradebook(sect, id))
        //Ok(views.html.grades.gradebook(grid, students, assignments, sect, id))
      }
    }  
  }

}