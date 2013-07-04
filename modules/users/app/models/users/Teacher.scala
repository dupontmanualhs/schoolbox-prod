package models.users

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import scalajdo.DataStore

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Teacher extends Role {
  @Unique(name="TEACHER_PERSONID")
  private[this] var _personId: String = _
  def personId: String = _personId
  def personId_=(thePersonId: String) { _personId = thePersonId }
  
  @Unique(name="TEACHER_STATEID")
  private[this] var _stateId: String = _
  def stateId: String = _stateId
  def stateId_=(theStateId: String) { _stateId = theStateId }
  
  def this(user: User, personId: String, stateId: String) = {
    this()
    user_=(user)
    _personId = personId
    _stateId = stateId
  }
  
  def allStudents(term: Term): List[Student] = {
    //TODO
    Nil
  }

  def role = "Teacher"    
}

object Teacher {
  def getByUsername(username: String): Option[Teacher] = {
	User.getByUsername(username) match {
      case Some(user) => {
        val cand = QTeacher.candidate
      	DataStore.pm.query[Teacher].filter(cand.user.eq(user)).executeOption
      }
      case _ => None
	}
  }
}

trait QTeacher extends QRole[Teacher] {
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