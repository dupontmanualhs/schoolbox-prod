package eschool.math

import ch.qos.logback.core.joran.conditional.ElseAction
import reflect.This

//MathConstant subclasses: MathConstantPi & MathConstantE (see below)
//                                      MathNumber (Math numbers.scala)
abstract class MathConstant extends MathValue {
	def getValue: BigDecimal
	override def equals(that: Any): Boolean = {
		that match {
			case that: MathConstant => (that.getValue == this.getValue)
			case _ => false
		}
	}

    def +(right: MathConstant): MathExpression = {
        (this, right) match {
            case (left: MathRealNumber, right: MathRealNumber) => left + right
            case (left: MathRealNumber, right: MathComplexNumber) => MathComplexNumber(left + right.getReal, right.getImaginary)
            case (left: MathComplexNumber, right: MathRealNumber) => MathComplexNumber(left.getReal + right, left.getImaginary)
            case (left: MathComplexNumber, right: MathComplexNumber) => MathComplexNumber(left.getReal + right.getReal, left.getImaginary + right.getImaginary)
            case (left: MathConstant, right: MathComplexNumber) => if(right.isApproximation) MathComplexNumber(MathApproximateNumber(left.getValue + right.getReal.getValue), right.getImaginary) else MathSum(left, right)
            case (left: MathComplexNumber, right: MathConstant) => if(left.isApproximation) MathComplexNumber(MathApproximateNumber(left.getReal.getValue + right.getValue), left.getImaginary) else MathSum(left, right)
            case (left: MathApproximateNumber, _) => MathApproximateNumber(left.getValue + right.getValue)
            case (_, right: MathApproximateNumber) => right + this
            case _ => if(this == right) this * MathInteger(2) else MathSum(this, right)
        }
    }
    def -(right: MathConstant): MathExpression = {
        (this, right) match {
            case (left: MathRealNumber, right: MathRealNumber) => left - right
            case (left: MathRealNumber, right: MathComplexNumber) => MathComplexNumber(left - right.getReal, right.getImaginary)
            case (left: MathComplexNumber, right: MathRealNumber) => MathComplexNumber(left.getReal - right, left.getImaginary)
            case (left: MathComplexNumber, right: MathComplexNumber) => MathComplexNumber(left.getReal - right.getReal, left.getImaginary - right.getImaginary)
            case (left: MathConstant, right: MathComplexNumber) => if(right.isApproximation) MathComplexNumber(MathApproximateNumber(left.getValue - right.getReal.getValue), right.getImaginary) else MathDifference(left, right)
            case (left: MathComplexNumber, right: MathConstant) => if(left.isApproximation) MathComplexNumber(MathApproximateNumber(left.getReal.getValue - right.getValue), left.getImaginary) else MathDifference(left, right)
            case (left: MathApproximateNumber, _) => MathApproximateNumber(left.getValue - right.getValue)
            case (_, right: MathApproximateNumber) => MathApproximateNumber(this.getValue - right.getValue)
            case _ => if(this == right) MathInteger(0) else MathDifference(this, right)
        }
    }
    def *(right: MathConstant): MathExpression = {
        (this, right) match {
            case (left: MathRealNumber, right: MathRealNumber) => left * right
            case (left: MathRealNumber, right: MathComplexNumber) => MathComplexNumber(left * right.getReal, left * right.getImaginary)
            case (left: MathComplexNumber, right: MathRealNumber) => MathComplexNumber(left.getReal * right, left.getImaginary * right)
            case (left: MathComplexNumber, right: MathComplexNumber) => MathComplexNumber(left.getReal * right.getReal - left.getImaginary * right.getImaginary, left.getReal * right.getImaginary + right.getReal * left.getImaginary)
            case (left: MathConstant, right: MathComplexNumber) => if(right.isApproximation) MathComplexNumber(MathApproximateNumber(left.getValue * right.getReal.getValue), MathApproximateNumber(left.getValue * right.getImaginary.getValue)) else MathProduct(left, right)
            case (left: MathComplexNumber, right: MathConstant) => if(left.isApproximation) MathComplexNumber(MathApproximateNumber(left.getReal.getValue * right.getValue), MathApproximateNumber(left.getImaginary.getValue * right.getValue)) else MathProduct(left, right)
            case (left: MathApproximateNumber, _) => MathApproximateNumber(left.getValue * right.getValue)
            case (_, right: MathApproximateNumber) => right * this
            case _ => if(this == right) MathExponentiation(this, MathInteger(2)) else MathProduct(this, right)
        }
    }
    def /(right: MathConstant): MathExpression = {
        (this, right) match {
            case (left: MathRealNumber, right: MathRealNumber) => left / right
            case (left: MathRealNumber, right: MathComplexNumber) => MathQuotient(left * MathComplexNumber(right.getReal, right.getImaginary * MathInteger(-1)), right * MathComplexNumber(right.getReal, right.getImaginary * MathInteger(-1))).simplify
            case (left: MathComplexNumber, right: MathRealNumber) => MathQuotient(left.getReal / right, left.getImaginary / right)
            case (left: MathComplexNumber, right: MathComplexNumber) => MathQuotient(left * MathComplexNumber(right.getReal, right.getImaginary * MathInteger(-1)), right * MathComplexNumber(right.getReal, right.getImaginary * MathInteger(-1))).simplify
            case (left: MathConstant, right: MathComplexNumber) => MathQuotient(left * MathComplexNumber(right.getReal, right.getImaginary * MathInteger(-1)), right * MathComplexNumber(right.getReal, right.getImaginary * MathInteger(-1))).simplify
            case (left: MathComplexNumber, right: MathConstant) => if(left.isApproximation) MathComplexNumber(MathApproximateNumber(left.getReal.getValue / right.getValue), MathApproximateNumber(left.getImaginary.getValue / right.getValue)) else MathQuotient(left, right)
            case (left: MathApproximateNumber, _) => MathApproximateNumber(left.getValue / right.getValue)
            case (_, right: MathApproximateNumber) => MathApproximateNumber(this.getValue / right.getValue)
            case _ => if(this == right) MathInteger(1) else MathQuotient(this, right)
        }
    }
}

object MathConstant {
	def apply(s: String): Option[MathConstant] = {
		s match {
			case "\\pi" => Some(MathConstantPi())
			case "e"  => Some(MathConstantE())
			case _    => MathNumber(s)
		}
	}
}

class MathConstantE extends MathConstant {
	override def getValue: BigDecimal = scala.math.E
	override def toLaTeX: String = "e"
	override def simplify = new MathConstantE
	override def description: String = "MathConstantE"
}

object MathConstantE {
	def apply() = new MathConstantE
}

class MathConstantPi extends MathConstant {
	override def getValue: BigDecimal = scala.math.Pi
	override def toLaTeX: String = MathConstantPi.symbol
	override def simplify = new MathConstantPi
	override def description: String = "MathConstantPi"
}

object MathConstantPi {
	def apply() = new MathConstantPi
	def symbol = "\\pi"
}


