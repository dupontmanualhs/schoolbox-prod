package models.users

import java.util.UUID
import javax.jdo.annotations._
import config.users.UsesDataStore
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class Activation extends UsesDataStore with DbEquality[Activation] {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _uuid: String = _
  def uuid: String = _uuid
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _user: User = _
  def user: User = _user
  
  def this(user: User) = {
    this()
    Activation.getByUser(user) match {
      case None => // do nothing
      case Some(act) => dataStore.pm.deletePersistent(act)
    }
    _uuid = UUID.randomUUID().toString
    _user = user
  }
}

object Activation extends UsesDataStore {
  val cand = QActivation.candidate()
  
  def getByUuid(uuid: String): Option[Activation] = {
    dataStore.pm.query[Activation].filter(cand.uuid.eq(uuid)).executeOption()
  }
  
  def getByUser(user: User): Option[Activation] = {
    dataStore.pm.query[Activation].filter(cand.user.eq(user)).executeOption()
  }
}

trait QActivation extends PersistableExpression[Activation] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _uuid: StringExpression = new StringExpressionImpl(this, "_uuid")
  def uuid: StringExpression = _uuid
  
  private[this] lazy val _user: ObjectExpression[User] = new ObjectExpressionImpl[User](this, "_user")
  def user: ObjectExpression[User] = _user
}

object QActivation {
  def apply(parent: PersistableExpression[Activation], name: String, depth: Int): QActivation = {
    new PersistableExpressionImpl[Activation](parent, name) with QActivation
  }
  
  def apply(cls: Class[Activation], name: String, exprType: ExpressionType): QActivation = {
    new PersistableExpressionImpl[Activation](cls, name, exprType) with QActivation
  }
  
  private[this] lazy val jdoCandidate: QActivation = candidate("this")
  
  def candidate(name: String): QActivation = QActivation(null, name, 5)
  
  def candidate(): QActivation = jdoCandidate

  def parameter(name: String): QActivation = QActivation(classOf[Activation], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QActivation = QActivation(classOf[Activation], name, ExpressionType.VARIABLE)
}
