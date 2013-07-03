package models.users

import javax.jdo.annotations._

import scala.collection.JavaConverters._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

@PersistenceCapable(detachable="true")
class Group {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Persistent
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }
  
  @Persistent
  @Element(types=Array(classOf[Role]))
  private[this] var _roles: java.util.Set[Role] = _
  def roles: Set[Role] = _roles.asScala.toSet
  def roles_=(theRoles: Set[Role]) { _roles = theRoles.asJava }
}