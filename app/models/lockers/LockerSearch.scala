package models.lockers

import play.api._
import play.api.mvc._
import play.libs.Scala._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import models.lockers._
import models.users._
import models.courses._
import forms._
import forms.fields._
import xml._
import views.html
import forms.validators.Validator
import forms.validators.ValidationError
import util.Helpers._

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users._
import util._
import util.Helpers._
import scala.xml._

@PersistenceCapable(detachable="true")
class LockerSearch {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  
  @Element(types=Array(classOf[Locker]))
  @Join
  private[this] var _lockers: java.util.List[Locker] = _
  
  def this(lockers: List[Locker]) {
    this()
    lockers_=(lockers)
  }
  
  def id: Long = _id
  
  def lockers: List[Locker] = toSeq(_lockers).toList
  def lockers_=(theLockers: List[Locker]) = (_lockers = asJava(theLockers))
}

object LockerSearch {
  def getById(id: Long)(implicit pm: ScalaPersistenceManager = null): Option[LockerSearch] = {
    DataStore.execute { epm =>
      val cand = QLockerSearch.candidate
      epm.query[LockerSearch].filter(cand.id.eq(id)).executeOption()
    }
  }
}

trait QLockerSearch extends PersistableExpression[LockerSearch] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _lockers: CollectionExpression[java.util.List[Locker], Locker] = 
      new CollectionExpressionImpl[java.util.List[Locker], Locker](this, "_lockers")
  def lockers: CollectionExpression[java.util.List[Locker], Locker] = _lockers
}

object QLockerSearch {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QLockerSearch = {
    new PersistableExpressionImpl[LockerSearch](parent, name) with QLockerSearch
  }
  
  def apply(cls: Class[LockerSearch], name: String, exprType: ExpressionType): QLockerSearch = {
    new PersistableExpressionImpl[LockerSearch](cls, name, exprType) with QLockerSearch
  }
  
  private[this] lazy val jdoCandidate: QLockerSearch = candidate("this")
  
  def candidate(name: String): QLockerSearch = QLockerSearch(null, name, 5)
  
  def candidate(): QLockerSearch = jdoCandidate
  
  def parameter(name: String): QLockerSearch = QLockerSearch(classOf[LockerSearch], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QLockerSearch = QLockerSearch(classOf[LockerSearch], name, ExpressionType.VARIABLE)
}
