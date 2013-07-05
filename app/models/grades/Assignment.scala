package models.grades

import java.sql.Date
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.PersistableFile
import models.courses.Section
import models.users.Student
import scalajdo.DataStore
import org.joda.time.{ LocalDate, LocalDateTime }

@PersistenceCapable(detachable = "true")
class Assignment {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }

  private[this] var _points: Int = _
  def points: Int = _points
  def points_=(thePoints: Int) { _points = thePoints }

  @Persistent(defaultFetchGroup="true")
  private[this] var _post: java.sql.Date = _
  def post: LocalDate = LocalDate.fromDateFields(_post)
  def post_=(thePost: LocalDate) { _post = new java.sql.Date(thePost.toDateTimeAtStartOfDay.getMillis) }

  @Persistent(defaultFetchGroup="true")
  private[this] var _due: java.sql.Timestamp = _
  def due: LocalDateTime = LocalDateTime.fromDateFields(_due)
  def due_=(theDue: LocalDateTime) { _due = new java.sql.Timestamp(theDue.toDate.getTime) }

  @Persistent(defaultFetchGroup="true")
  private[this] var _category: Category = _
  def category: Category = _category
  def category_=(theCategory: Category) { _category = theCategory }

  def this(theName: String, thePoints: Int, thePost: LocalDate, theDue: LocalDateTime, theCategory: Category) {
    this()
    name_=(theName)
    points_=(thePoints)
    post_=(thePost)
    due_=(theDue)
    category_=(theCategory)
  }

  def getDueDate: String = {
    due.toString
  }

  def getTurnin(student: Student): Option[Turnin] = {
    val cand = QTurnin.candidate
    DataStore.pm.query[Turnin].filter(cand.assignment.eq(this).and(cand.student.eq(student))).executeOption
  }

}

object Assignment {
  def forCategory(category: Category): List[Assignment] = {
    val cand = QAssignment.candidate
    DataStore.pm.query[Assignment].filter(cand.category.eq(category)).executeList
  }

  def getAssignments(section: Section): List[Assignment] = {
    val cand = QAssignment.candidate
    val varble = QCategory.variable("sect")
    DataStore.pm.query[Assignment].filter(cand.category.eq(varble).and(varble.section.eq(section))).executeList
  }
}

trait QAssignment extends PersistableExpression[Assignment] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name

  private[this] lazy val _points: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_points")
  def points: NumericExpression[Int] = _points

  private[this] lazy val _post: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_post")
  def post: DateExpression[java.util.Date] = _post

  private[this] lazy val _due: DateExpression[java.util.Date] = new DateExpressionImpl[java.sql.Date](this, "_due")
  def due: DateExpression[java.util.Date] = _due

  private[this] lazy val _category: ObjectExpression[Category] = new ObjectExpressionImpl[Category](this, "_category")
  def category: ObjectExpression[Category] = _category
}

object QAssignment {
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
