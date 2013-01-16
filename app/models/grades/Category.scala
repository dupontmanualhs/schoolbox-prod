package models.grades

import java.sql.Date
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile
import util.DataStore
import models.courses.Section

@PersistenceCapable(detachable="true")
class Category {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _name: String = _
  private[this] var _weight: Double = _
  private[this] var _section: Section = _
  
  def this(name: String, section: Section, weight: Double) {
    this()
    _name = name
    _weight = weight
    _section = section
  }
  
  def id: Long = _id
  
  def name: String = _name
  def name_=(theName: String) { _name = theName }
  
  def section: Section = _section
  def section_=(theSection: Section) { _section = theSection }
  
  
  def weight: Double = _weight
  def weight_=(theWeight: Double) { _weight = theWeight }
}

object Category {
  
}

trait QCategory extends PersistableExpression[Assignment] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section
  
  private[this] lazy val _weight: ObjectExpression[Double] = new ObjectExpressionImpl[Double](this, "_weight")
  def weight: ObjectExpression[Double] = _weight
}

object QCategory {
  def apply(parent: PersistableExpression[Assignment], name: String, depth: Int): QAssignment = {
    new PersistableExpressionImpl[Assignment](parent, name) with QAssignment
  }
  
  def apply(cls: Class[Assignment], name: String, exprType: ExpressionType): QAssignment = {
    new PersistableExpressionImpl[Assignment](cls, name, exprType) with QAssignment
  }
  
  private[this] lazy val jdoCandidate: QAssignment = candidate("this")
  
  def candidate(name: String): QAssignment = QAssignment(null, name, 5)
  
  def candidate(): QAssignment = jdoCandidate
  
  def parameter(name: String): QAssignment = QAssignment(classOf[Assignment], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QAssignment = QAssignment(classOf[Assignment], name, ExpressionType.VARIABLE)
}
