package util

import play.api.mvc.{ Action, AnyContent, BodyParser, BodyParsers, Request, Result, Results }
import javax.jdo.JDOHelper
import scalajdo.DataStore
import models.users.Visit
import play.api.mvc.WrappedRequest
import models.users.Role
import models.users.Permission
import scala.reflect.ClassTag

case class VisitRequest[A](visit: Visit, private val request: Request[A])
  extends WrappedRequest(request)

// TODO: we need a cache system
object VisitAction {
  def apply(block: => Result): Action[AnyContent] = apply(_ => block)

  def apply(block: VisitRequest[AnyContent] => Result): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }

  def apply[A](p: BodyParser[A])(f: VisitRequest[A] => Result): Action[A] = {
    Action(p)( request => {
      val pm = DataStore.pm
      pm.beginTransaction()
      val visitRequest = VisitRequest(Visit.getFromRequest(request), request)
      val res = f(visitRequest)
      if (JDOHelper.isDeleted(visitRequest.visit)) {
        pm.commitTransaction()
        res.withNewSession
      } else {
        visitRequest.visit.expiration = System.currentTimeMillis + Visit.visitLength
        pm.makePersistent(visitRequest.visit)
        pm.commitTransactionAndClose()
        if (request.session.get(visitRequest.visit.uuid).isDefined) res
        else res.withSession("visit" -> visitRequest.visit.uuid)
      }
    })
  }
}

case class AuthenticatedRequest[A](
    role: Role, 
    visit: Visit, 
    private val request: Request[A]) extends WrappedRequest(request)

object Authenticated {
  def apply(block: => Result): Action[AnyContent] = apply(_ => block)
  
  def apply(block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }
  
  def apply[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result): Action[A] = {
    VisitAction(p) { req =>
      req.visit.role match {
        case None => DataStore.execute { pm =>
          req.visit.redirectUrl = Call(Method(req.method), req.uri)
          pm.makePersistent(req.visit)
          Results.Redirect(controllers.routes.Users.login()).flashing("error" -> "You must log in to view that page.")
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
  
  def apply(permission: Permission)(block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
    TestAction.apply(permission2Test[AnyContent](permission), block)
  }
  
  def apply[A](permission: Permission, p: BodyParser[A])(block: AuthenticatedRequest[A] => Result): Action[A] = {
    TestAction.apply[A](permission2Test[A](permission), p, block)
  }
}
  
object RoleMustPass {
  import MustPassTest.Test
  type RoleTest = (Role => Boolean)
  
  private[this] def roleTest2Test[A](roleTest: RoleTest): Test[A] = {
    (req: AuthenticatedRequest[A]) => roleTest(req.role)
  }
  
  def apply(roleTest: RoleTest, block: => Result): Action[AnyContent] = {
    TestAction.apply(roleTest2Test[AnyContent](roleTest), block)
  }
  
  def apply(roleTest: RoleTest, block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
    TestAction.apply(roleTest2Test[AnyContent](roleTest), block)
  }
  
  def apply[A](roleTest: RoleTest, p: BodyParser[A], block: AuthenticatedRequest[A] => Result)(implicit tag: ClassTag[RoleTest]): Action[A] = {
    TestAction.apply[A](roleTest2Test[A](roleTest), p, block)
  }
}
  
object MustPassTest {
  type Test[A] = (AuthenticatedRequest[A] => Boolean)

  def apply(test: Test[AnyContent])(block: => Result): Action[AnyContent] = {
    TestAction.apply(test, block)
  }
  
  def apply(test: Test[AnyContent])(block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
    TestAction.apply(test, block)   
  }
  
  def apply[A](test: Test[A])(p: BodyParser[A])(f: AuthenticatedRequest[A] => Result): Action[A] = {
    TestAction.apply[A](test, p, f)
  }
}

private[util] object TestAction {
  import MustPassTest.Test
  
  def apply(test: Test[AnyContent], block: => Result): Action[AnyContent] = {
    apply[AnyContent](test, BodyParsers.parse.anyContent, (_: AuthenticatedRequest[AnyContent]) => block)
  }
  
  def apply(test: Test[AnyContent], block: AuthenticatedRequest[AnyContent] => Result): Action[AnyContent] = {
    apply[AnyContent](test, BodyParsers.parse.anyContent, block)    
  }
  
  def apply[A](test: Test[A], p: BodyParser[A], f: AuthenticatedRequest[A] => Result): Action[A] = {
    Authenticated(p) { req => 
      if (test(req)) f(req)
      else Results.Forbidden("You are not authorized to view this page.")
    }
  }
  
}



