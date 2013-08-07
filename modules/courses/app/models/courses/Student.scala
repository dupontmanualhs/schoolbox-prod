package models.courses

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.QRole
import models.users.Role
import models.users.User
import models.users.Permission
import config.users.UsesDataStore
import models.users.QUser

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Student extends Role {
  @Unique(name="STUDENT_STATEID")
  @Column(allowsNull="true")
  private[this] var _stateId: String = _
  def stateId: String = _stateId
  def stateId_=(theStateId: String) { 
    if (theStateId == "") _stateId = null
    else _stateId = theStateId
  }
  
  @Unique(name="STUDENT_STUDENTNUMBER")
  private[this] var _studentNumber: String = _
  def studentNumber: String = _studentNumber
  def studentNumber_=(theStudentNumber: String) { _studentNumber = theStudentNumber }
  
  private[this] var _grade: java.lang.Integer = _
  def grade: Option[Int] = if (_grade == null) None else Some(_grade.intValue)
  def grade_=(theGrade: Option[Int]) { _grade = if (theGrade.isDefined) theGrade.get else null.asInstanceOf[java.lang.Integer] }
  def grade_=(theGrade: Int) { _grade =  theGrade }
  
  private[this] var _teamName: String = _
  def teamName: String = _teamName
  def teamName_=(theTeamName: String) { _teamName = theTeamName }
    
  def this(user: User, stateId: String, studentNumber: String, grade: Int, teamName: String) = {
    this()
    user_=(user)
    stateId_=(stateId)
    studentNumber_=(studentNumber)
    grade_=(grade)
    teamName_=(teamName) 
  }
  
  def role = "Student"
}

object Student extends UsesDataStore {
  object Permissions {
    val Add = Permission(classOf[Student], 1, "Add", "can add a student")
    val View = Permission(classOf[StudentEnrollment], 2, "View", "can view any student's information")
  }
  
  def getByUsername(username: String): Option[Student] = {
    val cand = QStudent.candidate
    val userVar = QUser.variable("userVar")
    dataStore.pm.query[Student].filter(cand.user.eq(userVar).and(userVar.username.eq(username))).executeOption()
  }
  
  def getByStudentNumber(studentNumber: String): Option[Student] = {
    val cand = QStudent.candidate
    dataStore.pm.query[Student].filter(cand.studentNumber.eq(studentNumber)).executeOption()
  }

  def getByStateId(stateId: String): Option[Student] = {
    val cand = QStudent.candidate
    dataStore.pm.query[Student].filter(cand.stateId.eq(stateId)).executeOption()
  }
  
}

trait QStudent extends QRole[Student] {
  private[this] lazy val _stateId: StringExpression = new StringExpressionImpl(this, "_stateId")
  def stateId: StringExpression = _stateId
  
  private[this] lazy val _studentNumber: StringExpression = new StringExpressionImpl(this, "_studentNumber")
  def studentNumber: StringExpression = _studentNumber
  
  private[this] lazy val _grade: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_grade")
  def grade: NumericExpression[Int] = _grade
  
  private[this] lazy val _teamName: StringExpression = new StringExpressionImpl(this, "_teamName")
  def teamName: StringExpression = _teamName
}

object QStudent {
  def apply(parent: PersistableExpression[Student], name: String, depth: Int): QStudent = {
    new PersistableExpressionImpl[Student](parent, name) with QStudent
  }
  
  def apply(cls: Class[Student], name: String, exprType: ExpressionType): QStudent = {
    new PersistableExpressionImpl[Student](cls, name, exprType) with QStudent
  }
  
  private[this] lazy val jdoCandidate: QStudent = candidate("this")
  
  def candidate(name: String): QStudent = QStudent(null, name, 5)
  
  def candidate(): QStudent = jdoCandidate
  
  def parameter(name: String): QStudent = QStudent(classOf[Student], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QStudent = QStudent(classOf[Student], name, ExpressionType.VARIABLE)
}
