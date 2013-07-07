package models.courses

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import scalajdo.DataStore

@PersistenceCapable(detachable="true")
class Course {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }
  
  @Unique
  private[this] var _masterNumber: String = _
  def masterNumber: String = _masterNumber
  def masterNumber_=(theMasterNumber: String) { _masterNumber = theMasterNumber }
  
  @Persistent
  private[this] var _department: Department = _
  def department: Department = _department
  def department_=(theDepartment: Department) { _department = theDepartment }
  
  def this(name: String, masterNumber: String, department: Department) = {
    this()
    _name = name
    _masterNumber = masterNumber
    _department = department
  }
  
  override def toString = "%s (%s)".format(name, masterNumber)
}

object Course {
  def getByMasterNumber(masterNumber: String): Option[Course] = {
    val cand = QCourse.candidate
    DataStore.pm.query[Course].filter(cand.masterNumber.eq(masterNumber)).executeOption()
  }
}

trait QCourse extends PersistableExpression[Course] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _masterNumber: StringExpression = new StringExpressionImpl(this, "_masterNumber")
  def masterNumber: StringExpression = _masterNumber
  
  private[this] lazy val _department: ObjectExpression[Department] = new ObjectExpressionImpl[Department](this, "_department")
  def department: ObjectExpression[Department] = _department
}

object QCourse {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QCourse = {
    new PersistableExpressionImpl[Course](parent, name) with QCourse
  }
  
  def apply(cls: Class[Course], name: String, exprType: ExpressionType): QCourse = {
    new PersistableExpressionImpl[Course](cls, name, exprType) with QCourse
  }
  
  private[this] lazy val jdoCandidate: QCourse = candidate("this")
  
  def candidate(name: String): QCourse = QCourse(null, name, 5)
  
  def candidate(): QCourse = jdoCandidate
  
  def parameter(name: String): QCourse = QCourse(classOf[Course], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QCourse = QCourse(classOf[Course], name, ExpressionType.VARIABLE)
}