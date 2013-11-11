package controllers.users

import play.api.mvc.{ Action, AnyContent, BodyParser, BodyParsers, Request, Result, Results }
import javax.jdo.JDOHelper
import models.users.Visit
import play.api.mvc.WrappedRequest
import models.users.Role
import models.users.Permission
import scala.reflect.ClassTag
import config.users.Config
import org.dupontmanual.forms.{ Call, Method }
import com.google.inject.Inject
import config.users.UsesDataStore

case class VisitRequest[A](visit: Visit, private val request: Request[A])
  extends WrappedRequest(request)

/**
 * request that guarantees a user has logged in and chosen a role.
 * both role and visit are available from the request
 */
class AuthenticatedRequest[A](
    val role: Role, 
    visit: Visit, 
    request: Request[A]) extends VisitRequest[A](visit, request)

object AuthenticatedRequest {
  def apply[A](role: Role, visit: Visit, request: Request[A]) = new AuthenticatedRequest[A](role, visit, request)
}

// TODO: we need a cache system
object VisitAction extends UsesDataStore {
  def apply(block: VisitRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }

  def apply[A](p: BodyParser[A])(f: VisitRequest[A] => Result)(implicit config: Config): Action[A] = {
    Action(p)( request => {
      dataStore.withTransaction { pm => 
        val visitRequest = VisitRequest(Visit.getFromRequest(request, config), request)
        val res = f(visitRequest)
        if (JDOHelper.isDeleted(visitRequest.visit)) {
          res.withNewSession
        } else {
          visitRequest.visit.expiration = System.currentTimeMillis + Visit.visitLength
          if (request.session.get(visitRequest.visit.uuid).isDefined) res
          else res.withSession("visit" -> visitRequest.visit.uuid)
        }
      }
    })
  }
}
    
object Authenticated extends UsesDataStore {
  def apply(block: AuthenticatedRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }
  
  def apply[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result)(implicit config: Config): Action[A] = {
    VisitAction(p) { req =>
      req.visit.role match {
        case None => dataStore.execute { pm =>
          req.visit.redirectUrl = Call(Method(req.method), req.uri)
          pm.makePersistent(req.visit)
          Results.Redirect(controllers.users.routes.App.login()).flashing("error" -> "You must log in to view that page.")
        }
        case Some(role) => f(AuthenticatedRequest(role, req.visit, req))
      }
    }
  }
}

object PermissionRequired {
  import MustPassTest.Test
  
  private[this] def permission2Test[A](permission: Permission): Test[A] = {
    (req: AuthenticatedRequest[A]) => req.visit.permissions.contains(permission)
  }
  
  def apply(permission: Permission)(block: AuthenticatedRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    TestAction.apply(permission2Test[AnyContent](permission), block)
  }
  
  def apply[A](permission: Permission, p: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit config: Config): Action[A] = {
    TestAction.apply[A](permission2Test[A](permission), p, block)
  }
}
  
object RoleMustPass {
  import MustPassTest.Test
  type RoleTest = (Role => Boolean)
  
  private[this] def roleTest2Test[A](roleTest: RoleTest): Test[A] = {
    (req: AuthenticatedRequest[A]) => roleTest(req.role)
  }
  
  def apply(roleTest: RoleTest)(block: AuthenticatedRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    TestAction.apply(roleTest2Test[AnyContent](roleTest), block)
  }
  
  def apply[A](roleTest: RoleTest, p: BodyParser[A])(block: AuthenticatedRequest[A] => Result)(implicit config: Config): Action[A] = {
    TestAction.apply[A](roleTest2Test[A](roleTest), p, block)
  }
}

object MustPassTest {
  type Test[A] = (AuthenticatedRequest[A] => Boolean)

  def apply(test: Test[AnyContent])(block: AuthenticatedRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    TestAction.apply(test, block)   
  }
  
  def apply[A](test: Test[A], p: BodyParser[A])(f: AuthenticatedRequest[A] => Result)(implicit config: Config): Action[A] = {
    TestAction.apply[A](test, p, f)
  }
}

private[users] object TestAction {
  import MustPassTest.Test
  
  def apply(test: Test[AnyContent], block: AuthenticatedRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    apply[AnyContent](test, BodyParsers.parse.anyContent, block)    
  }
  
  def apply[A](test: Test[A], p: BodyParser[A], f: AuthenticatedRequest[A] => Result)(implicit config: Config): Action[A] = {
    Authenticated(p) { req => 
      if (test(req)) f(req)
      else Results.Forbidden("You are not authorized to view this page.")
    }
  }
}



