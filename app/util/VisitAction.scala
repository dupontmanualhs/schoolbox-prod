package util

import play.api.mvc.{Action, AnyContent, BodyParser, BodyParsers, Request, Result}
import javax.jdo.JDOHelper
import scalajdo.DataStore
import models.users.Visit

// TODO: we need a cache system
object VisitAction {
  def apply (block: => Result): Action[AnyContent] = apply(_ => block) 
  
  def apply(block: (Request[AnyContent]) => Result): Action[AnyContent] = {
    apply[AnyContent](BodyParsers.parse.anyContent)(block)
  }

  def apply[A](p: BodyParser[A])(f: Request[A] => Result): Action[A] = {
    Action(p) ( request => {
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
        pm.commitTransaction()
        if (request.session.get(visit.uuid).isDefined) res
        else res.withSession("visit" -> visit.uuid)
      }
    })
  }
}

/*
object Authenticated {
  def apply(f: VisitRequest[AnyContent] => PlainResult) = VisitAction( implicit req => {
	req.visit.user match {
	  case None => {
	    req.visit.redirectUrl = req.path
	    DataStore.pm.makePersistent(req.visit)
	    Results.Redirect(controllers.routes.Auth.login()).flashing("error" -> "You must log in to view that page.")
	  }
	  case Some(user) => {
	    implicit val u: User = user
	    f(req)
	  }
	}
  })
}
*/