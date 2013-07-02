package models.users

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

//TODO: not nearly finished
@PersistenceCapable(detachable="true")
class Permission {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _

  @Column(allowsNull="false")
  private[this] var _description: String = _
  
  private[this] var _roles: List[Role] = _
 
  private[this] var _groups: List[Group] = _
}