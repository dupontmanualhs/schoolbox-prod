package models.users

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import util.ScalaPersistenceManager
import util.DataStore

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
abstract class Perspective extends Ordered[Perspective] {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  private[this] var _user: User = _
  
  protected def this(user: User) = {
    this()
    user_=(user)
  }
  
  def id: Long = _id

  def user: User = _user
  def user_=(theUser: User) { _user = theUser }
  
  def displayName = "%s (%s)".format(user.displayName, role)
  def formalName = "%s (%s)".format(user.formalName, role)
  def role: String
  
  def compare(that: Perspective) = {
    this.user.compare(that.user)
  }
}

object Perspective {
  def getById(id: Long)(implicit pm: ScalaPersistenceManager = null): Option[Perspective] = {
    def query(epm: ScalaPersistenceManager): Option[Perspective] = {
    	val cand = QPerspective.candidate
    	pm.query[Perspective].filter(cand.id.eq(id)).executeOption()
    }
    if(pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }
}

trait QPerspective[PC <: Perspective] extends PersistableExpression[PC] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _user: ObjectExpression[User] = new ObjectExpressionImpl[User](this, "_user")
  def user: ObjectExpression[User] = _user 
}

object QPerspective {
  def apply(parent: PersistableExpression[Perspective], name: String, depth: Int): QPerspective[Perspective] = {
    new PersistableExpressionImpl[Perspective](parent, name) with QPerspective[Perspective]
  }
  
  def apply(cls: Class[Perspective], name: String, exprType: ExpressionType): QPerspective[Perspective] = {
    new PersistableExpressionImpl[Perspective](cls, name, exprType) with QPerspective[Perspective]
  }
  
  private[this] lazy val jdoCandidate: QPerspective[Perspective] = candidate("this")
  
  def candidate(name: String): QPerspective[Perspective] = QPerspective(null, name, 5)
  
  def candidate(): QPerspective[Perspective] = jdoCandidate

  def parameter(name: String): QPerspective[Perspective] = QPerspective(classOf[Perspective], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QPerspective[Perspective] = QPerspective(classOf[Perspective], name, ExpressionType.VARIABLE)

}

