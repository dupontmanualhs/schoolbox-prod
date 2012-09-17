package math

import scala.math.{round, pow}
import scala.util.Random

object RandomGen {
  private var NumberGenerator = new Random()
  private val defaultRange = (1, 10)
  private val defaultDecimals = 1
  private val defaultVariableArray = Array[String]("a","b","c","d","f","g","h","j","k","l","m","n","p","q","r","s","t","u","v","w","x","y","z",
                                         "A","B","C","D","F","G","H","J","K","L","M","N","P","Q","R","S","T","U","V","W","X","Y","Z")

  def resetGenerator(i: Int) {
	  NumberGenerator = new Random(i)
  }

  def getExpression(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): Expression = {
    val methodRandomizer = NumberGenerator.nextInt(20)
    methodRandomizer match{
      case num if (num <= 11 && num >= 0) => getOperation(range, decimals)
      case num if (num > 11) => getValue(range, decimals)
    }
  }

  def getOperation(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): Operation = {
    val methodRandomizer = NumberGenerator.nextInt(12)
    methodRandomizer match{
      case 0 => getSum(range, decimals)
      case 1 => getDifference(range, decimals)
      case 2 => getProduct(range, decimals)
      case 3 => getQuotient(range, decimals)
      case 4 => getRoot(range, decimals)
      case 5 => getSquareRoot(range, decimals)
      case 6 => getCubeRoot(range, decimals)
      case 7 => getExponentiation(range, decimals)
      case 8 => getLogarithm(range, decimals)
      case 9 => getBase10Logarithm(range, decimals)
      case 10 => getNaturalLogarithm(range, decimals)
      case 11 => getNegation(range, decimals)
    }
  }

  def getValue(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): Value = {
    val methodRandomizer = NumberGenerator.nextInt(8)
    methodRandomizer match{
      case 0 => getVariable()
      case num if (num > 0) => getConstant(range, decimals)
    }
  }

  def getConstant(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): Constant = {
    val methodRandomizer = NumberGenerator.nextInt(7)
    methodRandomizer match{
      case 0 => ConstantPi()
      case 1 => ConstantE()
      case num if (num > 1) => getNumber(range, decimals)
    }
  }

  def getNumber(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): Number = {
    val methodRandomizer = NumberGenerator.nextInt(5)
    methodRandomizer match{
      case 0 => getComplexNumber(range, decimals)
      case num if (num > 0) => getRealNumber(range, decimals)
    }
  }

  def getRealNumber(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): RealNumber = {
    val methodRandomizer = NumberGenerator.nextInt(4)
    methodRandomizer match{
      case 0 => getApproximateNumber(range, decimals)
      case num if (num > 0) => getExactNumber(range, decimals)
    }
  }

  def getExactNumber(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): ExactNumber = {
    val methodRandomizer = NumberGenerator.nextInt(3)
    methodRandomizer match{
      case 0 => getInteger(range)
      case 1 => getFraction(range)
      case 2 => getDecimal(range, decimals)
    }
  }

  def getInteger(range: (Int, Int) = defaultRange) = Integer(NumberGenerator.nextInt(range._2 - range._1 + 1) + range._1)

  def getFraction(range: (Int, Int) = defaultRange, range2: (Int, Int) = defaultRange) = Fraction(getInteger(range), getInteger(range2))

  def getDecimal(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals): Decimal = {
    val decimal = Decimal(round((NumberGenerator.nextDouble()*(range._2 - range._1) + range._1)*pow(10.0, decimals))/pow(10.0, decimals))
    if(decimal.getValue*pow(10.0, decimals) % 10 == 0) getDecimal(range, decimals) else decimal
  }

  def getApproximateNumber(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = ApproxNumber(NumberGenerator.nextDouble()*(range._2 - range._1) + range._1)

  def getComplexNumber(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = ComplexNumber(getRealNumber(range), getRealNumber(range))

  def getVariable(variableArray: Array[String] = defaultVariableArray) = new Var(variableArray(NumberGenerator.nextInt(variableArray.size)))

  def getSum(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Sum(getExpression(range, decimals), getExpression(range, decimals))

  def getDifference(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Difference(getExpression(range, decimals), getExpression(range, decimals))

  def getProduct(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Product(getExpression(range, decimals), getExpression(range, decimals))

  def getQuotient(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Quotient(getExpression(range, decimals), getExpression(range, decimals))

  def getExponentiation(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Exponentiation(getExpression(range, decimals), getExpression(range, decimals))

  def getLogarithm(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Logarithm(getConstant(range, decimals), getExpression(range, decimals))

  def getNaturalLogarithm(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = NaturalLogarithm(getExpression(range, decimals))

  def getBase10Logarithm(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Base10Logarithm(getExpression(range, decimals))

  def getRoot(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Root(getExpression(range, decimals), getExpression(range, decimals))

  def getSquareRoot(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = SquareRoot(getExpression(range, decimals))

  def getCubeRoot(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = CubeRoot(getExpression(range, decimals))

  def getNegation(range: (Int, Int) = defaultRange, decimals: Double = defaultDecimals) = Negation(getExpression(range, decimals))
}