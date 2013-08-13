package models.grades

import java.sql.Date
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.PersistableFile
import models.courses.Section
import config.users.UsesDataStore

@PersistenceCapable(detachable = "true")
class Category {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }

  private[this] var _weight: Double = _
  def weight: Double = _weight
  def weight_=(theWeight: Double) { _weight = theWeight }

  @Persistent(defaultFetchGroup="true")
  private[this] var _section: Section = _
  def section: Section = _section
  def section_=(theSection: Section) { _section = theSection }

  def this(theName: String, theSection: Section, theWeight: Double) {
    this()
    name_=(theName)
    weight_=(theWeight)
    section_=(theSection)
  }
}

object Category extends UsesDataStore {
  def forSection(section: Section): List[Category] = {
    val cand = QCategory.candidate
    dataStore.pm.query[Category].filter(cand.section.eq(section)).executeList
  }

}

trait QCategory extends PersistableExpression[Category] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name

  private[this] lazy val _section: ObjectExpression[Section] = new ObjectExpressionImpl[Section](this, "_section")
  def section: ObjectExpression[Section] = _section

  private[this] lazy val _weight: NumericExpression[Double] = new NumericExpressionImpl[Double](this, "_weight")
  def weight: NumericExpression[Double] = _weight
}

object QCategory {
  def apply(parent: PersistableExpression[Category], name: String, depth: Int): QCategory = {
    new PersistableExpressionImpl[Category](parent, name) with QCategory
  }

  def apply(cls: Class[Category], name: String, exprType: ExpressionType): QCategory = {
    new PersistableExpressionImpl[Category](cls, name, exprType) with QCategory
  }

  private[this] lazy val jdoCandidate: QCategory = candidate("this")

  def candidate(name: String): QCategory = QCategory(null, name, 5)

  def candidate(): QCategory = jdoCandidate

  def parameter(name: String): QCategory = QCategory(classOf[Category], name, ExpressionType.PARAMETER)

  def variable(name: String): QCategory = QCategory(classOf[Category], name, ExpressionType.VARIABLE)
}
