package models.users

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

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
  
  def displayName = user.displayName
  def formalName = user.formalName
  
  def compare(that: Perspective) = {
    this.user.compare(that.user)
  }
}

trait QPerspective[PC <: Perspective] extends PersistableExpression[PC] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _user: ObjectExpression[User] = new ObjectExpressionImpl[User](this, "_user")
  def user: ObjectExpression[User] = _user 
}
