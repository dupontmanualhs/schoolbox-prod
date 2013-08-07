package models.users

import java.util.UUID
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import scala.collection.JavaConverters._
import scala.xml.{ Elem, NodeSeq, XML }

import play.api.mvc.Request
import config.users.Config
import com.google.inject.Inject
import org.dupontmanual.forms.Call
import config.users.UsesDataStore

@PersistenceCapable(detachable="true")
class Visit {
  @PrimaryKey
  private[this] var _uuid: String = UUID.randomUUID().toString()
  def uuid: String = _uuid

  private[this] var _expiration: Long = _
  def expiration: Long = _expiration
  def expiration_=(theExpiration: Long) { _expiration = theExpiration }
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _user: User = _
  def user: Option[User] = if (_user == null) None else Some(_user)
  def user_=(maybeUser: Option[User]) { _user = maybeUser.getOrElse(null) }
  
  @Persistent(defaultFetchGroup="true")
  private[this] var _role: Role = _
  def role: Option[Role] = if (_role == null) None else Some(_role)
  def role_=(maybeRole: Option[Role]) { _role = maybeRole.getOrElse(null) }
  
  @Column(jdbcType="CLOB")
  private[this] var _menu: String = _
  def menu: NodeSeq = Visit.string2nodeSeq(_menu)
  def menu_=(theMenu: NodeSeq) { _menu = theMenu.toString }
      
  @Persistent(defaultFetchGroup = "true")
  @Element(types=Array(classOf[Permission]))
  @Join
  private[this] var _permissions: java.util.Set[Permission] = _
  def permissions: Set[Permission] = _permissions.asScala.toSet[Permission]
  def permissions_=(thePermissions: Set[Permission]) { _permissions = thePermissions.asJava }
  
  @Persistent(defaultFetchGroup = "true")
  private[this] var _redirectUrl: String = _
  def redirectUrl: Option[Call] = if (_redirectUrl == "" || _redirectUrl == null) None else Some(Call.fromXml(XML.loadString(_redirectUrl)))
  def redirectUrl_=(call: Option[Call]) { _redirectUrl = call.map(_.toXml.toString).getOrElse(null) }
  def redirectUrl_=(call: Call) { _redirectUrl = call.toXml.toString }
    
  @Persistent
  @Key(types=Array(classOf[String]))
  @Value(types=Array(classOf[Object]))
  @Serialized
  private[this] var _sessionItems: java.util.Map[String, Object] = _
  
  def this(theExpiration: Long, maybeUser: Option[User], maybeRole: Option[Role])(implicit config: Config) = {
    this()
    expiration_=(theExpiration)
    user_=(maybeUser)
    role_=(maybeRole)
    permissions_=(Set[Permission]())
    menu_=(config.menuBuilder(role))
    _sessionItems = new java.util.HashMap[String, Object]()  
    redirectUrl_=(None)
  }
  
  def isExpired: Boolean = System.currentTimeMillis > expiration
  
  def updateMenu()(implicit config: Config) {
    menu_=(config.menuBuilder(role))
  }
    
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

object Visit extends UsesDataStore {
  val visitLength = 3600000 // one hour in milliseconds
  
  def getByUuid(uuid: String): Option[Visit] = {
    dataStore.pm.query[Visit].filter(QVisit.candidate.uuid.eq(uuid)).executeOption()
  }
  
  def getFromRequest[A](implicit req: Request[A], config: Config): Visit = {
    req.session.get("visit").flatMap(
        Visit.getByUuid(_)).filter(!_.isExpired).getOrElse(
            new Visit(System.currentTimeMillis + Visit.visitLength, None, None))
  }
  
  def allExpired: List[Visit] = {
    dataStore.pm.query[Visit].filter(QVisit.candidate.expiration.lt(System.currentTimeMillis)).executeList()
  }
  
  def string2nodeSeq(legalNodeSeq: String): NodeSeq = {
    // TODO: this just crashes if someone passes in malformed XML
    val node = XML.loadString("<dummy>" + legalNodeSeq + "</dummy>")
    NodeSeq.fromSeq(node.child);
  }

}

trait QVisit extends PersistableExpression[Visit] {
  private[this] lazy val _uuid: StringExpression = new StringExpressionImpl(this, "_uuid")
  def uuid: StringExpression = _uuid
  
  private[this] lazy val _user: ObjectExpression[User] = new ObjectExpressionImpl[User](this, "_user")
  def user: ObjectExpression[User] = _user
  
  private[this] lazy val _expiration: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_expiration")
  def expiration: NumericExpression[Long] = _expiration
  
  private[this] lazy val _role: ObjectExpression[Role] = new ObjectExpressionImpl[Role](this, "_role")
  def role: ObjectExpression[Role] = _role
  
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
