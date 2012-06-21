package math

import java.math.MathContext
import scala.util.matching.Regex
import ch.qos.logback.core.joran.conditional.ElseAction

abstract class MathNumber extends MathConstant {
	//def toMathMlPresentation(): NodeSeq
	//def toMathMlContent(): NodeSeq
}

object MathNumber {
	def apply(s: String): Option[MathNumber] = {
		MathRealNumber(s) orElse MathComplexNumber(s)
	}

	def stringToDecimal(s: String): Option[BigDecimal] = {
		if (numBeginsWithPlusSign(s)) {
			stringToDecimal(s.substring(1))
		}
		try {
			Some(BigDecimal(s, MathContext.UNLIMITED))
		} catch {
			case e: NumberFormatException => None
		}
	}

	def stringToDouble(s: String): Option[Double] = {
		try {
			Some(s.toDouble)
		} catch {
			case e: NumberFormatException => None
		}
	}

	def stringToInt(num: String): Option[BigInt] = {
		val str = if (numBeginsWithPlusSign(num)) num.substring(1) else num
		try {
			Some(BigInt(str))
		} catch {
			case e: NumberFormatException => None
		}
	}

	def numBeginsWithPlusSign(num: String): Boolean = (num.length != 0 && num != null && num.charAt(0) == '+')


	def intDescription(i: BigInt): String = {
		if (i < Integer.MIN_VALUE || i > Integer.MAX_VALUE) {
			"BigInt(%s)".format(i.toString())
		} else {
    		"%d".format(i)
		}
	}
}

abstract class MathRealNumber extends MathNumber {
	override def getValue: BigDecimal = this.toApproximation.getValue
	def toApproximation: MathApproximateNumber
    def +(right: MathRealNumber): MathRealNumber = {
        (this, right) match {
            case (left: MathExactNumber, right: MathExactNumber) => left + right
            case _ => MathApproximateNumber(this.getValue + right.getValue)
        }
    }
    def -(right: MathRealNumber): MathRealNumber = {
        (this, right) match {
            case (left: MathExactNumber, right: MathExactNumber) => left - right
            case _ => MathApproximateNumber(this.getValue - right.getValue)
        }
    }
    def *(right: MathRealNumber): MathRealNumber = {
        (this, right) match {
            case (left: MathExactNumber, right: MathExactNumber) => left * right
            case _ => MathApproximateNumber(this.getValue * right.getValue)
        }
    }
    def /(right: MathRealNumber): MathRealNumber = {
        (this, right) match {
            case (left: MathExactNumber, right: MathExactNumber) => left / right
            case _ => MathApproximateNumber(this.getValue / right.getValue)
        }
    }
}
object MathRealNumber {
	def apply(s: String): Option[MathNumber] = {
		 MathExactNumber(s) orElse MathApproximateNumber(s)
	}
}

abstract class MathExactNumber extends MathRealNumber{
    def +(right: MathExactNumber): MathExactNumber = {
        (this, right) match {
            case (left: MathInteger, right: MathInteger) => MathInteger(left.getInt + right.getInt)
            case (left: MathInteger, right: MathFraction) => MathFraction(left.getInt * right.getDenominator + right.getNumerator, right.getDenominator).simplify
            case (left: MathFraction, right: MathFraction) => MathFraction(left.getNumerator * right.getDenominator + right.getNumerator * left.getDenominator, left.getDenominator * right.getDenominator).simplify
            case (left: MathFraction, right: MathInteger) => right + left
            case (_, _) => MathDecimal(this.getValue + right.getValue)
        }
    }
    def -(right: MathExactNumber): MathExactNumber = {
        (this, right) match {
            case (left: MathInteger, right: MathInteger) => MathInteger(left.getInt - right.getInt)
            case (left: MathInteger, right: MathFraction) => MathFraction(left.getInt * right.getDenominator - right.getNumerator, right.getDenominator).simplify
            case (left: MathFraction, right: MathFraction) => MathFraction(left.getNumerator * right.getDenominator - right.getNumerator * left.getDenominator, left.getDenominator * right.getNumerator).simplify
            case (left: MathFraction, right: MathInteger) => MathFraction(left.getNumerator - right.getInt * left.getDenominator, left.getDenominator).simplify
            case (_, _) => MathDecimal(this.getValue - right.getValue)
        }
    }
    def *(right: MathExactNumber): MathExactNumber = {
        (this, right) match {
            case (left: MathInteger, right: MathInteger) => MathInteger(left.getInt * right.getInt)
            case (left: MathInteger, right: MathFraction) => MathFraction(left.getInt * right.getNumerator, right.getDenominator).simplify
            case (left: MathFraction, right: MathFraction) => MathFraction(left.getNumerator * right.getNumerator, left.getDenominator * right.getDenominator).simplify
            case (left: MathFraction, right: MathInteger) => right * left
            case (_, _) => MathDecimal(this.getValue * right.getValue)
        }
    }
    def /(right: MathExactNumber): MathExactNumber = {
        (this, right) match {
            case (left: MathInteger, right: MathInteger) => MathFraction(left, right).simplify
            case (_, right: MathFraction) => this * right.getReciprocal
            case (left: MathFraction, right: MathInteger) => MathFraction(left.getNumerator, left.getDenominator * right.getInt).simplify
            case (_, _) => MathDecimal(this.getValue / right.getValue)
        }
    }
}

object MathExactNumber {
  def apply(s: String): Option[MathExactNumber] = {
    MathInteger(s) orElse MathFraction(s) orElse MathDecimal(s)
  }
}

class MathFraction(val numerator: BigInt, val denominator: BigInt) extends MathExactNumber {
	def getNumerator = numerator
	def getDenominator = denominator

	def toMathOperation: MathQuotient = MathQuotient(MathInteger(numerator), MathInteger(denominator))

	override def toApproximation: MathApproximateNumber = MathApproximateNumber(this.getNumerator.toDouble./(this.getDenominator.toDouble))

	override def simplify: MathExactNumber = {
		val gcf: BigInt = this.getGCF(this.getNumerator, this.getDenominator) //gcf: greatest common factor
		MathFraction(this.getNumerator / gcf, this.getDenominator / gcf) match {
			case aFrac: MathFraction if (aFrac.getValue < 0) => MathFraction(-(aFrac.getNumerator.abs), aFrac.getDenominator.abs)
			case aFrac: MathFraction if(aFrac.getDenominator.abs == 1) => MathInteger(aFrac.getNumerator * aFrac.getDenominator)  //turns a fraction with a denominator of 1 or -1 into an integer
            case aFrac: MathFraction => aFrac
		}
	}

	private def getGCF(numerator: BigInt, denominator: BigInt): BigInt = {
		if (numerator % denominator == 0) {
			denominator
		} else {
			getGCF(denominator, numerator % denominator)
		}
	}

    def getReciprocal = MathFraction(getDenominator, getNumerator)

	override def getPrecedence: Int = 3

	private def formatString(s: String): String = {
		s.format(this.getNumerator.toString(), this.getDenominator.toString())
	}

	override def toLaTeX: String = {
		/*if (this.getDenominator == 1) formatString("%s") else */formatString("\\frac{%s}{%s}")
	}

	override def description: String = {
		"MathFraction(%s, %s)".format(MathNumber.intDescription(this.getNumerator), MathNumber.intDescription(this.getDenominator))
	}
}

object MathFraction {
	def apply(numerator: BigInt, denominator: BigInt) = new MathFraction(numerator, denominator)
	def apply(numerator: MathInteger, denominator: MathInteger) = new MathFraction(numerator.getInt, denominator.getInt)
	def apply(s: String): Option[MathFraction] = MathFraction.stringToFraction(s)
	def stringToFraction(s: String): Option[MathFraction] = {
		val fractionLaTeXRegex = new Regex("""^([+-]?)\\frac\{([+-]?\d+)\}\{([+-]?\d+)\}$""", "operator", "numerator", "denominator")
		val normalFractionRegex = new Regex("""^([+-])?([+-]?\d+)/([+-]?\d+)$""", "operator", "numerator", "denominator")
		val potentialFraction = fractionLaTeXRegex.findFirstMatchIn(s) orElse normalFractionRegex.findFirstMatchIn(s)
		if (potentialFraction.isDefined) {
			Some(MathFraction(MathInteger(potentialFraction.get.group("operator") + potentialFraction.get.group("numerator")).get, MathInteger(potentialFraction.get.group("denominator")).get))
		} else {
			None
		}
	}
}

class MathInteger(anInt: BigInt) extends MathExactNumber {
  def getInt = anInt
  override def getValue: BigDecimal = BigDecimal(anInt)
  override def simplify = MathInteger(this.getInt)
  override def toApproximation: MathApproximateNumber = MathApproximateNumber(BigDecimal(anInt))
  override def description: String = "MathInteger(%s)".format(MathNumber.intDescription(anInt))
  override def toLaTeX: String = "" + this.getInt
  
  /**
   * returns false for any number <= 1. For all other numbers,
   * checks to see if the number is divisible by 2, 3, and 6k+/-1 for
   * all k up to the square root of this number. This is very slow for
   * large numbers.
   */
  def isPrime: Boolean = {
    if (anInt < 2) false
    else if (anInt == 2 || anInt == 3) true
    else if (anInt % 2 == 0) false
    else if (anInt % 3 == 0) false
    else {
      def check6kpm1(i: BigInt): Boolean = {
        val possFact = 6 * i - 1
        if (possFact * possFact > anInt) true
        else (anInt % possFact != 0 && anInt % (possFact + 2) != 0 && check6kpm1(i + 1))
      }
      check6kpm1(1)
    }
  }
}

object MathInteger {
  def apply(anInt: BigInt): MathInteger = new MathInteger(anInt)
  def apply(s: String): Option[MathInteger] = {
	MathNumber.stringToInt(s) match {
	  case Some(bigInt) => Some(MathInteger(bigInt))
	  case _ => None
	}
  }
}


class MathDecimal(val value: BigDecimal) extends MathExactNumber {
	override def getValue: BigDecimal = value
	
	override def simplify = MathDecimal(this.getValue)
	def toApproximation: MathApproximateNumber = MathApproximateNumber(this.getValue.toDouble)

	def toLaTeX: String = {
		if (this.getValue.toString().contains("E")) {
			this.scientificNumberLaTeX
		} else {
			this.getValue.toString()
		}
	}

	private def scientificNumberLaTeX: String = {
		val regex = new Regex("""^([+-]?[.\d]*)?E([+-]\d+)$""", "coefficient", "power")
		val scientificNum = regex.findFirstMatchIn(this.getValue.toString()).get
		val potentialCoef: String = scientificNum.group("coefficient")
		val potentialPower: String = scientificNum.group("power")
		val coefficient: String = getCoefficientFromString(potentialCoef)
		val power = getPowerFromString(potentialPower)
		"%s*10^{%s}".format(coefficient, power)
	}

	def getCoefficientFromString(s: String): String = {
		(MathNumber.stringToInt(s) orElse MathNumber.stringToDecimal(s)).getOrElse("1").toString
	}

	def getPowerFromString(s: String): String = {
		MathInteger(s) match {
			case Some(aMathInt) => aMathInt.toString
			case _ => ""
		}
	}

	def description: String = "MathDecimal(\"%s\")".format(this.getValue.toString())
}

object MathDecimal {
	def apply(bigDecimal: BigDecimal) = new MathDecimal(bigDecimal)

	def apply(s: String): Option[MathDecimal] = {
		MathNumber.stringToDecimal(s).map(MathDecimal(_))
	}

	def getPowerFromString(s: String): String = {
		MathInteger(s) match {
			case Some(aMathInt) => aMathInt.toString
			case _ => ""
		}
	}
}


class MathApproximateNumber(val value: BigDecimal) extends MathRealNumber {
	override def getValue: BigDecimal = value
	override def simplify = MathApproximateNumber(this.getValue)
	def toApproximation: MathApproximateNumber = this

	override def toLaTeX: String = MathApproximateNumber.prefix + this.getValue.toString()

	override def description: String = "MathApproximateNumber(%s)".format(this.getValue.toString())
}

object MathApproximateNumber {
	val prefix: String = "\\approx"
	val symbol: String = "\u2248"
	def apply(d: BigDecimal) = new MathApproximateNumber(d)
	def apply(s: String): Option[MathNumber] = {
		val approxRegex = new Regex("""^\\approx(.+)$""", "value")       //TODO: does not work for unicode (yet)
		val potentialApprox = approxRegex.findFirstMatchIn(s)
		if (potentialApprox.isDefined) {
			MathExpression(potentialApprox.get.group("value")) match {
				case Some(anInteger: MathInteger) => Some(MathApproximateNumber(anInteger.getValue))
				case Some(aBigDecimal: MathDecimal) => Some(MathApproximateNumber(aBigDecimal.getValue))
				case Some(aComplexNum: MathComplexNumber) => Some(MathComplexNumber(MathApproximateNumber(aComplexNum.getReal.getValue), MathApproximateNumber(aComplexNum.getImaginary.getValue)))
				case _ => None
			}
		} else {
			None
		}
	}
}

class MathComplexNumber(val real: MathRealNumber, val imag: MathRealNumber) extends MathNumber {
	def getReal: MathRealNumber = if(real.isInstanceOf[MathApproximateNumber]) MathDecimal(real.getValue) else real
	def getImaginary: MathRealNumber = if(imag.isInstanceOf[MathApproximateNumber]) MathDecimal(imag.getValue) else imag
	override def getValue: BigDecimal = null
	override def getPrecedence: Int = if(getReal.getValue != 0) -1 else super.getPrecedence
	override def simplify = {
		(this.getReal.simplify, this.getImaginary.simplify) match {
			case (real: MathRealNumber, imag: MathRealNumber) => MathComplexNumber(real, imag)
			case _ => MathComplexNumber(this.getReal, this.getImaginary)
		}
	}

	// if either real or imag are approximate, both are coerced to approximate
	def isApproximation: Boolean = real.isInstanceOf[MathApproximateNumber] || imag.isInstanceOf[MathApproximateNumber]

    private def imaginaryToLaTeX: String = {
        this.getImaginary.toLaTeX match {
          case "0" => ""
        	case "1" => "i"
            case "-1" => "-i"
            case str: String => str + "i"
        }
    }

	private def complexString: String = {
	  if(getReal.getValue == 0 && getImaginary.getValue == 0) "0"
	  else if(getReal.getValue == 0) imaginaryToLaTeX
	  else if(getImaginary.getValue == 0) getReal.toLaTeX
	  else getReal.toLaTeX + getOperator + imaginaryToLaTeX
	  }

	private def getOperator: String = if (imaginaryToLaTeX.startsWith("-") || imaginaryToLaTeX == "") "" else "+"

	def toLaTeX: String = {
		if (isApproximation) "\\approx(%s)".format(complexString) else complexString
	}

	def description: String = "MathComplexNumber(%s, %s)".format(real.description, imag.description)

	override def equals(that: Any): Boolean = {
		that match {
			case that: MathComplexNumber => this.getReal == that.getReal && this.getImaginary == that.getImaginary
			case _ => false
		}
	}
}

object MathComplexNumber {
	def apply(real: MathRealNumber, imaginary: MathRealNumber): MathComplexNumber = {
		new MathComplexNumber(real, imaginary)
	}

	def apply(s: String): Option[MathComplexNumber] = {
		val basicComplexRegex = new Regex("""^()(.*)$""", "real", "imaginary")
		val complexRegex = new Regex("""^(.*)(?<!E)(?=[+-])(.*)$""", "real", "imaginary")
		val potentialComplex = complexRegex.findFirstMatchIn(s) orElse  basicComplexRegex.findFirstMatchIn(s)
		if (!potentialComplex.isDefined) return None
		
		if (potentialComplex.get.group("real").endsWith("i") && 
		    potentialComplex.get.group("imaginary").endsWith("i")) {
		  return None
		} else if (!(potentialComplex.get.group("real").endsWith("i") || 
		    potentialComplex.get.group("imaginary").endsWith("i"))) {
		  return None
		}
		val actualRealGroup: String = {
		  if (potentialComplex.get.group("real").endsWith("i")) potentialComplex.get.group("imaginary") else 
		    potentialComplex.get.group("real")
		}
		val actualImaginaryGroup: String = {
		  if (potentialComplex.get.group("real").endsWith("i")) potentialComplex.get.group("real").dropRight(1) else 
		    potentialComplex.get.group("imaginary").dropRight(1)
		}
		val potentialRealPart = {
			actualRealGroup match {
				case "" => "0"
				case str: String => str
			}
		}
		val potentialImaginaryPart = {
			actualImaginaryGroup match {
				case "" => "1"
				case "+" => "1"
				case "-" => "-1"
				case str: String => str
			}
		}
		(MathRealNumber(potentialRealPart), MathRealNumber(potentialImaginaryPart)) match {
			case (Some(real: MathRealNumber), Some(imag: MathRealNumber)) => Some(MathComplexNumber(real, imag))
			case _ => None
		}
	}
}