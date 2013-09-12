package controllers.courses
import play.api.mvc.Request
import controllers.users.{Authenticated, AuthenticatedRequest, MustPassTest}
import models.courses.{ Guardian, Section, Student, StudentEnrollment, Teacher, Term }
import models.users.{ Role, Visit }
import javax.jdo.annotations.Inheritance
import javax.jdo.annotations.PersistenceCapable
import play.api.mvc.Action
import play.api.mvc.AnyContent
import config.users.Config
import play.api.mvc.Result
import play.api.mvc.BodyParsers
import play.api.mvc.BodyParser
import play.api.mvc.Results

class StudentRequest[A](val student: Student, role: Role,
    visit: Visit, request: Request[A]) extends AuthenticatedRequest[A](role, visit, request)
    
object StudentRequest {
  def apply[A](student: Student, role: Role, visit: Visit, request: Request[A]) =
      new StudentRequest[A](student, role, visit, request)
}

// collection of tests that require fetching a student, so the student is added to
// the request, so we don't have to fetch it again
object StudentCheck {
  def apply[A](student: Student, p: BodyParser[A] = BodyParsers.parse.anyContent)(f: StudentRequest[A] => Result)(implicit config: Config): Action[A] = {
    Authenticated(p)(req => f(StudentRequest(student, req.role, req.visit, req)))
  }
  
  // A student's schedule may be viewed by: that student, any teacher, that student's guardian
  def fromUsername[A](username: String, p: BodyParser[A] = BodyParsers.parse.anyContent)(f: StudentRequest[A] => Result)(implicit config: Config): Action[A] = {
    Authenticated(p)(implicit req => Student.getByUsername(username) match {
      case None => Results.NotFound(config.notFound("No such student."))
      case Some(student) => f(StudentRequest(student, req.role, req.visit, req))
    })
  }
  
  def canViewSchedule[A](username: String, p: BodyParser[A] = BodyParsers.parse.anyContent)(
      f: StudentRequest[A] => Result)(implicit config: Config): Action[A] = {
    def roleIsTeacherOrStudentGuardian(implicit req: StudentRequest[A]): Boolean = {
      req.role match {
        case teacher: Teacher => true
        case guardian: Guardian => guardian.children.contains(req.student)
        case _ => false
      }
    }
    def newF: StudentRequest[A] => Result = { implicit req: StudentRequest[A] => 
      if (req.visit.permissions.contains(StudentEnrollment.Permissions.View)
          || req.role == req.student || roleIsTeacherOrStudentGuardian) {
        f(req)
      } else {
        Results.Forbidden("You are not authorized to view this page.")     
      }  
    }
    fromUsername[A](username, p)(newF)(config)
  }
}

class GuardianRequest[A](val guardian: Guardian, role: Role,
		visit: Visit, request: Request[A]) extends AuthenticatedRequest[A](role, visit, request)
    
object GuardianRequest {
  def apply[A](guardian: Guardian, role: Role, visit: Visit, request: Request[A]) =
      new GuardianRequest[A](guardian, role, visit, request)
}

object GuardianCheck {
}


