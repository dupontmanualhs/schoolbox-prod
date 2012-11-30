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

object Lockers extends Controller {
  def index() = DbAction { implicit req =>
    Ok(views.html.lockers.index())
  }
  
  def searchLockerId(stu: Student): Long = {
      val maybeLocker = Locker.getByStudent(stu)
      maybeLocker match {
        case Some(s) => s.id
        case None => throw new Exception("Student does not own a locker")
      }
  }
   
  def getMyLocker() = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
  	val currentUser: Option[User] = User.current
  	if(currentUser.isDefined) {
      if (Teacher.getByUsername(currentUser.get.username)(pm).isDefined) {
        NotFound(views.html.notFound("Teachers do not have lockers."))
      } else {
        val Some(maybeStudent) = Student.getByUsername(currentUser.get.username)(pm)
        val maybeLocker: Option[Locker] = Locker.getByStudent(maybeStudent)
        maybeLocker match {
          case None => NotFound(views.html.notFound("You do not have a locker."))
          case Some(l) => Ok(views.html.lockers.getLocker(l))
        }
      }
    } else {
      NotFound(views.html.notFound("You are not logged in."))
    }
  }
  
  def getLocker(id: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    val maybeLocker = Locker.getById(id)(pm)
    maybeLocker match {
      case None => NotFound(views.html.notFound("No locker exists with this ID."))
      case Some(locker) => if(req.method == "GET") {
                        Ok(views.html.lockers.getLocker(locker))
                        } else {
      				    val currentUser: Option[User] = User.current
      				    if(currentUser.isDefined) {
      				      if (Teacher.getByUsername(currentUser.get.username)(pm).isDefined) {
      				    	NotFound(views.html.notFound("Teachers do not have lockers."))
      				      } else {
      				        val Some(student) = Student.getByUsername(currentUser.get.username)(pm)
      				        val oldLocker = Locker.getByStudent(student)
      				        if(locker.taken) Ok(views.html.notFound("This locker was taken."))
      				        else {
      				          locker.student_=(Some(student))
      				          locker.taken_=(true)
      				          pm.makePersistent(locker)
      				          oldLocker match {
      				            case None => {}
      				            case Some(ol) => {
      				              ol.student_=(None)
      				              ol.taken_=(false)
      				              pm.makePersistent(ol)
      				            }
      				          }
      				          Ok(views.html.lockers.lockerSuccess())
      				          }
      				        }
      				      } else {
      				        Ok(views.html.notFound("You are not logged in."))
      				      }
      				  }
    }
  }
  
  def lockerList(list: List[Locker]) = DbAction { implicit req =>
    Ok(views.html.lockers.lockerList(list))
  }
  
  def findLocker = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    object NumberForm extends Form {
      val number: TextField = new TextField("number")
      
      def fields = List(number)
      
      override def validate(vb: ValidBinding): ValidationError = {
        DataStore.withTransaction { implicit pm =>
          Locker.validateLockerNumber(vb.valueOf(number)) match {
            case None => ValidationError("Invalid Locker Number")
            case Some(l) => ValidationError(Nil)
          }
        }
      }
    }
    if(req.method == "GET") {
      Ok(views.html.lockers.findLocker(Binding(NumberForm)))
    } else {
      Binding(NumberForm, req) match {
        case ib: InvalidBinding => Ok(views.html.lockers.findLocker(ib))
        case vb: ValidBinding => {
          val maybeLocker = Locker.getByNumber(toInt(vb.valueOf(NumberForm.number)))(pm)
          maybeLocker match {
            case None => NotFound(views.html.notFound("No locker exists with this number."))
            case Some(l) => Redirect(routes.Lockers.getLocker(l.id))
          }
        }
      }
    }
  }
  
  def lockerSearch = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    object LockerForm extends Form {
      val floor: ChoiceField[Int] = new ChoiceField("floor",List(("1", 1), ("2", 2), ("3", 3)))
      val hall: ChoiceField[String] = new ChoiceField("hall",List(("Southeast", "SE"),("Southwest", "SW"),
    		  													  ("Northeast", "NE"),("Northwest", "NW"),
    		  													  ("Center East", "CE"),("Center West", "CW"),
    		  													  ("Manual Annex", "ANNEX")))
      val available: ChoiceField[Boolean] = new ChoiceField("available",List(("Available and Taken Lockers", false),("Available Lockers Only", true)))
      
      val fields = List(floor, hall, available)
    }
    if(req.method == "GET") {
      Ok(views.html.lockers.lockerSearch(Binding(LockerForm)))
    } else {
      Binding(LockerForm, req) match {
        case ib: InvalidBinding => Ok(views.html.lockers.lockerSearch(ib))
        case vb: ValidBinding => {
          val requestedLocation = LockerLocation(vb.valueOf(LockerForm.floor), 
                                                 vb.valueOf(LockerForm.hall))
          val matcher = (l: Locker) => l.matchingLocation(requestedLocation)
          
          val availabilityList = if(vb.valueOf(LockerForm.available)) Locker.availableLockers()(pm) else Locker.allLockers()(pm)
          val finalList = availabilityList.filter(matcher)
          Ok(views.html.lockers.lockerList(finalList))
        }
      }
    }
  }
  
  def lockerPicker = DbAction {implicit req => 
    implicit val pm = req.pm
    val currentUser = User.current
    val isStudent = currentUser.isDefined && Student.getByUsername(currentUser.get.username)(pm).isDefined
    if(req.method == "GET") {
      if(!isStudent) {
        NotFound(views.html.notFound("Must be logged-in student to select lockers."))
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
          <tr id = {sectionsThisPeriod.map(s => Text(RoomLocation.lockerPickerMake(s.room)))} >
            <td>{ p.name }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.course.name)), <br/>) }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.teachers.map(_.user.shortName).mkString("; "))), <br/>) }</td>
            <td>{ mkNodeSeq(sectionsThisPeriod.map(s => Text(s.room.name)), <br/>) }</td>
         </tr>
         }
        Ok(views.html.lockers.lockerPicker(student, table, hasEnrollments))
      }
    } else {
      NotFound(views.html.notFound("go away"))
    }
  }
  
  def lockerPickerSubmit = TODO
}