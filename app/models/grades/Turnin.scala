package models.grades

import java.sql.Timestamp
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

import models.users.Student
import util.PersistableFile

@PersistenceCapable(detachable = "true")
class Turnin {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  @Persistent(defaultFetchGroup = "true")
  private[this] var _date: java.sql.Timestamp = _
  @Persistent(defaultFetchGroup = "true")
  private[this] var _assignment: Assignment = _
  @Persistent(defaultFetchGroup = "true")
  private[this] var _student: Student = _
  private[this] var _points: Double = _

  def this(student: Student, date: java.sql.Timestamp, assignment: Assignment, points: Double) {
    this()
    _date = date
    _assignment = assignment
    _student = student
    _points = points
  }

  def id: Long = _id

  def date: java.sql.Timestamp = _date
  def date_=(theDate: java.sql.Timestamp) { _date = theDate }

  def assignment: Assignment = _assignment
  def assignment_=(theAssignment: Assignment) { _assignment = theAssignment }

  def student: Student = _student
  def student_=(theStudent: Student) { _student = theStudent }

  def points: Double = _points
  def points_=(thePoints: Double) { _points = thePoints }

}

trait QTurnin extends PersistableExpression[Turnin] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _date: ObjectExpression[java.sql.Timestamp] = new ObjectExpressionImpl[java.sql.Timestamp](this, "_date")
  def date: ObjectExpression[java.sql.Timestamp] = _date

  private[this] lazy val _assignment: ObjectExpression[Assignment] = new ObjectExpressionImpl[Assignment](this, "_assignment")
  def assignment: ObjectExpression[Assignment] = _assignment

  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student

  private[this] lazy val _points: ObjectExpression[Double] = new ObjectExpressionImpl[Double](this, "_points")
  def points: ObjectExpression[Double] = _points

}

object QTurnin {
  def apply(parent: PersistableExpression[Turnin], name: String, depth: Int): QTurnin = {
    new PersistableExpressionImpl[Turnin](parent, name) with QTurnin
  }

  def apply(cls: Class[Turnin], name: String, exprType: ExpressionType): QTurnin = {
    new PersistableExpressionImpl[Turnin](cls, name, exprType) with QTurnin
  }

  private[this] lazy val jdoCandidate: QTurnin = candidate("this")

  def candidate(name: String): QTurnin = QTurnin(null, name, 5)

  def candidate(): QTurnin = jdoCandidate

  def parameter(name: String): QTurnin = QTurnin(classOf[Turnin], name, ExpressionType.PARAMETER)

  def variable(name: String): QTurnin = QTurnin(classOf[Turnin], name, ExpressionType.VARIABLE)
}