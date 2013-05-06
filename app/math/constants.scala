package math

import scala.collection.immutable.HashMap

//Constant subclasses: ConstantPi & ConstantE (see below)
//                                      Number (numbers.scala)
abstract class Constant extends Value {
	def getValue: BigDecimal
	override def equals(that: Any): Boolean = {
		that match {
			case that: Constant => (that.getValue == this.getValue)
			case _ => false
		}
	}

    def +(right: Constant): Expression = {
        (this, right) match {
            case (left: RealNumber, right: RealNumber) => left + right
            case (left: RealNumber, right: ComplexNumber) => ComplexNumber(left + right.getReal, right.getImaginary)
            case (left: ComplexNumber, right: RealNumber) => ComplexNumber(left.getReal + right, left.getImaginary)
            case (left: ComplexNumber, right: ComplexNumber) => ComplexNumber(left.getReal + right.getReal, left.getImaginary + right.getImaginary)
            case (left: Constant, right: ComplexNumber) => if(right.isApproximation) ComplexNumber(ApproxNumber(left.getValue + right.getReal.getValue), right.getImaginary) else Sum(left, right)
            case (left: ComplexNumber, right: Constant) => if(left.isApproximation) ComplexNumber(ApproxNumber(left.getReal.getValue + right.getValue), left.getImaginary) else Sum(left, right)
            case (left: ApproxNumber, _) => ApproxNumber(left.getValue + right.getValue)
            case (_, right: ApproxNumber) => right + this
            case _ => if(this == right) this * Integer(2) else Sum(this, right)
        }
    }
    def -(right: Constant): Expression = {
        (this, right) match {
            case (left: RealNumber, right: RealNumber) => left - right
            case (left: RealNumber, right: ComplexNumber) => ComplexNumber(left - right.getReal, right.getImaginary)
            case (left: ComplexNumber, right: RealNumber) => ComplexNumber(left.getReal - right, left.getImaginary)
            case (left: ComplexNumber, right: ComplexNumber) => ComplexNumber(left.getReal - right.getReal, left.getImaginary - right.getImaginary)
            case (left: Constant, right: ComplexNumber) => if(right.isApproximation) ComplexNumber(ApproxNumber(left.getValue - right.getReal.getValue), right.getImaginary) else Difference(left, right)
            case (left: ComplexNumber, right: Constant) => if(left.isApproximation) ComplexNumber(ApproxNumber(left.getReal.getValue - right.getValue), left.getImaginary) else Difference(left, right)
            case (left: ApproxNumber, _) => ApproxNumber(left.getValue - right.getValue)
            case (_, right: ApproxNumber) => ApproxNumber(this.getValue - right.getValue)
            case _ => if(this == right) Integer(0) else Difference(this, right)
        }
    }
    def *(right: Constant): Expression = {
        (this, right) match {
            case (left: RealNumber, right: RealNumber) => left * right
            case (left: RealNumber, right: ComplexNumber) => ComplexNumber(left * right.getReal, left * right.getImaginary)
            case (left: ComplexNumber, right: RealNumber) => ComplexNumber(left.getReal * right, left.getImaginary * right)
            case (left: ComplexNumber, right: ComplexNumber) => ComplexNumber(left.getReal * right.getReal - left.getImaginary * right.getImaginary, left.getReal * right.getImaginary + right.getReal * left.getImaginary)
            case (left: Constant, right: ComplexNumber) => if(right.isApproximation) ComplexNumber(ApproxNumber(left.getValue * right.getReal.getValue), ApproxNumber(left.getValue * right.getImaginary.getValue)) else Product(left, right)
            case (left: ComplexNumber, right: Constant) => if(left.isApproximation) ComplexNumber(ApproxNumber(left.getReal.getValue * right.getValue), ApproxNumber(left.getImaginary.getValue * right.getValue)) else Product(left, right)
            case (left: ApproxNumber, _) => ApproxNumber(left.getValue * right.getValue)
            case (_, right: ApproxNumber) => right * this
            case _ => if(this == right) Exponentiation(this, Integer(2)) else Product(this, right)
        }
    }
    def /(right: Constant): Expression = {
        (this, right) match {
            case (left: RealNumber, right: RealNumber) => left / right
            case (left: RealNumber, right: ComplexNumber) => Quotient(left * ComplexNumber(right.getReal, right.getImaginary * Integer(-1)), right * ComplexNumber(right.getReal, right.getImaginary * Integer(-1))).simplify
            case (left: ComplexNumber, right: RealNumber) => Quotient(left.getReal / right, left.getImaginary / right)
            case (left: ComplexNumber, right: ComplexNumber) => Quotient(left * ComplexNumber(right.getReal, right.getImaginary * Integer(-1)), right * ComplexNumber(right.getReal, right.getImaginary * Integer(-1))).simplify
            case (left: Constant, right: ComplexNumber) => Quotient(left * ComplexNumber(right.getReal, right.getImaginary * Integer(-1)), right * ComplexNumber(right.getReal, right.getImaginary * Integer(-1))).simplify
            case (left: ComplexNumber, right: Constant) => if(left.isApproximation) ComplexNumber(ApproxNumber(left.getReal.getValue / right.getValue), ApproxNumber(left.getImaginary.getValue / right.getValue)) else Quotient(left, right)
            case (left: ApproxNumber, _) => ApproxNumber(left.getValue / right.getValue)
            case (_, right: ApproxNumber) => ApproxNumber(this.getValue / right.getValue)
            case _ => if(this == right) Integer(1) else Quotient(this, right)
        }
    }
    override def evaluate(variables: HashMap[Expression, Value]): Expression = this
}

object Constant {
	def apply(s: String): Option[Constant] = {
		s match {
			case "\\pi" => Some(ConstantPi)
			case "e"  => Some(ConstantE)
			case _    => Number(s)
		}
	}
}

object ConstantE extends RealNumber {
	override def getValue: BigDecimal = scala.math.E
	override def toLaTeX: String = "e"
	override def simplify = ConstantE
	override def description: String = "ConstantE"
	def toApproximation: ApproxNumber = ApproxNumber(scala.math.E)
}

object ConstantPi extends RealNumber {
	override def getValue: BigDecimal = scala.math.Pi
	override def toLaTeX: String = "\\pi"
	override def simplify = ConstantPi
	override def description: String = "ConstantPi"
	def toApproximation: ApproxNumber = ApproxNumber(scala.math.Pi)
}
