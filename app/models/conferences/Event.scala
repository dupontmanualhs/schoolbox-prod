package models.conferences

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.QueryClass
import config.users.UsesDataStore
import models.users.DbEquality

@PersistenceCapable(detachable="true")
class Event extends UsesDataStore with DbEquality[Event] {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id
  
  @Unique
  @Column(allowsNull="false")
  private[this] var _name: String = _  
  def name: String = _name
  def name_=(theName: String) {_name = theName}
  
  @Column(allowsNull="false")
  private[this] var _isActive: Boolean = _
  def isActive: Boolean = _isActive
  def isActive_=(theActivation: Boolean) {_isActive = theActivation}

  def this(name: String, isActive: Boolean) = {
    this()
    name_=(name)
    isActive_=(isActive)
  }
  
  def sessions: List[Session] = dataStore.execute { pm => 
    val cand = QSession.candidate
    pm.query[Session].filter(cand.event.eq(this)).orderBy(cand.startTime.asc).executeList()
  }
  
  override def toString(): String = s"Conference Event: ${this.name}"
}

object Event extends UsesDataStore {
  val cand = QEvent.candidate()

  def getActive(): List[Event] = {
    dataStore.pm.query[Event].filter(cand.isActive.eq(true)).executeList()
  }
  
  def getById(id: Long): Option[Event] = {
    dataStore.pm.query[Event].filter(cand.id.eq(id)).executeOption()
  }
}

trait QEvent extends PersistableExpression[Event] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _isActive: BooleanExpression = new BooleanExpressionImpl(this, "_isActive")
  def isActive: BooleanExpression = _isActive
}

object QEvent extends QueryClass[Event, QEvent] {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QEvent = {
    new PersistableExpressionImpl[Event](parent, name) with QEvent
  }
  
  def apply(cls: Class[Event], name: String, exprType: ExpressionType): QEvent = {
    new PersistableExpressionImpl[Event](cls, name, exprType) with QEvent
  }
  
  def myClass = classOf[Event]
}