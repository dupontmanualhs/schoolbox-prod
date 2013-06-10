package models.users

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import play.api.mvc.{RequestHeader, Session}

//TODO: this is not nearly finished
@PersistenceCapable(detachable="true")
class Group {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _

  private[this] var _perspectives: List[Perspective] = _
}