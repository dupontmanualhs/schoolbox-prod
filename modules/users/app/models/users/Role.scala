package models.users

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import scalajdo.DataStore

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
abstract class Role extends Ordered[Role] {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  @Persistent(defaultFetchGroup="true")
  private[this] var _user: User = _
  
  protected def this(user: User) = {
    this()
    user_=(user)
  }
  
  def id: Long = _id

  def user: User = _user
  def user_=(theUser: User) { _user = theUser }
  
  def displayNameWithRole = "%s (%s)".format(user.displayName, role)
  def formalNameWithRole = "%s (%s)".format(user.formalName, role)
  def shortNameWithRole = "%s (%s)".format(user.shortName, role)
  def displayName = user.displayName
  def formalName = user.formalName
  def shortName = user.shortName
  def role: String
  
  def compare(that: Role) = {
    this.user.compare(that.user)
  }
}

object Role {
  def getById(id: Long): Option[Role] = {
    val cand = QRole.candidate
    DataStore.pm.query[Role].filter(cand.id.eq(id)).executeOption()
  }
}

trait QRole[PC <: Role] extends PersistableExpression[PC] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _user: ObjectExpression[User] = new ObjectExpressionImpl[User](this, "_user")
  def user: ObjectExpression[User] = _user 
}

object QRole {
  def apply(parent: PersistableExpression[Role], name: String, depth: Int): QRole[Role] = {
    new PersistableExpressionImpl[Role](parent, name) with QRole[Role]
  }
  
  def apply(cls: Class[Role], name: String, exprType: ExpressionType): QRole[Role] = {
    new PersistableExpressionImpl[Role](cls, name, exprType) with QRole[Role]
  }
  
  private[this] lazy val jdoCandidate: QRole[Role] = candidate("this")
  
  def candidate(name: String): QRole[Role] = QRole(null, name, 5)
  
  def candidate(): QRole[Role] = jdoCandidate

  def parameter(name: String): QRole[Role] = QRole(classOf[Role], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QRole[Role] = QRole(classOf[Role], name, ExpressionType.VARIABLE)

}

