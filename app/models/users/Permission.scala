package models.users

import javax.jdo.annotations._

import scala.collection.JavaConverters._

import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import play.api.mvc.{RequestHeader, Session}

import scalajdo.DataStore

@PersistenceCapable(detachable="true")
@Unique(name="CLASS_WITH_NAME", members=Array("_klass", "_enumId"))
class Permission {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Column(allowsNull="false")
  private[this] var _klass: String = _
  def klass: String = _klass
  def klass_=(theKlass: String) { _klass = theKlass }
  
  @Column(allowsNull="false")
  private[this] var _enumId: Int = _
  def enumId: Int = _enumId
  def enumId_=(theEnumId: Int) { _enumId = theEnumId }
  
  @Column(allowsNull="false")
  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }
  
  @Column(allowsNull="false")
  private[this] var _description: String = _
  def description: String = _description
  def description_=(theDescription: String) { _description = theDescription }
  
  @Join
  private[this] var _roles: java.util.Set[Role] = _
  def roles: Set[Role] = _roles.asScala.toSet
  def roles_=(theRoles: Set[Role]) { _roles = theRoles.asJava }
  
  @Join
  private[this] var _groups: java.util.Set[Group] = _
  def groups: Set[Group] = _groups.asScala.toSet
  def groups_=(theGroups: Set[Group]) { _groups = theGroups.asJava }
  
  private[users] def this(theKlass: String, theEnumId: Int, theName: String, theDescription: String) {
    this()
    klass_=(theKlass)
    enumId_=(theEnumId)
    name_=(theName)
    description_=(theDescription)
    roles_=(Set[Role]())
    groups_=(Set[Group]())
  }
  
  override def toString: String = s"Permission(${klass}.Permissions.${name}"
  
  def canEqual(that: Any): Boolean = that.isInstanceOf[Permission]
  
  override def equals(that: Any): Boolean = that match {
    case that: Permission => this.canEqual(that) && this.id == that.id
    case _ => false
  }
  
  override def hashCode: Int = id.hashCode
}

object Permission {
  def apply(klass: Class[_], enumId: Int, name: String, description: String): Permission = {
    val cand = QPermission.candidate
    val className = klass.getName()
    DataStore.execute { pm => 
      pm.query[Permission].filter(cand.klass.eq(className).and(cand.enumId.eq(enumId))).executeOption() match {
        case None => val perm = new Permission(className, enumId, name, description)
          pm.makePersistent(perm)
          perm
        case Some(perm) => 
          if (perm.name != name || perm.description != description) {
            perm.name = name
            perm.description = description
            pm.makePersistent(perm)
          }
          perm
      }
    }
  }
}

trait QPermission extends PersistableExpression[Permission] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _klass: StringExpression = new StringExpressionImpl(this, "_klass")
  def klass: StringExpression = _klass
  
  private[this] lazy val _enumId: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_enumId")
  def enumId: NumericExpression[Int] = _enumId

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _description: StringExpression = new StringExpressionImpl(this, "_description")
  def description: StringExpression = _description
  
  private[this] lazy val _roles: CollectionExpression[java.util.Set[Role], Role] = new CollectionExpressionImpl[java.util.Set[Role], Role](this, "_roles")
  def roles: CollectionExpression[java.util.Set[Role], Role] = _roles
  
  private[this] lazy val _groups: ListExpression[java.util.List[Group], Group] = new ListExpressionImpl[java.util.List[Group], Group](this, "_groups")
  def groups: ListExpression[java.util.List[Group], Group] = _groups
}

object QPermission {
  def apply(parent: PersistableExpression[Permission], name: String, depth: Int): QPermission = {
    new PersistableExpressionImpl[Permission](parent, name) with QPermission
  }
  
  def apply(cls: Class[Permission], name: String, exprType: ExpressionType): QPermission = {
    new PersistableExpressionImpl[Permission](cls, name, exprType) with QPermission
  }
  
  private[this] lazy val jdoCandidate: QPermission = candidate("this")
  
  def candidate(name: String): QPermission = QPermission(null, name, 5)
  
  def candidate(): QPermission = jdoCandidate

  def parameter(name: String): QPermission = QPermission(classOf[Permission], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QPermission = QPermission(classOf[Permission], name, ExpressionType.VARIABLE)
}
