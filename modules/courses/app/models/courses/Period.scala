package models.courses

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import config.users.UsesDataStore

@PersistenceCapable(detachable="true")
class Period {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  private[this] var _name: String = _
  def name: String = _name
  def name_=(theName: String) { _name = theName }
  
  private[this] var _order: Int = _
  def order: Int = _order
  def order_=(theOrder: Int) { _order = theOrder }
  
  @Unique
  private[this] var _slug: String = _
  def slug: String = _slug
  def slug_=(theSlug: String) { _slug = theSlug }
  
  def this(name: String, order: Int, slug: String) {
    this()
    name_=(name)
    order_=(order)
    slug_=(slug)
  }
  
  def this(name: String, order: Int) = {
    this(name, order, Period.slugFromName(name))
  }
}

object Period extends UsesDataStore {
  def slugFromName(name: String): String = {
    val wds = name.split(" ", 2)
    val day = wds(0).substring(0, 1).toLowerCase
    val rest = wds(1).toLowerCase()
    day + rest
  }
  
  def getBySlug(slug: String): Option[Period] = {
    val cand = QPeriod.candidate
    dataStore.pm.query[Period].filter(cand.slug.eq(slug)).executeOption()
  }
}

trait QPeriod extends PersistableExpression[Period] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _order: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_name")
  def order: NumericExpression[Int] = _order
  
  private[this] lazy val _slug: StringExpression = new StringExpressionImpl(this, "_slug")
  def slug: StringExpression = _slug
}

object QPeriod {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QPeriod = {
    new PersistableExpressionImpl[Period](parent, name) with QPeriod
  }
  
  def apply(cls: Class[Period], name: String, exprType: ExpressionType): QPeriod = {
    new PersistableExpressionImpl[Period](cls, name, exprType) with QPeriod
  }
  
  private[this] lazy val jdoCandidate: QPeriod = candidate("this")
  
  def candidate(name: String): QPeriod = QPeriod(null, name, 5)
  
  def candidate(): QPeriod = jdoCandidate
  
  def parameter(name: String): QPeriod = QPeriod(classOf[Period], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QPeriod = QPeriod(classOf[Period], name, ExpressionType.VARIABLE)
}