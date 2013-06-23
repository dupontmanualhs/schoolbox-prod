package controllers

import play.api._
import play.api.mvc._
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
import util.VisitAction
import scalajdo.DataStore
import util.Authenticated

object Lockers extends Controller {

  /**
   * Regex: /lockers/myLocker
   *
   *  Presents a page displaying the information for a student's current locker,
   *  or tells them they have no locker.
   */
  def getMyLocker() = Authenticated { implicit req =>
    req.perspective match {
      case teacher: Teacher => NotFound(views.html.notFound("Teachers do not have lockers."))
      case student: Student => Locker.getByStudent(student) match {
        case None => NotFound(views.html.notFound("You do not have a locker."))
        case Some(l) => Ok(views.html.lockers.getLocker(l))
      }
    }
  }

  /**
   * Regex: /lockers/locker/:num
   *
   *  Presents a page displaying the information for the locker with given number, and redirects
   *  if none exist.
   */
  def getLocker(num: Int) = Authenticated { implicit req =>
    Locker.getByNumber(num) match {
      case None => NotFound(views.html.notFound("No locker exists with this ID."))
      case Some(locker) => Ok(views.html.lockers.getLocker(locker))
    }
  }

  def claimLocker(num: Int) = Authenticated { implicit req =>
    Locker.getByNumber(num) match {
      case None => Ok(views.html.notFound(s"There is no locker with number $num"))
      case Some(locker) => req.perspective match {
        case student: Student =>
          val oldLocker: Option[Locker] = Locker.getByStudent(student)
          if (locker.taken) Ok(views.html.notFound("This locker was taken."))
          else DataStore.execute { pm =>
            locker.student = student
            locker.taken = true
            pm.makePersistent(locker)
            oldLocker.foreach { ol =>
              ol.student = None
              ol.taken = false
              pm.makePersistent(ol)
            }
            Redirect(routes.Lockers.getMyLocker()).flashing("message" -> "You have successfully changed lockers.")
          }
        case _ => NotFound(views.html.notFound("Only students have lockers."))
      }
    }
  }

  object NumberForm extends Form {
    val number: NumericField[Int] = new NumericField[Int]("number")

    def fields = List(number)

    override def validate(vb: ValidBinding): ValidationError = {
      Locker.getByNumber(vb.valueOf(number)) match {
        case None => ValidationError("Invalid Locker Number")
        case Some(l) => ValidationError(Nil)
      }
    }
  }

  /**
   * Regex: /lockers/lockerByNumber
   *
   *  Presents a form for users to search for a locker based on it's number.
   *  Also handles the post request for the form.
   */
  def lockerByNumber() = VisitAction { implicit req =>
    Ok(views.html.lockers.lockerByNumber(Binding(NumberForm)))
  }

  def lockerByNumberP() = VisitAction { implicit req =>
    Binding(NumberForm, req) match {
      case ib: InvalidBinding => Ok(views.html.lockers.lockerByNumber(ib))
      case vb: ValidBinding => Redirect(routes.Lockers.getLocker(vb.valueOf(NumberForm.number)))
    }
  }

  // TODO: this form should get built by querying the db for options
  object LockerForm extends Form {
    val floor: ChoiceField[Int] = new ChoiceField("floor", List(("1", 1), ("2", 2), ("3", 3)))
    val hall: ChoiceField[String] = new ChoiceField("hall", List(("Southeast", "SE"), ("Southwest", "SW"),
      ("Northeast", "NE"), ("Northwest", "NW"),
      ("Center East", "CE"), ("Center West", "CW"),
      ("Manual Annex", "ANNEX")))
    val available: ChoiceField[Boolean] = new ChoiceField("available", List(("Available and Taken Lockers", false), ("Available Lockers Only", true)))

    val fields = List(floor, hall, available)
  }

  /**
   * Regex: /lockers/lockerSearch
   *
   *  Presents a form for students to look for lockers based on multiple requirements.
   *  Handles the post request for the form as well.
   */
  def lockerSearch = VisitAction { implicit req =>
    Ok(views.html.lockers.lockerSearch(Binding(LockerForm)))
  }

  def lockerSearchP() = VisitAction { implicit req =>
    Binding(LockerForm, req) match {
      case ib: InvalidBinding => Ok(views.html.lockers.lockerSearch(ib))
      case vb: ValidBinding => {
        val requestedLocation = LockerLocation(vb.valueOf(LockerForm.floor), vb.valueOf(LockerForm.hall))
        val matcher = (l: Locker) => l.matchingLocation(requestedLocation)
        // TODO: we might want to do a query, so this happens in the db
        val availabilityList = if (vb.valueOf(LockerForm.available)) Locker.availableLockers() else Locker.allLockers()
        val finalList = availabilityList.filter(matcher)
        Ok(views.html.lockers.lockerList(finalList))
      }
    }
  }

  /**
   * Regex: /lockers/lockerByRoom/:room
   *
   *  Presents a list of lockers that are in the same hall as a room
   *  with the given name.
   */
  def lockerByRoom(room: String) = VisitAction { implicit req =>
    DataStore.execute { pm =>
      val roomLocation = RoomLocation.makeRoomLoc(room)
      val matchingLockerLocation = roomLocation.toLockerLocation
      val matcher: Locker => Boolean = (l: Locker) => l.matchingLocation(matchingLockerLocation)
      val resultLocker = Locker.allLockers().filter(matcher)
      Ok(views.html.lockers.lockerList(resultLocker))
    }
  }

  /**
   * Regex: /lockers/schedule
   *
   *  Presents a page that displays the schedule for a student, with an option
   *  to find lockers near each class.
   */
  def schedule = VisitAction { implicit req =>
    DataStore.execute { pm =>
      val currentUser = User.current
      val isStudent = currentUser.isDefined && Student.getByUsername(currentUser.get.username).isDefined
      if (!isStudent) {
        NotFound(views.html.notFound("Must be logged-in student to select lockers."))
      } else {
        val Some(student) = Student.getByUsername(currentUser.get.username)
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
          val roomName = sectionsThisPeriod match {
            case s :: list => s.room.name
            case _ => "0"
          }
          val linkNode: NodeSeq = { <a class="btn" href={ controllers.routes.Lockers.lockerByRoom(roomName).url }>Lockers Near Here</a> }
          <tr>
            <td>{ p.name }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.course.name)), <br/>) }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.teachers.map(_.user.shortName).mkString("; "))), <br/>) }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.room.name)), <br/>) }</td>
            <td>{ linkNode }</td>
          </tr>
        }
        Ok(views.html.lockers.schedule(student, table, hasEnrollments))
      }
    }
  }
}