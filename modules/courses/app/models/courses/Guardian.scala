package models.courses

import scala.collection.JavaConverters._
import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users.QRole
import models.users.Role
import models.users.User
import config.users.UsesDataStore
import models.users.QUser

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Guardian extends Role {
  @Persistent
  @Element(types=Array(classOf[Student]))
  private[this] var _children: java.util.Set[Student] = _
  def children: Set[Student] = _children.asScala.toSet
  def children_=(theChildren: Set[Student]) { _children = theChildren.asJava }
    
  def this(theUser: User, theChildren: Set[Student]){
    this()
    user_=(theUser)
    children_=(theChildren)
  }
  
  def role = "Parent/Guardian"
}

object Guardian extends UsesDataStore {
  def getByUsername(username: String): Option[Guardian] = {
    val cand = QGuardian.candidate
    val userVar = QUser.variable("userVar")
    dataStore.pm.query[Guardian].filter(cand.user.eq(userVar).and(userVar.username.eq(username))).executeOption()
  }
}


trait QGuardian extends QRole[Guardian] {
  private[this] lazy val _children: CollectionExpression[java.util.Set[Student], Student] = 
      new CollectionExpressionImpl[java.util.Set[Student], Student](this, "_children")
}

object QGuardian {
  def apply(parent: PersistableExpression[Guardian], name: String, depth: Int): QGuardian = {
    new PersistableExpressionImpl[Guardian](parent, name) with QGuardian
  }
  
  def apply(cls: Class[Guardian], name: String, exprType: ExpressionType): QGuardian = {
    new PersistableExpressionImpl[Guardian](cls, name, exprType) with QGuardian
  }
  
  private[this] lazy val jdoCandidate: QGuardian = candidate("this")
  
  def candidate(name: String): QGuardian = QGuardian(null, name, 5)
  
  def candidate(): QGuardian = jdoCandidate
  
  def parameter(name: String): QGuardian = QGuardian(classOf[Guardian], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QGuardian = QGuardian(classOf[Guardian], name, ExpressionType.VARIABLE)  
}