package models.lockers

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users._
import util._
import util.Helpers._
import scala.xml._

@PersistenceCapable(detachable="true")
class Locker {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _number: Int = _
  
  private[this] var _combination: String = _
  private[this] var _location: LockerLocation = _
  private[this] var _student: Option[Student] = _
  private[this] var _taken: Boolean = _
  
  def this(number: Int, combination: String, location: LockerLocation, student: Option[Student], taken: Boolean) {
     this()
     _number = number
     _combination = combination
     _location = location
     _student = student
     _taken = taken
  }
  
  def id: Long = _id
  
  def number: Int = _number
  def number_=(theNumber: Int) = (_number = theNumber)
  
  def combination: String = _combination
  def combination_=(theCombination: String) = (_combination = theCombination)
  
  def location: LockerLocation = _location
  def location_=(theLocation: LockerLocation) = (_location = theLocation)
  
  def student: Option[Student] = _student
  def student_=(theStudent: Option[Student]) = (_student = theStudent)
  
  def taken: Boolean = _taken
  def taken_=(theStatus: Boolean) = (_taken = theStatus)
  
  def available = if(taken) "No" else "Yes"
  
  def asHtml: Elem = {
    <tr><td>number</td><td>location</td><td>available</td></tr>
  }
  
  def matchingLocation(loc: LockerLocation): Boolean = {
    location.floor == loc.floor && location.hall == loc.hall
  }
  
  override def toString = student match {
    case None => "Locker #%d -- Available".format(number)
    case Some(s) => "Locker #%d -- Taken by %s".format(number, s.formalName)
  }
}

object Locker {
  def getById(id: Long)(implicit pm: ScalaPersistenceManager = null): Option[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.id.eq(id)).executeOption()
    }
  }
  
  def getByStudent(stu: Student)(implicit pm: ScalaPersistenceManager = null): Option[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.student.eq(Some(stu))).executeOption()
    }
  }
  
  def getByNumber(number: Int)(implicit pm: ScalaPersistenceManager = null): Option[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.number.eq(number)).executeOption()
    }
  }
  
  def availableLockers()(implicit pm: ScalaPersistenceManager = null): List[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.taken.eq(false)).executeList()
    }
  }
  
  def allLockers()(implicit pm: ScalaPersistenceManager = null): List[Locker] = {
    DataStore.execute { epm =>
      val cand = QLocker.candidate
      epm.query[Locker].filter(cand.taken.eq(false).or(cand.taken.eq(true))).executeList()
    }
  }
  
  def validateLockerNumber(number: String)(implicit pm: ScalaPersistenceManager = null) : Option[Locker] = {
    if(isNumber(number)) getByNumber(toInt(number))
    else None
  }
}

trait QLocker extends PersistableExpression[Locker] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _number: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_number")
  def number: NumericExpression[Int] = _number
  
  private[this] lazy val _combination: StringExpression = new StringExpressionImpl(this, "_combination")
  def combination: StringExpression = _combination
  
  private[this] lazy val _location: ObjectExpression[LockerLocation] = new ObjectExpressionImpl(this, "_location")
  def location: ObjectExpression[LockerLocation] = _location
  
  private[this] lazy val _student: ObjectExpression[Option[Student]] = new ObjectExpressionImpl(this, "_student")
  def student: ObjectExpression[Option[Student]] = _student
  
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
