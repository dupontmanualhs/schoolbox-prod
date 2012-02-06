package util

import play.api.mvc.{Request, WrappedRequest}
import models.users.User
import play.api.mvc.Result
import play.api.mvc.BodyParser
import play.api.mvc.BodyParsers.parse
import play.api.mvc.Action
import play.api.mvc.AnyContent

class DbRequest[A](val request: Request[A]) extends WrappedRequest[A](request) {
  implicit val pm = DataStore.getPersistenceManager()
}

object DbAction {
  def apply[A](p: BodyParser[A])(f: DbRequest[A] => Result) = {
    Action(p) { implicit request =>
      f(new DbRequest[A](request))
    }
  }

  def apply(f: DbRequest[AnyContent] => Result) = {
    apply[AnyContent](parse.anyContent)(f)
  }
}