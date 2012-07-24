package models.users

import java.util.UUID
import javax.jdo.annotations._

import scala.collection.immutable.HashSet
import scala.collection.JavaConverters._

@PersistenceCapable(detachable="true")
class Visit {
  @PrimaryKey
  private[this] var _uuid: String = UUID.randomUUID().toString()
  private[this] var _user: User = _
  private[this] var _perspective: Perspective = _
  private[this] var _permissions: java.util.Set[Permission] = _
  
  def this(user: User, perspective: Perspective) = {
    this()
    user_=(user)
    perspective_=(perspective)
    permissions_=(new HashSet[Permission]())
  }
  
  def uuid: String = _uuid
  
  def user: User = _user
  def user_=(theUser: User) { _user = theUser }
  
  def perspective: Perspective = _perspective
  def perspective_=(thePerspective: Perspective) { _perspective = thePerspective }
  
  def permissions: Set[Permission] = _permissions.asScala
  def permissions_=(thePermissions: Set[Permission]) { _permissions = thePermissions.asJava }
  
}