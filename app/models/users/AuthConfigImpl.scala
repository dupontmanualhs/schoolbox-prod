package models.users
import jp.t2v.lab.play20.auth.AuthConfig
import play.api.mvc.PlainResult
import util.ScalaPersistenceManager
import play.api.mvc.Request
import play.api.mvc.Results.{Forbidden, Redirect}
import controllers.routes



trait AuthConfigImpl extends AuthConfig {
  implicit def pm: ScalaPersistenceManager 
  /** 
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on. 
   */
  type Id = Long
  
  /** 
   * A type that represents a user in your application.
   * `User`, `Perspective` and so on.
   */
  type User = Perspective
  
   /**
    * A type that is defined by every action for authorization.
    */
  type Authority = Permission
  
   /**
   * A `ClassManifest` is used to get an id from the Cache API.
   * Basically use the same setting as the following.
   */
  val idManifest: ClassManifest[Id] = classManifest[Id]

  /**
   * A duration of the session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600

  /**
   * A function that returns a `User` object from an `Id`.
   * Describe the procedure according to your application.
   */
  def resolveUser(id: Id): Option[User] = Perspective.getById(id)

  /**
   * A redirect target after a successful user login.
   */
  def loginSucceeded[A](request: Request[A]): PlainResult = Redirect(controllers.routes.Application.index)

  /**
   * A redirect target after a successful user logout.
   */
  def logoutSucceeded[A](request: Request[A]): PlainResult = Redirect(controllers.routes.Users.login())

  /**
   * A redirect target after a failed authentication.
   */
  def authenticationFailed[A](request: Request[A]): PlainResult = Redirect(controllers.routes.Users.login())

  /**
   * A redirect target after a failed authorization.
   */
  def authorizationFailed[A](request: Request[A]): PlainResult = Forbidden("no permission")

  /**
   * A function that authorizes a user by `Authority`.
   * Describe the procedure according to your application.
   */
  def authorize(user: User, authority: Authority): Boolean = 
    (user.permission, authority) match {
      case (Teacher, _) => true
      case (Student, _) => true
      case (Guardian, _) => true
      case _ => false
    }

  
  

}