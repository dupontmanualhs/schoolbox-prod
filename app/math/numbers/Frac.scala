package math.numbers

class Frac(val num: BigInt, val denom: BigInt) extends Real {
  def +(that: Frac) = new Frac(this.num * that.denom + this.denom * that.num, this.denom * that.denom).simplified
  def -(that: Frac) = new Frac(this.num * that.denom - this.denom * that.num, this.denom * that.denom).simplified
  def *(that: Frac) = new Frac(this.num * that.num, this.denom * that.denom).simplified
  def /(that: Frac) = new Frac(this.num * that.denom, this.denom * that.num).simplified
  
  def simplified: Frac = {
    val signum = num.signum * denom.signum
    val g = num.abs.gcd(denom.abs)
    val newNum = signum * (num.abs / g)
    val newDenom = denom.abs / g
    if (newNum == 0) Integer(0)
    else if (newDenom == 1) Integer(newNum)
    else Frac(newNum, newDenom)
  }
  
  // equals is defined to indicate exact equality, i.e., same num and denom
  // use equiv if you're interested in seeing if two objects represent the
  // same number
  def canEqual(that: Any) = that.isInstanceOf[Frac]
  override def equals(other: Any) = other match {
    case that: Frac => this.canEqual(that) && this.num == that.num && this.denom == that.denom
    case _ => false
  }
  
  override def toString = "%s/%s".format(this.num, this.denom)
  def repr: String = "Frac(\"%s\", \"%s\")".format(this.num, this.denom)
}

object Frac {
  def apply(num: Int, denom: Int) = new Frac(num, denom)
  def apply(num: BigInt, denom: BigInt) = new Frac(num, denom)
  def apply(num: String, denom: String) = new Frac(BigInt(num), BigInt(denom))
}