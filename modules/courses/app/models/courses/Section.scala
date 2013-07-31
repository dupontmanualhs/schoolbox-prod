package models.courses

import javax.jdo.annotations._
import scala.collection.JavaConverters._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import scala.xml.NodeSeq
import org.joda.time.{ LocalDate, ReadablePartial }

import scalajdo.DataStore

@PersistenceCapable(detachable = "true")
class Section {  
  // TODO: this needs to be in a util class; maybe in ScalaJDO once we put in date stuff?
  implicit object LocalDateOrdering extends Ordering[LocalDate] {
    def compare(ld1: LocalDate, ld2: LocalDate) = ld1.compareTo(ld2)
  }

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  @Persistent
  private[this] var _course: Course = _
  def course: Course = _course
  def course_=(theCourse: Course) { _course = theCourse }

  @Unique
  private[this] var _sectionId: String = _
  def sectionId: String = _sectionId
  def sectionId_=(theSectionId: String) { _sectionId = theSectionId }

  @Persistent
  @Element(types = Array(classOf[Term]))
  @Join
  private[this] var _terms: java.util.Set[Term] = _
  def terms: Set[Term] = _terms.asScala.toSet
  def terms_=(theTerms: Set[Term]) { _terms = theTerms.asJava }

  @Persistent
  @Element(types = Array(classOf[Period]))
  @Join
  private[this] var _periods: java.util.Set[Period] = _
  def periods: Set[Period] = _periods.asScala.toSet
  def periods_=(thePeriods: Set[Period]) { _periods = thePeriods.asJava }

  private[this] var _room: Room = _
  def room: Room = _room
  def room_=(theRoom: Room) { _room = theRoom }

  def this(course: Course, sectionId: String, terms: Set[Term], periods: Set[Period], room: Room) = {
    this()
    _course = course
    _sectionId = sectionId
    terms_=(terms)
    periods_=(periods)
    _room = room
  }

  override def toString: String = s"Section(${course.name} - ${periodNames} - ${room.name})"

  def displayName: String = s"${course.name} - ${periodNames}, ${room.name} - ${teachers.mkString(", ")}"

  def labelName: String = s"${course.name} - ${periodNames}"

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
  def teachers(): List[Teacher] = {
    val cand = QTeacherAssignment.candidate
    val assignments = DataStore.pm.query[TeacherAssignment].filter(cand.section.eq(this)).executeList()
    assignments.map(_.teacher)
  }

  // TODO: figure out which students to get in what order
  def students(): List[Student] = this.enrollments().map(_.student)

  def enrollments(): List[StudentEnrollment] = {
    val cand = QStudentEnrollment.candidate
    DataStore.pm.query[StudentEnrollment].filter(cand.section.eq(this)).executeList()
  }
  
  def numStudents(): Long = {
    val cand = QStudentEnrollment.candidate
    DataStore.pm.query[StudentEnrollment].filter(
        cand.section.eq(this).and(
            cand.end.eq(null.asInstanceOf[java.sql.Date]))).executeResultUnique(false, cand.count()).asInstanceOf[Long]
  }
}

object Section {
  def getBySectionId(sectionId: String): Option[Section] = {
    val cand = QSection.candidate
    DataStore.pm.query[Section].filter(cand.sectionId.eq(sectionId)).executeOption()
  }

  def getById(id: Long): Option[Section] = {
    val cand = QSection.candidate
    DataStore.pm.query[Section].filter(cand.id.eq(id)).executeOption()
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
    new CollectionExpressionImpl[java.util.Set[Period], Period](this, "_periods")
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
