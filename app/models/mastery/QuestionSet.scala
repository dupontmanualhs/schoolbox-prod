package models.mastery
import javax.jdo.annotations._

import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class QuestionSet {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _

  @Column(allowsNull="false")
  private[this] var _howMany: Int = _
  
  @Column(allowsNull="false")
  private[this] var _kind: Kind = _
  
  def this(howMany: Int, kind: Kind) = {
    this()
    //TODO: make sure howMany is positive and smaller than the total number of kind
    howMany_=(howMany)
    kind_=(kind)
  }
  
  def id: Long = _id
  
  def howMany: Int = _howMany
  def howMany_=(theHowMany: Int) { _howMany = theHowMany }
  
  def kind: Kind = _kind
  def kind_=(theKind: Kind) { _kind = theKind }
}

trait QQuestionSet extends PersistableExpression[QuestionSet] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id

  private[this] lazy val _howMany: NumericExpression[Int] = new NumericExpressionImpl[Int](this, "_howMany")
  def howMany: NumericExpression[Int] = _howMany
  
  private[this] lazy val _kind: ObjectExpression[Kind] = new ObjectExpressionImpl[Kind](this, "_kind")
  def kind: ObjectExpression[Kind] = _kind
}

object QQuestionSet {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QQuestionSet = {
    new PersistableExpressionImpl[QuestionSet](parent, name) with QQuestionSet
  }
  
  def apply(cls: Class[QuestionSet], name: String, exprType: ExpressionType): QQuestionSet = {
    new PersistableExpressionImpl[QuestionSet](cls, name, exprType) with QQuestionSet
  }
  
  private[this] lazy val jdoCandidate: QQuestionSet = candidate("this")
  
  def candidate(name: String): QQuestionSet = QQuestionSet(null, name, 5)
  
  def candidate(): QQuestionSet = jdoCandidate
  
  def parameter(name: String): QQuestionSet = QQuestionSet(classOf[QuestionSet], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QQuestionSet = QQuestionSet(classOf[QuestionSet], name, ExpressionType.VARIABLE)
}

