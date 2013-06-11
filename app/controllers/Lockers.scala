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

object Lockers extends Controller {
   
  def getMyLocker() = VisitAction { implicit req =>
  	DataStore.execute { pm => 
    val currentUser: Option[User] = User.current
  	if(currentUser.isDefined) {
      if (Teacher.getByUsername(currentUser.get.username).isDefined) {
        NotFound(views.html.notFound("Teachers do not have lockers."))
      } else {
        val Some(maybeStudent) = Student.getByUsername(currentUser.get.username)
        val maybeLocker: Option[Locker] = Locker.getByStudent(maybeStudent)
        maybeLocker match {
          case None => NotFound(views.html.notFound("You do not have a locker."))
          case Some(l) => Ok(views.html.lockers.getLocker(l))
        }
      }
    } else {
      val visit: Visit = Visit.getFromRequest(req)
      visit.redirectUrl = controllers.routes.Lockers.getMyLocker()
      pm.makePersistent(visit)
      Redirect(routes.Users.login()).flashing("error" -> "You are not logged in.")
    }
  	}
  }
  
  def getLocker(num: Int) = VisitAction { implicit req =>
    DataStore.execute { pm => 
    val maybeLocker = Locker.getByNumber(num)
    maybeLocker match {
      case None => NotFound(views.html.notFound("No locker exists with this ID."))
      case Some(locker) => if(req.method == "GET") {
                        Ok(views.html.lockers.getLocker(locker))
                        } else {
      				    val currentUser: Option[User] = User.current
      				    if(currentUser.isDefined) {
      				      if (Teacher.getByUsername(currentUser.get.username).isDefined) {
      				    	NotFound(views.html.notFound("Teachers do not have lockers."))
      				      } else {
      				        val maybeStudent: Option[Student] = Student.getByUsername(currentUser.get.username)
      				        val oldLocker: Option[Locker] = maybeStudent.flatMap(Locker.getByStudent(_))
      				        if(locker.taken) Ok(views.html.notFound("This locker was taken."))
      				        else {
      				          locker.student = maybeStudent
      				          locker.taken = true
      				          pm.makePersistent(locker)
      				          oldLocker match {
      				            case None => {}
      				            case Some(ol) => {
      				              ol.student_=(None)
      				              ol.taken_=(false)
      				              pm.makePersistent(ol)
      				            }
      				          }
      				          Redirect(routes.Application.index()).flashing("message" -> "You have successfully changed lockers.")
      				          }
      				        }
      				      } else {
      				        val visit = Visit.getFromRequest(req)
      				        visit.redirectUrl = controllers.routes.Lockers.getLocker(num)
      				        pm.makePersistent(visit)
      				        Redirect(routes.Users.login()).flashing("error" -> "You are not logged in.")
      				      }
      				  }
    }
    }
  }
  
  def lockerList(list: List[Locker]) = VisitAction { implicit req =>
    Ok(views.html.lockers.lockerList(list))
  }
  
  def lockerByNumber = VisitAction { implicit req =>
    DataStore.execute { pm => 
    object NumberForm extends Form {
      val number: TextField = new TextField("number")
      
      def fields = List(number)
      
      override def validate(vb: ValidBinding): ValidationError = {
        DataStore.execute { implicit pm =>
          Locker.validateLockerNumber(vb.valueOf(number)) match {
            case None => ValidationError("Invalid Locker Number")
            case Some(l) => ValidationError(Nil)
          }
        }
      }
    }
    if(req.method == "GET") {
      Ok(views.html.lockers.lockerByNumber(Binding(NumberForm)))
    } else {
      Binding(NumberForm, req) match {
        case ib: InvalidBinding => Ok(views.html.lockers.lockerByNumber(ib))
        case vb: ValidBinding => {
          val maybeLocker = Locker.getByNumber(toInt(vb.valueOf(NumberForm.number)))
          maybeLocker match {
            case None => NotFound(views.html.notFound("No locker exists with this number."))
            case Some(l) => Redirect(routes.Lockers.getLocker(l.number))
          }
        }
      }
    }
    }
  }
  
  def lockerSearch = VisitAction { implicit req =>
    object LockerForm extends Form {
      val floor: ChoiceField[Int] = new ChoiceField("floor",List(("1", 1), ("2", 2), ("3", 3)))
      val hall: ChoiceField[String] = new ChoiceField("hall",List(("Southeast", "SE"),("Southwest", "SW"),
    		  													  ("Northeast", "NE"),("Northwest", "NW"),
    		  													  ("Center East", "CE"),("Center West", "CW"),
    		  													  ("Manual Annex", "ANNEX")))
      val available: ChoiceField[Boolean] = new ChoiceField("available",List(("Available and Taken Lockers", false),("Available Lockers Only", true)))
      
      val fields = List(floor, hall, available)
    }
    DataStore.execute { pm => 
    if(req.method == "GET") {
      Ok(views.html.lockers.lockerSearch(Binding(LockerForm)))
    } else {
      Binding(LockerForm, req) match {
        case ib: InvalidBinding => Ok(views.html.lockers.lockerSearch(ib))
        case vb: ValidBinding => {
          val requestedLocation = LockerLocation(vb.valueOf(LockerForm.floor), 
                                                 vb.valueOf(LockerForm.hall))
          val matcher = (l: Locker) => l.matchingLocation(requestedLocation)
          
          val availabilityList = if(vb.valueOf(LockerForm.available)) Locker.availableLockers() else Locker.allLockers()
          val finalList = availabilityList.filter(matcher)
          Ok(views.html.lockers.lockerList(finalList))
        }
      }
    }
    }
  }
  
  def lockerByRoom(room: String) = VisitAction {implicit req =>
    DataStore.execute { pm =>
    val roomLocation = RoomLocation.makeRoomLoc(room)
    val matchingLockerLocation = roomLocation.toLockerLocation
    val matcher: Locker => Boolean = (l: Locker) => l.matchingLocation(matchingLockerLocation)
    val resultLocker = Locker.allLockers().filter(matcher)
    Ok(views.html.lockers.lockerList(resultLocker))
    }
  }
  
  def schedule = VisitAction {implicit req => 
    DataStore.execute { pm => 
    val currentUser = User.current
    val isStudent = currentUser.isDefined && Student.getByUsername(currentUser.get.username).isDefined
    if(!isStudent) {
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
        val linkNode: NodeSeq = {<a class ="btn" href={controllers.routes.Lockers.lockerByRoom(roomName).url}>Lockers Near Here</a>}
        <tr>
          <td>{ p.name }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.course.name)), <br />) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.teachers.map(_.user.shortName).mkString("; "))), <br />) }</td>
          <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.room.name)), <br />) }</td>
          <td>{ linkNode }</td>
       </tr>
      }
        Ok(views.html.lockers.schedule(student, table, hasEnrollments))
    }
  }
  }
}