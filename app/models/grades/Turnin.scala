package models.grades

import java.sql.Date
import models.users.Student
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile
import util.DataStore

@PersistenceCapable(detachable = "true")
class Turnin {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _grade: Grade = _
  @Persistent(defaultFetchGroup = "true")
  private[this] var _date: java.sql.Date = _
  
  def this(student: Student, grade: Grade, date: java.sql.Date, assignment: Assignment){
    this()
    _grade = grade
    _date = date
  }
  
  def id: Long = _id
  
  def grade: Grade = _grade
  def grade_=(theGrade: Grade) { _grade = theGrade }
  
  def date: java.sql.Date = _date
  def date_=(theDate: java.sql.Date) { _date = theDate }
  
trait QTurnin extends PersistableExpression[Turnin] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _date: ObjectExpression[java.sql.Date] = new ObjectExpressionImpl[java.sql.Date](this, "_date")
  def date: ObjectExpression[java.sql.Date] = _date
  
  private[this] lazy val _grade: ObjectExpression[Grade] = new ObjectExpressionImpl(this, "_grade")
  def grade: ObjectExpression[Grade] = _grade
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

  
  
  
  
  
  
  
  
  
  
}