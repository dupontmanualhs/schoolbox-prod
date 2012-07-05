package models.users

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.DataStore
import util.ScalaPersistenceManager

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Teacher extends Perspective {
  @Unique(name="TEACHER_PERSONID")
  private[this] var _personId: String = _
  @Unique(name="TEACHER_STATEID")
  private[this] var _stateId: String = _
  
  def this(user: User, personId: String, stateId: String) = {
    this()
    user_=(user)
    _personId = personId
    _stateId = stateId
  }
  
  def personId: String = _personId
  def personId_=(thePersonId: String) { _personId = thePersonId }
  
  def stateId: String = _stateId
  def stateId_=(theStateId: String) { _stateId = theStateId }
}

object Teacher {
  def getByUsername(username: String)(implicit pm: ScalaPersistenceManager): Option[Teacher] = {
    User.getByUsername(username) match {
      case Some(user) => {
        val cand = QTeacher.candidate
        pm.query[Teacher].filter(cand.user.eq(user)).executeOption
      }
      case _ => None
    }
  }
}

trait QTeacher extends QPerspective[Teacher] {
  private[this] lazy val _personId: StringExpression = new StringExpressionImpl(this, "_personId")
  def personId: StringExpression = _personId
  
  private[this] lazy val _stateId: StringExpression = new StringExpressionImpl(this, "_stateId")
  def stateId: StringExpression = _stateId
}

object QTeacher {
  def apply(parent: PersistableExpression[Teacher], name: String, depth: Int): QTeacher = {
    new PersistableExpressionImpl[Teacher](parent, name) with QTeacher
  }
  
  def apply(cls: Class[Teacher], name: String, exprType: ExpressionType): QTeacher = {
    new PersistableExpressionImpl[Teacher](cls, name, exprType) with QTeacher
  }
  
  private[this] lazy val jdoCandidate: QTeacher = candidate("this")
  
  def candidate(name: String): QTeacher = QTeacher(null, name, 5)
  
  def candidate(): QTeacher = jdoCandidate
  
  def parameter(name: String): QTeacher = QTeacher(classOf[Teacher], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QTeacher = QTeacher(classOf[Teacher], name, ExpressionType.VARIABLE)
}