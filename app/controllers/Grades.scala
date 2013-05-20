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
import play.api.templates.Html
import models.grades.Turnin

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

  def assignments(id: Long) = Authenticated { implicit req =>
    val persp = req.visit.perspective.get
    persp match {
      case teacher: Teacher => assignmentsForTeachers(id, teacher)(req).asInstanceOf[PlainResult] //TODO fix this cast
      //case _:Student => assignmentsForStudents(id)
    }

  }

  def assignmentsForTeachers(id: Long, teacher: Teacher) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    Section.getById(id) match {
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
            Ok(html.grades.assignments(sect, cats, Binding(dropMenu), id))
          } else Binding(dropMenu, req) match {
            case ib: InvalidBinding => Ok(views.html.grades.assignments(sect, cats, ib, id))
            case vb: ValidBinding => {
              val TheCat: Category = vb.valueOf(dropMenu.category)
              val TheTitle: String = vb.valueOf(dropMenu.title)
              val ThePoints: Int = vb.valueOf(dropMenu.numPoints)
              val TheDueDate: java.sql.Date = vb.valueOf(dropMenu.dueDate)
              val TheLocked: java.sql.Date = vb.valueOf(dropMenu.locked)
              val assignment = new Assignment(TheTitle, ThePoints, TheDueDate, TheLocked, TheCat)
              pm.makePersistent(assignment)
              Redirect(routes.Grades.assignments(id))
            }
          }
        }
      }
    }
  }

  def deleteAssignment(id: Long, assignmentId: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    pm.query[Assignment].filter(QAssignment.candidate.id.eq(assignmentId)).executeOption() match {
      case None => NotFound(views.html.notFound("No assignment with that id."))
      case Some(assign) => {
        pm.deletePersistent(assign)
        Redirect(routes.Grades.assignments(id))
      }
    }
  }

  def home(id: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    Section.getById(id) match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        Ok(views.html.grades.home(id, sect))
      }
    }
  }

  def announcements(id: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    Section.getById(id) match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        val announcements = Announcement.getAnnouncements(sect)
        Ok(views.html.grades.announcements(id, sect, announcements))
      }
    }
  }
  
  def gradebook(id: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    Section.getById(id) match {
      case None => NotFound(views.html.notFound("No section with that id."))
      case Some(sect) => {
        val assignments = Assignment.getAssignments(sect)
        val students = sect.students
        val colHeaders = assignments.map(_.name.substring(0, 5))
        val rowHeaders = students.map(_.user.displayName)
        val grades: List[List[Double]] = {
          for (s <- students) yield assignments.map(_.getTurnin(s).map(_.points).getOrElse(0.0))
        }
        val grid = (colHeaders, rowHeaders, grades)
        Ok(views.html.grades.gradebook(grid, students, assignments, sect, id))
        
      }
    }  
  }
  
  def topRowGradebook(assignments: List[Assignment]): String = {
    val s = "[ ,"
    ("''" :: assignments.map("'"+_.name+"'")).mkString("[", ", ", "]")
  }
  
  def rowN(student: Student, assignments: List[Assignment]): String = {
    ("'"+student.user.displayName+"'" :: assignments.map(_.getTurnin(student))).mkString("[", ", ", "]")
  }

}