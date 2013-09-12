package models.courses

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.QRole
import models.users.Role
import models.users.User
import config.users.UsesDataStore
import models.users.QUser
import org.dupontmanual.forms.fields._
import org.dupontmanual.forms.widgets._
import org.dupontmanual.forms.validators._


@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Teacher extends Role {
  @Unique(name="TEACHER_PERSONID")
  private[this] var _personId: String = _
  def personId: String = _personId
  def personId_=(thePersonId: String) { _personId = thePersonId }
  
  @Unique(name="TEACHER_STATEID")
  @Column(allowsNull="true")
  private[this] var _stateId: String = _
  def stateId: String = _stateId
  def stateId_=(theStateId: String) { 
    if (theStateId == "") _stateId = null
    else _stateId = theStateId
  }
  
  def this(user: User, personId: String, stateId: String) = {
    this()
    user_=(user)
    personId_=(personId)
    stateId_=(stateId)
  }
  
  /*def allStudents(term: Term): List[Student] = {
    //TODO
    Nil
  }*/

  def role = "Teacher"
    
  override def canEqual(that: Any): Boolean = that.isInstanceOf[Teacher]
}

object Teacher extends UsesDataStore {
  def getByUsername(username: String): Option[Teacher] = {
    val cand = QTeacher.candidate
    val userVar = QUser.variable("userVar")
    dataStore.pm.query[Teacher].filter(cand.user.eq(userVar).and(userVar.username.eq(username))).executeOption()
  }
  
  def getByStateId(stateId: String): Option[Teacher] = {
    val cand = QTeacher.candidate
    dataStore.pm.query[Teacher].filter(cand.stateId.eq(stateId)).executeOption()
  }
  
  def getByPersonId(personId: String): Option[Teacher] = {
    val cand = QTeacher.candidate
    dataStore.pm.query[Teacher].filter(cand.personId.eq(personId)).executeOption()
  }
  
  object TeacherList {
    val cand = QTeacher.candidate
    val userVar = QUser.variable("userVar")
    lazy val teachers = dataStore.pm.query[Teacher].filter(
        cand.user.eq(userVar).and(userVar.isActive.eq(true))).executeList()
    lazy val teacherIds = teachers.map(t => {
    	val num = List(t.stateId, t.personId).find(x => x != null && x != "").getOrElse("0000000000")
        s"${t.formalName} - $num"
    })
  }
  
  class TeacherField(name: String, list: List[String]) extends BaseAutocompleteField[Teacher](name, list) {
    def asValue(strs: Seq[String]): Either[ValidationError, Teacher] = {
      dataStore.execute { pm =>
        if (strs.size == 1 && !strs(0).isEmpty) {
          val s = strs(0)
          val tId = s.split("-").last.trim
          val cand = QTeacher.candidate
          val userVar = QUser.variable("userVar")
          pm.query[Teacher].filter(cand.stateId.eq(tId).or(cand.personId.eq(tId))).executeOption() match {
              case Some(tchr) => Right(tchr)
              case _ => Left(ValidationError("Teacher not found"))
          }
        } else {
          Left(ValidationError("Please enter only one string"))
        }
      }
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
