package models.lockers

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users._
import util.Helpers._
import scala.xml._

import scalajdo.DataStore

@PersistenceCapable(detachable="true")
class Locker {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _number: Int = _
  
  private[this] var _combination: String = _
  
  // these get combined into LockerLocation
  // we probably need to make this more generic to work with other schools
  private[this] var _floor: Int = _
  private[this] var _hall: String = _
  
  @Persistent(defaultFetchGroup = "true")
  private[this] var _student: Student = _
  private[this] var _taken: Boolean = _
  
  def this(number: Int, combination: String, location: LockerLocation, student: Option[Student], taken: Boolean) {
     this()
     _number = number
     _combination = combination
     _floor = location.floor
     _hall = location.hall
     student_=(student)
     _taken = taken
  }
  
  def id: Long = _id
  
  def number: Int = _number
  def number_=(theNumber: Int) = (_number = theNumber)
  
  def combination: String = _combination
  def combination_=(theCombination: String) = (_combination = theCombination)
  
  def location: LockerLocation = LockerLocation(_floor, _hall)
  def location_=(theLocation: LockerLocation) { _floor = theLocation.floor; _hall = theLocation.hall }
  
  def student: Option[Student] = if (_student == null) None else Some(_student)
  def student_=(theStudent: Student) { student_=(Option(theStudent)) }
  def student_=(theStudent: Option[Student]) {
    theStudent match {
      case None => _student = null
      case Some(s) => _student = s
    }
  }
  
  def taken: Boolean = _taken
  def taken_=(theStatus: Boolean) = (_taken = theStatus)
  
  def available = if(taken) "No" else "Yes"
  
  def asHtml: Elem = {
    <tr><td>@locker.number</td><td>@locker.location</td><td>@locker.available</td></tr>
  }
  
  def matchingLocation(loc: LockerLocation): Boolean = this.location == loc
  
  def studentName = student match {
    case None => "Available"
    case Some(s) => s.displayName
  }
  
  override def toString = student match {
    case None => "Locker #%d -- Available".format(number)
    case Some(s) => "Locker #%d -- Taken by %s".format(number, s.formalName)
  }
}

object Locker {
  def getById(id: Long): Option[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.id.eq(id)).executeOption()
    }
  }
  
  def getByStudent(stu: Student): Option[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.student.eq(stu)).executeOption()
    }
  }
  
  def getByNumber(number: Int): Option[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.number.eq(number)).executeOption()
    }
  }
  
  def availableLockers(): List[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.taken.eq(false)).executeList()
    }
  }
  
  def allLockers(): List[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.taken.eq(false).or(cand.taken.eq(true))).executeList()
    }
  }
}

trait QLocker extends PersistableExpression[Locker] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _number: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_number")
  def number: NumericExpression[Int] = _number
  
  private[this] lazy val _combination: StringExpression = new StringExpressionImpl(this, "_combination")
  def combination: StringExpression = _combination
  
  // can't search by this, because it's not really a field
  // private[this] lazy val _location: ObjectExpression[LockerLocation] = new ObjectExpressionImpl[LockerLocation](this, "_location")
  // def location: ObjectExpression[LockerLocation] = _location
  
  private[this] lazy val _student: ObjectExpression[Student] = new ObjectExpressionImpl[Student](this, "_student")
  def student: ObjectExpression[Student] = _student
  
  private[this] lazy val _taken: BooleanExpression = new BooleanExpressionImpl(this, "_taken")
  def taken: BooleanExpression = _taken
}

object QLocker {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QLocker = {
    new PersistableExpressionImpl[Locker](parent, name) with QLocker
  }
  
  def apply(cls: Class[Locker], name: String, exprType: ExpressionType): QLocker = {
    new PersistableExpressionImpl[Locker](cls, name, exprType) with QLocker
  }
  
  private[this] lazy val jdoCandidate: QLocker = candidate("this")
  
  def candidate(name: String): QLocker = QLocker(null, name, 5)
  
  def candidate(): QLocker = jdoCandidate
  
  def parameter(name: String): QLocker = QLocker(classOf[Locker], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QLocker = QLocker(classOf[Locker], name, ExpressionType.VARIABLE)
}
