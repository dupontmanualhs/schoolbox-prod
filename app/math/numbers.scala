package math

import java.math.MathContext
import scala.util.matching.Regex
import ch.qos.logback.core.joran.conditional.ElseAction

abstract class Number extends Constant {
  //def toMathMlPresentation(): NodeSeq
  //def toMathMlContent(): NodeSeq
}

object Number {
  def apply(s: String): Option[Number] = {
    RealNumber(s) orElse ComplexNumber(s)
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
    if (i < java.lang.Integer.MIN_VALUE || i > java.lang.Integer.MAX_VALUE) {
      "BigInt(%s)".format(i.toString())
    } else {
      "%d".format(i)
    }
  }
}

abstract class RealNumber extends Number {
  override def getValue: BigDecimal = this.toApproximation.getValue
  def toApproximation: ApproxNumber
  def +(right: RealNumber): RealNumber = {
    (this, right) match {
      case (left: ExactNumber, right: ExactNumber) => left + right
      case _ => ApproxNumber(this.getValue + right.getValue)
    }
  }
  def -(right: RealNumber): RealNumber = {
    (this, right) match {
      case (left: ExactNumber, right: ExactNumber) => left - right
      case _ => ApproxNumber(this.getValue - right.getValue)
    }
  }
  def *(right: RealNumber): RealNumber = {
    (this, right) match {
      case (left: ExactNumber, right: ExactNumber) => left * right
      case _ => ApproxNumber(this.getValue * right.getValue)
    }
  }
  def /(right: RealNumber): RealNumber = {
    (this, right) match {
      case (left: ExactNumber, right: ExactNumber) => left / right
      case _ => ApproxNumber(this.getValue / right.getValue)
    }
  }
}
object RealNumber {
  def apply(s: String): Option[Number] = {
    ExactNumber(s) orElse ApproxNumber(s)
  }
}

abstract class ExactNumber extends RealNumber {
  def +(right: ExactNumber): ExactNumber = {
    (this, right) match {
      case (left: Integer, right: Integer) => Integer(left.getInt + right.getInt)
      case (left: Integer, right: Fraction) => Fraction(left.getInt * right.getDenominator + right.getNumerator, right.getDenominator).simplify
      case (left: Fraction, right: Fraction) => Fraction(left.getNumerator * right.getDenominator + right.getNumerator * left.getDenominator, left.getDenominator * right.getDenominator).simplify
      case (left: Fraction, right: Integer) => right + left
      case (_, _) => Decimal(this.getValue + right.getValue)
    }
  }
  def -(right: ExactNumber): ExactNumber = {
    (this, right) match {
      case (left: Integer, right: Integer) => Integer(left.getInt - right.getInt)
      case (left: Integer, right: Fraction) => Fraction(left.getInt * right.getDenominator - right.getNumerator, right.getDenominator).simplify
      case (left: Fraction, right: Fraction) => Fraction(left.getNumerator * right.getDenominator - right.getNumerator * left.getDenominator, left.getDenominator * right.getNumerator).simplify
      case (left: Fraction, right: Integer) => Fraction(left.getNumerator - right.getInt * left.getDenominator, left.getDenominator).simplify
      case (_, _) => Decimal(this.getValue - right.getValue)
    }
  }
  def *(right: ExactNumber): ExactNumber = {
    (this, right) match {
      case (left: Integer, right: Integer) => Integer(left.getInt * right.getInt)
      case (left: Integer, right: Fraction) => Fraction(left.getInt * right.getNumerator, right.getDenominator).simplify
      case (left: Fraction, right: Fraction) => Fraction(left.getNumerator * right.getNumerator, left.getDenominator * right.getDenominator).simplify
      case (left: Fraction, right: Integer) => right * left
      case (_, _) => Decimal(this.getValue * right.getValue)
    }
  }
  def /(right: ExactNumber): ExactNumber = {
    (this, right) match {
      case (left: Integer, right: Integer) => Fraction(left, right).simplify
      case (_, right: Fraction) => this * right.getReciprocal
      case (left: Fraction, right: Integer) => Fraction(left.getNumerator, left.getDenominator * right.getInt).simplify
      case (_, _) => Decimal(this.getValue / right.getValue)
    }
  }
}

object ExactNumber {
  def apply(s: String): Option[ExactNumber] = {
    Integer(s) orElse Fraction(s) orElse Decimal(s)
  }
}

class Fraction(val numerator: BigInt, val denominator: BigInt) extends ExactNumber {
  def getNumerator = numerator
  def getDenominator = denominator
  
  def negate = Fraction(-1 * numerator, denominator)

  def toOperation: Quotient = Quotient(Integer(numerator), Integer(denominator))

  override def toApproximation: ApproxNumber = ApproxNumber(this.getNumerator.toDouble./(this.getDenominator.toDouble))

  override def simplify: ExactNumber = {
    val gcf: BigInt = this.getGCF(this.getNumerator, this.getDenominator) //gcf: greatest common factor
    Fraction(this.getNumerator / gcf, this.getDenominator / gcf) match {
      case aFrac: Fraction if (aFrac.getValue < 0) => Fraction(-(aFrac.getNumerator.abs), aFrac.getDenominator.abs)
      case aFrac: Fraction if (aFrac.getDenominator.abs == 1) => Integer(aFrac.getNumerator * aFrac.getDenominator) //turns a fraction with a denominator of 1 or -1 into an integer
      case aFrac: Fraction => aFrac
    }
  }

  private def getGCF(numerator: BigInt, denominator: BigInt): BigInt = {
    if (numerator % denominator == 0) {
      denominator
    } else {
      getGCF(denominator, numerator % denominator)
    }
  }

  def getReciprocal = Fraction(getDenominator, getNumerator)

  override def getPrecedence: Int = 3

  private def formatString(s: String): String = {
    s.format(this.getNumerator.toString(), this.getDenominator.toString())
  }

  override def toLaTeX: String = {
    /*if (this.getDenominator == 1) formatString("%s") else */ formatString("\\frac{%s}{%s}")
  }

  override def description: String = {
    "Fraction(%s, %s)".format(Number.intDescription(this.getNumerator), Number.intDescription(this.getDenominator))
  }
}

object Fraction {
  def apply(numerator: BigInt, denominator: BigInt) = new Fraction(numerator, denominator)
  def apply(numerator: Integer, denominator: Integer) = new Fraction(numerator.getInt, denominator.getInt)
  def apply(s: String): Option[Fraction] = Fraction.stringToFraction(s)
  def stringToFraction(s: String): Option[Fraction] = {
    val fractionLaTeXRegex = new Regex("""^([+-]?)\\frac\{([+-]?\d+)\}\{([+-]?\d+)\}$""", "operator", "numerator", "denominator")
    val normalFractionRegex = new Regex("""^([+-])?([+-]?\d+)/([+-]?\d+)$""", "operator", "numerator", "denominator")
    val potentialFraction = fractionLaTeXRegex.findFirstMatchIn(s) orElse normalFractionRegex.findFirstMatchIn(s)
    if (potentialFraction.isDefined) {
      val opMatch = potentialFraction.get.group("operator")
      val op = if (opMatch == null) "" else opMatch
      Some(Fraction(Integer(op + potentialFraction.get.group("numerator")).get, Integer(potentialFraction.get.group("denominator")).get))
    } else {
      None
    }
  }
}

class Integer(anInt: BigInt) extends ExactNumber {
  def getInt = anInt
  override def getValue: BigDecimal = BigDecimal(anInt)
  override def simplify = Integer(this.getInt)
  override def toApproximation: ApproxNumber = ApproxNumber(BigDecimal(anInt))
  override def description: String = "Integer(%s)".format(Number.intDescription(anInt))
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

object Integer {
  def apply(anInt: BigInt): Integer = new Integer(anInt)
  def apply(s: String): Option[Integer] = {
    Number.stringToInt(s) match {
      case Some(bigInt) => Some(Integer(bigInt))
      case _ => None
    }
  }
}

class Decimal(val value: BigDecimal) extends ExactNumber {
  override def getValue: BigDecimal = value

  override def simplify = Decimal(this.getValue)
  def toApproximation: ApproxNumber = ApproxNumber(this.getValue.toDouble)

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
    (Number.stringToInt(s) orElse Number.stringToDecimal(s)).getOrElse("1").toString
  }

  def getPowerFromString(s: String): String = {
    Integer(s) match {
      case Some(aInt) => aInt.toString
      case _ => ""
    }
  }

  def description: String = "Decimal(\"%s\")".format(this.getValue.toString())
}

object Decimal {
  def apply(bigDecimal: BigDecimal) = new Decimal(bigDecimal)

  def apply(s: String): Option[Decimal] = {
    Number.stringToDecimal(s).map(Decimal(_))
  }

  def getPowerFromString(s: String): String = {
    Integer(s) match {
      case Some(aInt) => aInt.toString
      case _ => ""
    }
  }
}

class ApproxNumber(val value: BigDecimal) extends RealNumber {
  override def getValue: BigDecimal = value
  override def simplify = ApproxNumber(this.getValue)
  def toApproximation: ApproxNumber = this

  override def toLaTeX: String = ApproxNumber.prefix + this.getValue.toString()

  override def description: String = "ApproxNumber(%s)".format(this.getValue.toString())
}

object ApproxNumber {
  val prefix: String = "\\approx"
  val symbol: String = "\u2248"
  def apply(d: BigDecimal) = new ApproxNumber(d)
  def apply(s: String): Option[Number] = {
    val approxRegex = new Regex("""^\\approx(.+)$""", "value") //TODO: does not work for unicode (yet)
    val potentialApprox = approxRegex.findFirstMatchIn(s)
    if (potentialApprox.isDefined) {
      Expression(potentialApprox.get.group("value")) match {
        case Some(anInteger: Integer) => Some(ApproxNumber(anInteger.getValue))
        case Some(aBigDecimal: Decimal) => Some(ApproxNumber(aBigDecimal.getValue))
        case Some(aComplexNum: ComplexNumber) => Some(ComplexNumber(ApproxNumber(aComplexNum.getReal.getValue), ApproxNumber(aComplexNum.getImaginary.getValue)))
        case _ => None
      }
    } else {
      None
    }
  }
}

class ComplexNumber(val real: RealNumber, val imag: RealNumber) extends Number {
  def getReal: RealNumber = if (real.isInstanceOf[ApproxNumber]) Decimal(real.getValue) else real
  def getImaginary: RealNumber = if (imag.isInstanceOf[ApproxNumber]) Decimal(imag.getValue) else imag
  override def getValue: BigDecimal = null
  override def getPrecedence: Int = if (getReal.getValue != 0) -1 else super.getPrecedence
  override def simplify = {
    (this.getReal.simplify, this.getImaginary.simplify) match {
      case (real: RealNumber, imag: RealNumber) => ComplexNumber(real, imag)
      case _ => ComplexNumber(this.getReal, this.getImaginary)
    }
  }

  // if either real or imag are approximate, both are coerced to approximate
  def isApproximation: Boolean = real.isInstanceOf[ApproxNumber] || imag.isInstanceOf[ApproxNumber]

  private def imaginaryToLaTeX: String = {
    this.getImaginary.toLaTeX match {
      case "0" => ""
      case "1" => "i"
      case "-1" => "-i"
      case str: String => str + "i"
    }
  }

  private def complexString: String = {
    if (getReal.getValue == 0 && getImaginary.getValue == 0) "0"
    else if (getReal.getValue == 0) imaginaryToLaTeX
    else if (getImaginary.getValue == 0) getReal.toLaTeX
    else getReal.toLaTeX + getOperator + imaginaryToLaTeX
  }

  private def getOperator: String = if (imaginaryToLaTeX.startsWith("-") || imaginaryToLaTeX == "") "" else "+"

  def toLaTeX: String = {
    if (isApproximation) "\\approx(%s)".format(complexString) else complexString
  }

  def description: String = "ComplexNumber(%s, %s)".format(real.description, imag.description)

  override def equals(that: Any): Boolean = {
    that match {
      case that: ComplexNumber => this.getReal == that.getReal && this.getImaginary == that.getImaginary
      case _ => false
    }
  }
}

object ComplexNumber {
  def apply(real: RealNumber, imaginary: RealNumber): ComplexNumber = {
    new ComplexNumber(real, imaginary)
  }

  def apply(s: String): Option[ComplexNumber] = {
    val basicComplexRegex = new Regex("""^()(.*)$""", "real", "imaginary")
    val complexRegex = new Regex("""^(.*)(?<!E)(?=[+-])(.*)$""", "real", "imaginary")
    val potentialComplex = complexRegex.findFirstMatchIn(s) orElse basicComplexRegex.findFirstMatchIn(s)
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
    (RealNumber(potentialRealPart), RealNumber(potentialImaginaryPart)) match {
      case (Some(real: RealNumber), Some(imag: RealNumber)) => Some(ComplexNumber(real, imag))
      case _ => None
    }
  }
}