package models.users

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.DataStore
import models.grades._

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Student extends Perspective {
  @Unique(name="STUDENT_STATEID")
  private[this] var _stateId: String = _
  @Unique(name="STUDENT_STUDENTNUMBER")
  private[this] var _studentNumber: String = _
  private[this] var _grade: Int = _
  private[this] var _teamName: String = _
  
  def this(user: User, stateId: String, studentNumber: String, grade: Int, teamName: String) = {
    this()
    user_=(user)
    _stateId = stateId
    _studentNumber = studentNumber
    _grade = grade
    _teamName = teamName 
  }
  
  def stateId: String = _stateId
  def stateId_=(theStateId: String) { _stateId = theStateId }
  
  def studentNumber: String = _studentNumber
  def studentNumber_=(theStudentNumber: String) { _studentNumber = theStudentNumber }
  
  def grade: Int = _grade
  def grade_=(theGrade: Int) { _grade = theGrade }
  
  def teamName: String = _teamName
  def teamName_=(theTeamName: String) { _teamName = theTeamName }
  
  def role = "Student"
}

object Student {
  def getByUsername(username: String)(implicit pm: ScalaPersistenceManager = null): Option[Student] = {
    def query(epm: ScalaPersistenceManager): Option[Student] = {
    User.getByUsername(username) match {
      case Some(user) => {
        val cand = QStudent.candidate
        pm.query[Student].filter(cand.user.eq(user)).executeOption
      }
      case _ => None
    }
    }
    if(pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
 }
  
  def getByStudentNumber(studentNumber: String)(implicit pm: ScalaPersistenceManager = null): Option[Student] = {
    def query(epm: ScalaPersistenceManager): Option[Student] = {
    	val cand = QStudent.candidate
    	pm.query[Student].filter(cand.studentNumber.eq(studentNumber)).executeOption()
    }
    if(pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }

  def getByStateId(stateId: String)(implicit pm: ScalaPersistenceManager = null): Option[Student] = {
    def query(epm: ScalaPersistenceManager): Option[Student] = {
    	val cand = QStudent.candidate
    	pm.query[Student].filter(cand.stateId.eq(stateId)).executeOption()
    }
    if(pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }
  
}

trait QStudent extends QPerspective[Student] {
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
