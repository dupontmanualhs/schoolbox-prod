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
  def id: Long = _id

  @Persistent(defaultFetchGroup="true")
  private[this] var _user: User = _
  def user: User = _user
  def user_=(theUser: User) { _user = theUser }
    
  def role: String

  protected def this(user: User) = {
    this()
    user_=(user)
  }
  
  override def toString: String = formalNameWithRole
  
  def groups(): Set[Group] = {
    val cand = QGroup.candidate
    DataStore.pm.query[Group].filter(cand.roles.contains(this)).executeList().toSet
  }
  
  def rolePermissions(): Set[Permission] = {
    val cand = QPermission.candidate
    DataStore.pm.query[Permission].filter(cand.roles.contains(this)).executeList().toSet
  }
  
  def permissions(): Set[Permission] = {
    (this.groups().map(_.permissions()) + this.rolePermissions()).flatten
  }
  
  def addPermission(permission: Permission) {
    permission.roles_=(permission.roles + this)
    DataStore.pm.makePersistent(permission)
  }
  
  def displayNameWithRole = "%s (%s)".format(user.displayName, role)
  def formalNameWithRole = "%s (%s)".format(user.formalName, role)
  def shortNameWithRole = "%s (%s)".format(user.shortName, role)
  def displayName = user.displayName
  def formalName = user.formalName
  def shortName = user.shortName
  
  def compare(that: Role) = {
    val comp = this.user.compare(that.user)
    if (comp == 0) this.role.compare(that.role) else comp
  }
  
  def canEqual(that: Any): Boolean = that.isInstanceOf[Role]
  
  override def equals(that: Any): Boolean = that match {
    case that: Role => this.canEqual(that) && this.id == that.id
    case _ => false
  }
  
  override def hashCode: Int = this.id.hashCode
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

