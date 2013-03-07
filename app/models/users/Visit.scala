package models.users


import java.util.UUID
import models.mastery._
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import scala.collection.immutable.HashSet
import scala.collection.JavaConverters._
import util.Helpers.{string2elem, string2nodeSeq}
import util.{Menu, DataStore, ScalaPersistenceManager}
import scala.xml.Elem
import play.api.mvc.Call

@PersistenceCapable(detachable="true")
class Visit {
  @PrimaryKey
  private[this] var _uuid: String = UUID.randomUUID().toString()
  private[this] var _expiration: Long = _
  private[this] var _user: User = _
  private[this] var _perspective: Perspective = _
  @Persistent(defaultFetchGroup = "true")
  private[this] var _redirectURL: Option[Call] = _
  @Element(types=Array(classOf[Permission]))
  @Join
  private[this] var _permissions: java.util.Set[Permission] = _
  @Column(jdbcType="CLOB")
  private[this] var _menu: String = _
  @Key(types=Array(classOf[String]))
  @Value(types=Array(classOf[Object]))
  @Serialized
  private[this] var _sessionItems: java.util.Map[String, Object] = _
  
  def this(theExpiration: Long, maybeUser: Option[User], maybePerspective: Option[Perspective]) = {
    this()
    _expiration = theExpiration
    _user = maybeUser.getOrElse(null)
    _perspective = maybePerspective.getOrElse(null)
    permissions_=(Set[Permission]())
    menu_=(Menu.buildMenu(perspective))
    _sessionItems = new java.util.HashMap[String, Object]()  
    _redirectURL = None
  }
  
  def uuid: String = _uuid
  
  def expiration: Long = _expiration
  def expiration_=(theExpiration: Long) { _expiration = theExpiration }
  
  def user: Option[User] = if (_user == null) None else Some(_user)
  def user_=(maybeUser: Option[User]) { _user = maybeUser.getOrElse(null) }
  
  def perspective: Option[Perspective] = if (_perspective == null) None else Some(_perspective)
  def perspective_=(maybePerspective: Option[Perspective]) { _perspective = maybePerspective.getOrElse(null) }
  
  def redirectURL: Option[Call] = _redirectURL
  def redirectURL_=(url: Call) {_redirectURL = Some(url)}
  
  def permissions: Set[Permission] = _permissions.asScala.toSet[Permission]
  def permissions_=(thePermissions: Set[Permission]) { _permissions = thePermissions.asJava }
  
  def menu: Elem = string2elem(_menu)
  def menu_=(theMenu: Elem) { _menu = theMenu.toString }
    
  def isExpired: Boolean = System.currentTimeMillis > expiration
  
  def updateMenu { menu = Menu.buildMenu(perspective) }
  
  def set(key: String, value: Any) {
    value match {
      case obj: AnyRef => _sessionItems.put(key, obj)
      case byte: Byte => _sessionItems.put(key, byte: java.lang.Byte)
      case short: Short => _sessionItems.put(key, short: java.lang.Short)
      case int: Int => _sessionItems.put(key, int: java.lang.Integer)
      case long: Long => _sessionItems.put(key, long: java.lang.Long)
      case float: Float => _sessionItems.put(key, float: java.lang.Float)
      case double: Double => _sessionItems.put(key, double: java.lang.Double)
      case boolean: Boolean => _sessionItems.put(key, boolean: java.lang.Boolean)
      case char: Char => _sessionItems.put(key, char: java.lang.Character)
    }
  }
  
  def getAs[T](key: String): Option[T] = {
    if (_sessionItems.containsKey(key)) Some(_sessionItems.get(key).asInstanceOf[T])
    else None
  }
}

object Visit {
  def getByUuid(uuid: String)(implicit pm: ScalaPersistenceManager = null): Option[Visit] = {
    def query(epm: ScalaPersistenceManager): Option[Visit] = {
      epm.query[Visit].filter(QVisit.candidate.uuid.eq(uuid)).executeOption()
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }
  
  def allExpired(implicit pm: ScalaPersistenceManager = null): List[Visit] = {
    def query(epm: ScalaPersistenceManager): List[Visit] = {
      epm.query[Visit].filter(QVisit.candidate.expiration.lt(System.currentTimeMillis)).executeList()
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }
}

trait QVisit extends PersistableExpression[Visit] {
  private[this] lazy val _uuid: StringExpression = new StringExpressionImpl(this, "_uuid")
  def uuid: StringExpression = _uuid
  
  private[this] lazy val _user: ObjectExpression[User] = new ObjectExpressionImpl[User](this, "_user")
  def user: ObjectExpression[User] = _user
  
  private[this] lazy val _expiration: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_expiration")
  def expiration: NumericExpression[Long] = _expiration
  
  private[this] lazy val _perspective: ObjectExpression[Perspective] = new ObjectExpressionImpl[Perspective](this, "_perspective")
  def perspective: ObjectExpression[Perspective] = _perspective
  
  private[this] lazy val _permissions: CollectionExpression[java.util.Set[Permission], Permission] =
      new CollectionExpressionImpl[java.util.Set[Permission], Permission](this, "_permissions")
  def permissions: CollectionExpression[java.util.Set[Permission], Permission] = _permissions
}

object QVisit {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QVisit = {
    new PersistableExpressionImpl[Visit](parent, name) with QVisit
  }
  
  def apply(cls: Class[Visit], name: String, exprType: ExpressionType): QVisit = {
    new PersistableExpressionImpl[Visit](cls, name, exprType) with QVisit
  }
  
  private[this] lazy val jdoCandidate: QVisit = candidate("this")
  
  def candidate(name: String): QVisit = QVisit(null, name, 5)
  
  def candidate(): QVisit = jdoCandidate
  
  def parameter(name: String): QVisit = QVisit(classOf[Visit], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QVisit = QVisit(classOf[Visit], name, ExpressionType.VARIABLE)
}