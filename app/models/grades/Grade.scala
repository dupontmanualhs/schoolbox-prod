package models.grades

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile
import util.DataStore
import models.users.Student //or just models.users?

@PersistenceCapable(detachable="true")
class Grade {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _student: Student = _
  private[this] var _assignment: Assignment = _
  private[this] var _points: Double = _
  
  def this(student: Student, assignment: Assignment, points: Double) = {
    this()
    _student = student
    _assignment = assignment
    _points = points
  }
  
  def id: Long = _id
  
  def student: Student = _student
  def student_=(theStudent: Student) { _student = theStudent }
  
  def assignment: Assignment = _assignment
  def assignment_=(theAssignment: Assignment) { _assignment = theAssignment }
  
  def points: Double = _points
  def points_=(thePoints: Double) { _points = thePoints }
}

object Grade {
/*def forAssignment(assignment: Assignment)(implicit pm: ScalaPersistenceManager = null): List[Grade] = {
    def query(epm: ScalaPersistenceManager): List[Grade] = {
      val cand = QGrade.candidate
      epm.query[Grade].filter(cand.assignment.eq(assignment)).executeList
    }
    if(pm != null) query(pm)
    else  DataStore.withTransaction( tpm => query(tpm) )
  } */ // Not sure If this is actually useful
  
}

trait QGrade extends PersistableExpression[Grade] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl(this, "_student")
  def student: ObjectExpression[Student] = _student
  
  private[this] lazy val _assignment: ObjectExpression[Assignment] = new ObjectExpressionImpl[Assignment](this, "_assignment")
  def assignment: ObjectExpression[Assignment] = _assignment
  
  private[this] lazy val _points: ObjectExpression[Double] = new ObjectExpressionImpl[Double](this, "_points")
  def points: ObjectExpression[Double] = _points
}

object QGrade {
  def apply(parent: PersistableExpression[Grade], name: String, depth: Int): QGrade = {
    new PersistableExpressionImpl[Grade](parent, name) with QGrade
  }
  
  def apply(cls: Class[Grade], name: String, exprType: ExpressionType): QGrade = {
    new PersistableExpressionImpl[Grade](cls, name, exprType) with QGrade
  }
  
  private[this] lazy val jdoCandidate: QGrade = candidate("this")
  
  def candidate(name: String): QGrade = QGrade(null, name, 5)
  
  def candidate(): QGrade = jdoCandidate
  
  def parameter(name: String): QGrade = QGrade(classOf[Grade], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QGrade = QGrade(classOf[Grade], name, ExpressionType.VARIABLE)
}
