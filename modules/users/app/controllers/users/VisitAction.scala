package controllers.users

import javax.jdo.JDOHelper
import play.api.mvc._
import scalajdo.DataStore
import forms.Method
import models.users.{ Role, Visit }
import config.users.Config

class VisitRequest[A](val visit: Visit, private val request: Request[A]) extends WrappedRequest(request)

object VisitRequest {
  def apply[A](visit: Visit, request: Request[A]) = new VisitRequest(visit, request)
}

// TODO: we need a cache system
object VisitAction {
  def apply(block: => Result)(implicit config: Config): Action[AnyContent] = apply(_ => block)

  def apply(block: VisitRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }

  def apply[A](p: BodyParser[A])(f: VisitRequest[A] => Result)(implicit config: Config): Action[A] = {
    Action(p)( request => {
      val pm = DataStore.pm
      pm.beginTransaction()
      val visitRequest = VisitRequest(Visit.getFromRequest(request, config), request)
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

class AuthenticatedRequest[A](
    val role: Role, 
    visit: Visit, 
    request: Request[A]) extends VisitRequest[A](visit, request)
    
object AuthenticatedRequest {
  def apply[A](role: Role, visit: Visit, request: Request[A]) = new AuthenticatedRequest[A](role, visit, request)
}

object Authenticated {
  def apply(block: => Result)(implicit config: Config): Action[AnyContent] = apply(_ => block)
  
  def apply(block: AuthenticatedRequest[AnyContent] => Result)(implicit config: Config): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }
  
  def apply[A](p: BodyParser[A])(f: AuthenticatedRequest[A] => Result)(implicit config: Config): Action[A] = {
    VisitAction(p) { req =>
      req.visit.role match {
        case None => DataStore.execute { pm =>
          req.visit.redirectUrl = Call(Method(req.method), req.uri)
          pm.makePersistent(req.visit)
          Results.Redirect(controllers.users.routes.App.login()).flashing("error" -> "You must log in to view that page.")
        }
        case Some(role) => f(AuthenticatedRequest(role, req.visit, req))
      }
    }
  }
}
