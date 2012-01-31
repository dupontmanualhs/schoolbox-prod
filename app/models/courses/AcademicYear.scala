package models.courses

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class AcademicYear {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _
  
  def this(name: String) = {
    this()
    _name = name
  }
  
  def id: Long = _id

  def name: String = _name
  def name_=(theName: String) { _name = theName }
}

trait QAcademicYear extends PersistableExpression[AcademicYear] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
}

object QAcademicYear {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QAcademicYear = {
    new PersistableExpressionImpl[AcademicYear](parent, name) with QAcademicYear
  }
  
  def apply(cls: Class[AcademicYear], name: String, exprType: ExpressionType): QAcademicYear = {
    new PersistableExpressionImpl[AcademicYear](cls, name, exprType) with QAcademicYear
  }
  
  private[this] lazy val jdoCandidate: QAcademicYear = candidate("this")
  
  def candidate(name: String): QAcademicYear = QAcademicYear(null, name, 5)
  
  def candidate(): QAcademicYear = jdoCandidate
  
  def parameter(name: String): QAcademicYear = QAcademicYear(classOf[AcademicYear], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QAcademicYear = QAcademicYear(classOf[AcademicYear], name, ExpressionType.VARIABLE)
}