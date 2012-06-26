package models.users

import scala.collection.JavaConverters._
import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Guardian extends Perspective {
  private[this] var _children: java.util.Set[Student] = _
  
  def children: Set[Student] = _children.asScala.toSet
  def children_=(theChildren: Set[Student]) { _children = theChildren.asJava }
}

trait QGuardian extends QPerspective[Guardian] {
  private[this] lazy val _children: CollectionExpression[java.util.List[Student], Student] = 
      new CollectionExpressionImpl[java.util.List[Student], Student](this, "_children")
}

object QGuardian {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QGuardian = {
    new PersistableExpressionImpl[Guardian](parent, name) with QPerspective[Guardian] with QGuardian
  }
  
  def apply(cls: Class[Guardian], name: String, exprType: ExpressionType): QGuardian = {
    new PersistableExpressionImpl[Guardian](cls, name, exprType) with QPerspective[Guardian] with QGuardian
  }
  
  private[this] lazy val jdoCandidate: QGuardian = candidate("this")
  
  def candidate(name: String): QGuardian = QGuardian(null, name, 5)
  
  def candidate(): QGuardian = jdoCandidate
  
  def parameter(name: String): QGuardian = QGuardian(classOf[Guardian], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QGuardian = QGuardian(classOf[Guardian], name, ExpressionType.VARIABLE)  
}