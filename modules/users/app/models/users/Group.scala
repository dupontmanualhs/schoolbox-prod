package models.users

import scala.collection.JavaConverters._
import javax.jdo.annotations._
import scala.collection.JavaConverters._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

import config.users.Config
import com.google.inject.Inject
import config.users.UsesDataStore

@PersistenceCapable(detachable="true")
class Group extends UsesDataStore {
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
  
  def this(theName: String, theRoles: Set[Role] = Set[Role]()) {
    this()
    name_=(theName)
    roles_=(theRoles)
  }
  
  override def toString: String = s"Group: ${name}"
  
  def permissions(): Set[Permission] = {
    val cand = QPermission.candidate
    dataStore.pm.query[Permission].filter(cand.groups.contains(this)).executeList().toSet
  }
  
  def addPermission(permission: Permission) {
    permission.groups_=(permission.groups + this)
    dataStore.pm.makePersistent(permission)
  }

  def canEqual(that: Any): Boolean = that.isInstanceOf[Group]
  
  override def equals(that: Any): Boolean = that match {
    case that: Group => this.canEqual(that) && this.id == that.id
    case _ => false
  }
  
  override def hashCode: Int = this.id.hashCode
}

object Group extends UsesDataStore {
  @Inject
  def config(conf: Config): Config = conf
  
  def apply(name: String): Group = {
    val cand = QGroup.candidate()
    dataStore.execute { pm =>
      pm.query[Group].filter(cand.name.eq(name)).executeOption() match {
        case None => {
          val newGroup = new Group(name)
          pm.makePersistent(newGroup)
          newGroup
        }
        case Some(group) => group
      }  
    }
  }
}

trait QGroup[PC <: Group] extends PersistableExpression[PC] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _roles: CollectionExpression[java.util.Set[Role], Role] = new CollectionExpressionImpl[java.util.Set[Role], Role](this, "_roles")
  def roles: CollectionExpression[java.util.Set[Role], Role] = _roles
}

object QGroup {
  def apply(parent: PersistableExpression[Group], name: String, depth: Int): QGroup[Group] = {
    new PersistableExpressionImpl[Group](parent, name) with QGroup[Group]
  }
  
  def apply(cls: Class[Group], name: String, exprType: ExpressionType): QGroup[Group] = {
    new PersistableExpressionImpl[Group](cls, name, exprType) with QGroup[Group]
  }
  
  private[this] lazy val jdoCandidate: QGroup[Group] = candidate("this")
  
  def candidate(name: String): QGroup[Group] = QGroup(null, name, 5)
  
  def candidate(): QGroup[Group] = jdoCandidate

  def parameter(name: String): QGroup[Group] = QGroup(classOf[Group], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QGroup[Group] = QGroup(classOf[Group], name, ExpressionType.VARIABLE)

}
