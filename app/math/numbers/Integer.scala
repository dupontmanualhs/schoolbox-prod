package math.numbers

class Integer(val intVal: BigInt) extends Dec(BigDecimal(intVal, 0)) {
  def +(that: Integer): Integer = Integer(this.intVal + that.intVal)
  def -(that: Integer): Integer = Integer(this.intVal - that.intVal)
  def *(that: Integer): Integer = Integer(this.intVal * that.intVal)
  def /(that: Integer): Frac = Frac(this.intVal, that.intVal).simplified 
  def ^^(that: Integer) = {
    if (that.intVal.isValidInt) this.intVal.pow(that.intVal.toInt)
    else throw new ArithmeticException("Exponent too large: %s".format(that.intVal))
  }
  
  override def canEqual(that: Any) = that.isInstanceOf[Integer]
  override def equals(other: Any) = other match {
    case that: Integer => this.canEqual(that) && this.intVal == that.intVal
    case _ => false
  }
  
  override def toString = this.intVal.toString
  override def repr = "Integer(\"%s\")".format(this.intVal)
}

object Integer {
  def apply(value: BigInt) = new Integer(value)
}