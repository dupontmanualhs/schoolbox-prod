package math.numbers

class Complex(val re: Real, val im: Real) extends Number {
  def +(that: Complex): Number = Complex(this.re + that.re, this.im + that.im).simplified
  def -(that: Complex): Number = Complex(this.re - that.re, this.im - that.im).simplified
  def *(that: Complex): Number = {
    val newRe: Real = (this.re * that.re) - (this.im * that.im)
    val newIm: Real = (this.im * that.re) + (this.re * that.im)
    Complex(newRe, newIm).simplified
  }
  def /(that: Complex): Number = {
    val denom: Real = (that.re * that.re) + (that.im * that.im)
    val newRe: Real = ((this.re * that.re) + (this.im * that.im)) / denom
    val newIm: Real = ((this.im * that.re) - (this.re * that.im)) / denom
    Complex(newRe, newIm).simplified
  }
  
  def simplified: Number = {
    if (this.im == Integer(0)) this.re
    else this
  }
  
  override def toString = {
    val rePart = this.re.toString
    val imPart = this.im.toString
    if (imPart.contains('/')) {
      "%s+(%s)i".format(rePart, imPart)
    } else if (imPart.startsWith("-")) {
      "%s%si".format(rePart, imPart)
    } else {
      "%s+%si".format(rePart, imPart)
    }
  }
  def repr: String = "Complex(%s, %s)".format(this.re.repr, this.im.repr)
  
  def canEqual(that: Any) = that.isInstanceOf[Complex]
  override def equals(other: Any) = other match {
    case that: Complex => this.canEqual(that) && this.re == that.re && this.im == that.im
    case _ => false
  }
}

object Complex {
  def apply(re: Real, im: Real) = new Complex(re, im)
  def apply(re: BigInt, im: BigInt) = new Complex(Integer(re), Integer(im))
}