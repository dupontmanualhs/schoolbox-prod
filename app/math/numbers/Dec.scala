package math.numbers

import java.math.BigInteger
import scala.math.max

class Dec(val decVal: BigDecimal) extends Frac(Dec.numerator(decVal), Dec.denominator(decVal)) {
  def +(that: Dec): Dec = Dec(this.decVal + that.decVal)
  def -(that: Dec): Dec = Dec(this.decVal - that.decVal)
  def *(that: Dec) = Dec(this.decVal * that.decVal)
  def /(that: Dec) = {
    try {
      new Dec(this.decVal.bigDecimal.divide(that.decVal.bigDecimal))
    } catch {
      case e: ArithmeticException => super./(that)
    }
  }
  override def simplified = this.decVal.toBigIntExact match {
    case Some(int) => Integer(int)
    case None => this
  }

  override def canEqual(that: Any) = that.isInstanceOf[Dec]
  override def equals(other: Any) = other match {
    case that: Dec => this.canEqual(that) && this.decVal == that.decVal
    case _ => false
  }
  
  override def toString = this.decVal.toString
  override def repr: String = "Dec(\"%s\")".format(this.decVal)
}

object Dec {
  def apply(value: BigDecimal) = new Dec(value)
  def apply(str: String) = new Dec(BigDecimal(str))
  
  def numerator(x: BigDecimal): BigInt = {
    if (x.scale >= 0) {
      BigInt(x.bigDecimal.unscaledValue().toString)
    } else {
      BigInt(x.bigDecimal.unscaledValue().multiply(java.math.BigInteger.TEN.pow(-x.scale)).toString)
    }
  }
  
  def denominator(x: BigDecimal): BigInt = {
    if (x.scale >= 0) BigInt(10).pow(x.scale)
    else 1
  }
}