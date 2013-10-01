package models.courses

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.users.QRole
import models.users.Role
import models.users.{User, Gender}
import models.users.Permission
import config.users.UsesDataStore
import models.users.QUser
import org.dupontmanual.forms.{ Binding, InvalidBinding, ValidBinding, Call, Method, Form }
import org.dupontmanual.forms.fields._
import org.dupontmanual.forms.widgets._
import org.dupontmanual.forms.validators._

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
  
  @Column(allowsNull="true")
  private[this] var _grade: java.lang.Integer = _
  def grade: Option[Int] = if (_grade == null) None else Some(_grade.intValue)
  def grade_=(theGrade: Option[Int]) { _grade = if (theGrade.isDefined) new Integer(theGrade.get) else null.asInstanceOf[java.lang.Integer] }
  def grade_=(theGrade: Int) { _grade =  new Integer(theGrade) }
  
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
    
  def guardians(): List[Guardian] = {
    val guardCand = QGuardian.candidate()
    dataStore.pm.query[Guardian].filter(guardCand.children.contains(this)).executeList()
  }
  
  def activeEnrollments(term: Term): List[StudentEnrollment] = {
    val sectVar = QSection.variable("sectVar")
    val cand = QStudentEnrollment.candidate()
    dataStore.pm.query[StudentEnrollment].filter(cand.student.eq(this).and(
        cand.end.eq(null.asInstanceOf[java.sql.Date])).and(
        cand.section.eq(sectVar)).and(sectVar.terms.contains(term))).executeList()
  }
    
  override def canEqual(that: Any): Boolean = that.isInstanceOf[Student]
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
  
  object StudentList {
    val cand = QStudent.candidate
    val userVar = QUser.variable("userVar")
    lazy val students = dataStore.pm.query[Student].filter(
        cand.user.eq(userVar).and(userVar.isActive.eq(true))).executeList()
    lazy val studentsIds = students.map(s => {
          val num = List(s.stateId, s.studentNumber).find(x => x != null && x != "").getOrElse("0000000000")
          s"${s.formalName} - $num"
        })
  }
  
  class StudentField(name: String, list: List[String]) extends BaseAutocompleteField[Student](name, list) {
    def asValue(strs: Seq[String]): Either[ValidationError, Student] = {
      dataStore.execute { pm =>
        if (strs.size == 1 && !strs(0).isEmpty) {
          val s = strs(0)
          val sId = s.split("-").last.trim
          val cand = QStudent.candidate
          val userVar = QUser.variable("userVar")
          pm.query[Student].filter(cand.stateId.eq(sId).or(cand.studentNumber.eq(sId)).or(cand.user.eq(userVar).and(userVar.username.eq(sId)))).executeOption() match {
            case Some(stu) => Right(stu)
            case _ => Left(ValidationError("Student not found"))
          }
        } else {
          Left(ValidationError("Please enter only one string"))
        }
      }
    }
  }

  class StudentIdField(name: String, list: List[String]) extends BaseAutocompleteField[String](name, list) {
    def asValue(strs: Seq[String]): Either[ValidationError, String] = {
      dataStore.execute { pm =>
        if (strs.size == 1 && !strs(0).isEmpty) {
          val s = strs(0)
          val sId = s.split("-").last.trim
          val cand = QStudent.candidate
          pm.query[Student].filter(cand.stateId.eq(sId).or(cand.studentNumber.eq(sId))).executeOption() match {
            case Some(stu) => Right(sId)
            case _ => Left(ValidationError("Student not found"))
          }
        } else {
          Left(ValidationError("Please enter only one string"))
        }
      }
    }
  }
  
  val sampleStudent = new Student(new User("", "", None, "", None, Gender.NotListed, "", ""),
		  						  "", "", 0, "")
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
