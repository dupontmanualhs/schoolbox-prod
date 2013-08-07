package models.courses

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

import config.users.UsesDataStore

@PersistenceCapable(detachable = "true")
class Department {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }

  def this(name: String) = {
    this()
    _name = name
  }
}

object Department extends UsesDataStore {
  def getOrCreate(name: String): Department = {
    val pm = dataStore.pm
    val cand = QDepartment.candidate
    pm.query[Department].filter(cand.name.eq(name)).executeOption() match {
      case Some(dept) => dept
      case None => {
        val dept = new Department(name)
        pm.makePersistent(dept)
      }
    }
  }
}

trait QDepartment extends PersistableExpression[Department] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
}

object QDepartment {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QDepartment = {
    new PersistableExpressionImpl[Department](parent, name) with QDepartment
  }

  def apply(cls: Class[Department], name: String, exprType: ExpressionType): QDepartment = {
    new PersistableExpressionImpl[Department](cls, name, exprType) with QDepartment
  }

  private[this] lazy val jdoCandidate: QDepartment = candidate("this")

  def candidate(name: String): QDepartment = QDepartment(null, name, 5)

  def candidate(): QDepartment = jdoCandidate

  def parameter(name: String): QDepartment = QDepartment(classOf[Department], name, ExpressionType.PARAMETER)

  def variable(name: String): QDepartment = QDepartment(classOf[Department], name, ExpressionType.VARIABLE)
}
