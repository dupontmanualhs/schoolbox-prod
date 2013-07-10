package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import models.courses.Student
import org.joda.time.LocalDate

@PersistenceCapable(detachable="true")
class Checkout {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  @Persistent
  private[this] var _student: Student = _
  def student: Student = _student
  def student_=(theStudent: Student) { _student = theStudent }

  @Persistent
  private[this] var _copy: Copy = _
  def copy: Copy = _copy
  def copy_=(theCopy: Copy) { _copy = theCopy }

  @Persistent
  private[this] var _startDate: java.sql.Date = _
  def startDate: Option[LocalDate] = Option(_startDate).map(d => LocalDate.fromDateFields(d))
  def startDate_=(theStartDate: Option[LocalDate]) {
    if (theStartDate.isDefined) _startDate = new java.sql.Date(theStartDate.get.toDateTimeAtStartOfDay.getMillis)
    else _startDate = null
  }

  @Persistent
  private[this] var _endDate: java.sql.Date = _
  def endDate: Option[LocalDate] = Option(_endDate).map(d => LocalDate.fromDateFields(d))
  def endDate_=(theEndDate: Option[LocalDate]) { 
    if (theEndDate.isDefined) _endDate = new java.sql.Date(theEndDate.get.toDateTimeAtStartOfDay.getMillis)
    else _endDate = null
  }
  
  def this(theStudent: Student, theCopy: Copy, theStartDate: Option[LocalDate], theEndDate: Option[LocalDate]) = {
    this()
    student_=(theStudent)
    copy_=(theCopy)
    startDate_=(theStartDate)
    endDate_=(theEndDate)
  }

  override def toString: String = {
    "Checkout: Copy %s to %s from %s to %s".format(copy, student.displayName, startDate, endDate)
  }
}

object Checkout {
  //TODO - Write the jdoPreStore method
}

trait QCheckout extends PersistableExpression[Checkout] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student

  private[this] lazy val _copy: ObjectExpression[Copy] = new ObjectExpressionImpl[Copy](this, "_copy")
  def copy: ObjectExpression[Copy] = _copy

  private[this] lazy val _startDate: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_startDate")
  def startDate: ObjectExpression[java.sql.Date] = _startDate

  private[this] lazy val _endDate: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_endDate")
  def endDate: ObjectExpression[java.sql.Date] = _endDate
}

object QCheckout {
  def apply(parent: PersistableExpression[Checkout], name: String, depth: Int): QCheckout = {
    new PersistableExpressionImpl[Checkout](parent, name) with QCheckout
  }

  def apply(cls: Class[Checkout], name: String, exprType: ExpressionType): QCheckout = {
    new PersistableExpressionImpl[Checkout](cls, name, exprType) with QCheckout
  }

  private[this] lazy val jdoCandidate: QCheckout = candidate("this")

  def candidate(name: String): QCheckout = QCheckout(null, name, 5)

  def candidate(): QCheckout = jdoCandidate

  def parameter(name: String): QCheckout = QCheckout(classOf[Checkout], name, ExpressionType.PARAMETER)

  def variable(name: String): QCheckout = QCheckout(classOf[Checkout], name, ExpressionType.VARIABLE)
}
