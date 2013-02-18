package util

import org.datanucleus.query.typesafe.PersistableExpression
import org.datanucleus.api.jdo.query.ExpressionType

trait QueryClass[T, QT <: PersistableExpression[T]] {
  // implement these
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QT
  def apply(cls: Class[T], name: String, exprType: ExpressionType): QT
  def myClass: Class[T]

  private[this] lazy val jdoCandidate: QT = candidate("this")
  
  def candidate(name: String): QT = apply(null, name, 5)
  
  def candidate(): QT = jdoCandidate
   
  def parameter(name: String): QT = apply(myClass, name, ExpressionType.PARAMETER)
  
  def variable(name: String): QT = apply(myClass, name, ExpressionType.VARIABLE)
}
