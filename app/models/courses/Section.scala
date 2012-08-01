package models.courses

import javax.jdo.annotations._
import scala.collection.JavaConverters._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users.{Student, Teacher}
import util.ScalaPersistenceManager
import scala.xml.NodeSeq
import util.DataStore
import org.joda.time.LocalDate

import util.Helpers.LocalDateOrdering


@PersistenceCapable(detachable="true")
class Section {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _course: Course = _
  @Unique
  private[this] var _sectionId: String = _
  @Element(types=Array(classOf[Term]))
  @Join
  private[this] var _terms: java.util.Set[Term] = _
  @Element(types=Array(classOf[Period]))
  @Join
  private[this] var _periods: java.util.Set[Period] = _
  private[this] var _room: Room = _
  
  def this(course: Course, sectionId: String, terms: Set[Term], periods: Set[Period], room: Room) = {
    this()
    _course = course
    _sectionId = sectionId
    terms_=(terms)
    periods_=(periods)
    _room = room
  }
  
  def id: Long = _id

  def course: Course = _course
  def course_=(theCourse: Course) { _course = theCourse }
  
  def sectionId: String = _sectionId
  def sectionId_=(theSectionId: String) { _sectionId = theSectionId }
  
  def terms: Set[Term] = _terms.asScala.toSet
  def terms_=(theTerms: Set[Term]) { _terms = theTerms.asJava }
  
  def room: Room = _room
  def room_=(theRoom: Room) { _room = theRoom }
  
  def periods: Set[Period] = _periods.asScala.toSet
  def periods_=(thePeriods: Set[Period]) { _periods = thePeriods.asJava }
  
  def periodNames: String = {
    periods.map(_.name).mkString(", ")
  }
  
  def startDate: LocalDate = {
    terms.map(_.start).min
  }
  
  def endDate: LocalDate = {
    terms.map(_.end).max
  }
  
  // TODO: figure out which teachers to get in what order
  def teachers(implicit pm: ScalaPersistenceManager = null): List[Teacher] = {
    def query(epm: ScalaPersistenceManager): List[Teacher] = {	
    	val cand = QTeacherAssignment.candidate
    	val assignments = pm.query[TeacherAssignment].filter(cand.section.eq(this)).executeList()
    	assignments.map(_.teacher)
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }
  
  // TODO: figure out which students to get in what order
  def students(implicit pm: ScalaPersistenceManager = null): List[Student] = {
    def query(epm: ScalaPersistenceManager): List[Student] = {	
    	enrollments.map(_.student)
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }
  
  def enrollments(implicit pm: ScalaPersistenceManager = null): List[StudentEnrollment] = {
    def query(epm: ScalaPersistenceManager): List[StudentEnrollment] = {	
    	val cand = QStudentEnrollment.candidate
    	pm.query[StudentEnrollment].filter(cand.section.eq(this)).executeList()
    }
    if (pm != null) query(pm)
    else DataStore.withTransaction( tpm => query(tpm) )
  }
}

trait QSection extends PersistableExpression[Section] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _course: ObjectExpression[Course] = new ObjectExpressionImpl[Course](this, "_course")
  def course: ObjectExpression[Course] = _course
  
  private[this] lazy val _sectionId: StringExpression = new StringExpressionImpl(this, "_sectionId")
  def sectionId: StringExpression = _sectionId
  
  private[this] lazy val _terms: CollectionExpression[java.util.Set[Term], Term] = 
      new CollectionExpressionImpl[java.util.Set[Term], Term](this, "_terms")
  def terms: CollectionExpression[java.util.Set[Term], Term] = _terms

  private[this] lazy val _periods: CollectionExpression[java.util.Set[Period], Period] = 
      new CollectionExpressionImpl[java.util.Set[Period], Period](this, "_terms")
  def periods: CollectionExpression[java.util.Set[Period], Period] = _periods

  private[this] lazy val _room: ObjectExpression[Room] = new ObjectExpressionImpl[Room](this, "_room")
  def room: ObjectExpression[Room] = _room
}

object QSection {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QSection = {
    new PersistableExpressionImpl[Section](parent, name) with QSection
  }
  
  def apply(cls: Class[Section], name: String, exprType: ExpressionType): QSection = {
    new PersistableExpressionImpl[Section](cls, name, exprType) with QSection
  }
  
  private[this] lazy val jdoCandidate: QSection = candidate("this")
  
  def candidate(name: String): QSection = QSection(null, name, 5)
  
  def candidate(): QSection = jdoCandidate
  
  def parameter(name: String): QSection = QSection(classOf[Section], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QSection = QSection(classOf[Section], name, ExpressionType.VARIABLE)
}
