package util

import play.api.mvc.{ Action, AnyContent, BodyParser, BodyParsers, Request, Result, Results }
import javax.jdo.JDOHelper
import scalajdo.DataStore
import models.users.Visit
import play.api.mvc.Call

// TODO: we need a cache system
object VisitAction {
  def apply(block: => Result): Action[AnyContent] = apply(_ => block)

  def apply(block: Request[AnyContent] => Result): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }

  def apply[A](p: BodyParser[A])(f: Request[A] => Result): Action[A] = {
    Action(p)(request => {
      val pm = DataStore.pm
      pm.beginTransaction()
      val visit = Visit.getFromRequest(request)
      val res = f(request)
      if (JDOHelper.isDeleted(visit)) {
        pm.commitTransaction()
        res.withNewSession
      } else {
        visit.expiration = System.currentTimeMillis + Visit.visitLength
        pm.makePersistent(visit)
        pm.commitTransactionAndClose()
        if (request.session.get(visit.uuid).isDefined) res
        else res.withSession("visit" -> visit.uuid)
      }
    })
  }
}

object Authenticated {
  def apply(block: => Result): Action[AnyContent] = apply(_ => block)
  
  def apply(block: Request[AnyContent] => Result): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }
  
  def apply[A](p: BodyParser[A])(f: Request[A] => Result): Action[A] = {
    VisitAction(p) { implicit req =>
      DataStore.execute { pm =>
        val visit = Visit.getFromRequest(req)
        visit.user match {
          case None => {
            visit.redirectUrl = new Call(req.method, req.uri)
            pm.makePersistent(visit)
            Results.Redirect(controllers.routes.Users.login()).flashing("error" -> "You must log in to view that page.")
          }
          case Some(user) => {
            f(req)
          }
        }
      }
    }
  }
}
