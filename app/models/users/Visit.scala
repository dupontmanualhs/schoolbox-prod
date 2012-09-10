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

@PersistenceCapable(detachable="true")
class Visit {
  @PrimaryKey
  private[this] var _uuid: String = UUID.randomUUID().toString()
  private[this] var _expiration: Long = _
  private[this] var _user: User = _
  private[this] var _perspective: Perspective = _
  private[this] var _permissions: java.util.Set[Permission] = _
  @Column(jdbcType="CLOB")
  private[this] var _menu: String = _
  @Element(types=Array(classOf[Question]))
  @Join
  private[this] var _SAndQ: java.util.List[Question] = _
  private[this] var _quiz: Quiz = _
  @Element(types=Array(classOf[Question]))
  @Join
  private[this] var _LQ: java.util.List[Question] = _
  @Element(types=Array(classOf[Question]))
  @Join
  private[this] var _LA: java.util.List[String] = _
  
  def this(theExpiration: Long, maybeUser: Option[User], maybePerspective: Option[Perspective]) = {
    this()
    _expiration = theExpiration
    _user = maybeUser.getOrElse(null)
    _perspective = maybePerspective.getOrElse(null)
    permissions_=(Set[Permission]())
    menu_=(Menu.buildMenu(perspective))
    SAndQ = List[Question]()
    _quiz = new Quiz(null, null)
    LQ = List[Question]()
    LA = List[String]()
    
  }
  
  def uuid: String = _uuid
  
  def expiration: Long = _expiration
  def expiration_=(theExpiration: Long) { _expiration = theExpiration }
  
  def user: Option[User] = if (_user == null) None else Some(_user)
  def user_=(maybeUser: Option[User]) { _user = maybeUser.getOrElse(null) }
  
  def perspective: Option[Perspective] = if (_perspective == null) None else Some(_perspective)
  def perspective_=(maybePerspective: Option[Perspective]) { _perspective = maybePerspective.getOrElse(null) }
  
  def permissions: Set[Permission] = _permissions.asScala.toSet[Permission]
  def permissions_=(thePermissions: Set[Permission]) { _permissions = thePermissions.asJava }
  
  def menu: Elem = string2elem(_menu)
  def menu_=(theMenu: Elem) { _menu = theMenu.toString }
  
  def SAndQ: List[Question] ={ _SAndQ.asScala.toList }
  def SAndQ_=(theSAndQ: List[Question]) { _SAndQ = theSAndQ.asJava }
  
  def LQ: List[Question] = { _LQ.asScala.toList }
  def LQ_=(theLQ: List[Question]) { _LQ = theLQ.asJava }
  
  def LA: List[String] = { _LA.asScala.toList }
  def LA_=(theLA: List[String]) { _LA = theLA.asJava }
  
  def isExpired: Boolean = System.currentTimeMillis > expiration
  
  def updateMenu { menu = Menu.buildMenu(perspective) }
  
  def updateListOfQuestions(nSAQ: List[Question]){ _SAndQ = nSAQ.asJava }
  def updateLQ(nLQ: List[Question]){ _LQ = nLQ.asJava }
  def updateLA(nLA: List[String]){ _LA = nLA.asJava }
  def updateQuiz(nQuiz: Quiz) { _quiz = nQuiz }
  def getQuiz = _quiz
  def getLQ = _LQ.asScala.toList
  def getLA = _LA.asScala.toList
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