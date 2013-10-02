package models.users

import javax.jdo.spi.PersistenceCapable

trait DbEquality[T <: DbEquality[T]] {
  def id: Long
  
  def canEqual(that: Any): Boolean = that.isInstanceOf[DbEquality[T]]
  
  override def equals(that: Any): Boolean = that match {
    case that: DbEquality[T] => this.canEqual(that) && this.id == that.id
    case _ => false
  }
  
  override def hashCode: Int = this.id.hashCode
}