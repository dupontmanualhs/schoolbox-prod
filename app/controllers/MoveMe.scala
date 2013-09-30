package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import controllers.users.VisitAction
import com.google.inject.{ Inject, Singleton }
import config.Config
import models.users.User
import controllers.users.PermissionRequired
import models.courses.Teacher
import org.dupontmanual.forms._
import controllers.users.VisitRequest
import config.users.UsesDataStore
import config.courses.SendActivation

class MoveMe @Inject()(implicit config: Config) extends Controller with UsesDataStore {
  object TeacherActivation {
    def apply(form: Binding)(implicit req: VisitRequest[_], config: Config) = {
      config.main("Send Teacher Activation")(
        form.render()    
      )
    }
  }
  
  object TeacherForm extends Form {
    val teacher = new Teacher.ChooseActiveTeacherField("teacher")
    
    def fields() = List(teacher)
  }
  
  def sendTeacherActivation() = PermissionRequired(User.Permissions.Manage) { implicit req =>
    Ok(TeacherActivation(Binding(TeacherForm)))  
  }
  
  def sendTeacherActivationP() = PermissionRequired(User.Permissions.Manage) { implicit req =>
    dataStore.execute { pm =>
      Binding(TeacherForm, req) match {
        case ib: InvalidBinding => Ok(TeacherActivation(ib))
        case vb: ValidBinding => {
          val teacher: Teacher = vb.valueOf(TeacherForm.teacher)
          teacher.user.email match {
            case None => Redirect(controllers.routes.MoveMe.sendTeacherActivation()).flashing(
                "error" -> s"No activation email was sent, because the user does not have an email address.")
            case Some(email) => {
              SendActivation.toTeacher(teacher)
              Redirect(controllers.routes.MoveMe.sendTeacherActivation()).flashing(
                  "message" -> s"A new activation email was sent to ${email}")
            }
          }
        }
      }  
    }
  }
}