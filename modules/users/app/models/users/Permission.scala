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
  
  @Persistent
  @Element(types=Array(classOf[Role]))
  private[this] var _roles: List[Role] = _
 
  @Persistent
  @Element(types=Array(classOf[Group]))
  private[this] var _groups: List[Group] = _
}