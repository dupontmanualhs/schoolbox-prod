package math

import scala.math.{round, pow}

import scala.util.Random

object MathRandom {
  private var NumberGenerator = new Random()
  private val defaultRange = 1 to 10
  private val defaultDecimals = 1
  private val defaultVariableArray = Array[String]("a","b","c","d","f","g","h","j","k","l","m","n","p","q","r","s","t","u","v","w","x","y","z",
                                         "A","B","C","D","F","G","H","J","K","L","M","N","P","Q","R","S","T","U","V","W","X","Y","Z")

  def resetGenerator(i: Int) {
	  NumberGenerator = new Random(i)
  }
  def getRandomExpression: MathExpression = getRandomExpression(defaultRange)
  def getRandomExpression(range: Range): MathExpression = getRandomExpression(defaultRange, defaultDecimals)
  def getRandomExpression(range: Range, decimals: Double): MathExpression = {
    val methodRandomizer = NumberGenerator.nextInt(20)
    methodRandomizer match{
      case num if (num <= 11 && num >= 0) => getRandomOperation(range, decimals)
      case num if (num > 11) => getRandomValue(range, decimals)
    }
  }

  def getRandomOperation: MathOperation = getRandomOperation(defaultRange)
  def getRandomOperation(range: Range): MathOperation = getRandomOperation(range, defaultDecimals)
  def getRandomOperation(range: Range, decimals: Double): MathOperation = {
    val methodRandomizer = NumberGenerator.nextInt(12)
    methodRandomizer match{
      case 0 => getRandomSum(range, decimals)
      case 1 => getRandomDifference(range, decimals)
      case 2 => getRandomProduct(range, decimals)
      case 3 => getRandomQuotient(range, decimals)
      case 4 => getRandomRoot(range, decimals)
      case 5 => getRandomSquareRoot(range, decimals)
      case 6 => getRandomCubeRoot(range, decimals)
      case 7 => getRandomExponentiation(range, decimals)
      case 8 => getRandomLogarithm(range, decimals)
      case 9 => getRandomBase10Logarithm(range, decimals)
      case 10 => getRandomNaturalLogarithm(range, decimals)
      case 11 => getRandomNegation(range, decimals)
    }
  }

  def getRandomValue: MathValue = getRandomValue(defaultRange)
  def getRandomValue(range: Range): MathValue = getRandomValue(range, defaultDecimals)
  def getRandomValue(range: Range, decimals: Double): MathValue = {
    val methodRandomizer = NumberGenerator.nextInt(8)
    methodRandomizer match{
      case 0 => getRandomVariable
      case num if (num > 0) => getRandomConstant(range, decimals)
    }
  }

  def getRandomConstant: MathConstant = getRandomConstant(defaultRange)
  def getRandomConstant(range: Range): MathConstant = getRandomConstant(range, defaultDecimals)
  def getRandomConstant(range: Range, decimals: Double): MathConstant = {
    val methodRandomizer = NumberGenerator.nextInt(7)
    methodRandomizer match{
      case 0 => getRandomConstantE
      case 1 => getRandomConstantPi
      case num if (num > 1) => getRandomNumber(range, decimals)
    }
  }

  def getRandomNumber: MathNumber = getRandomNumber(defaultRange)
  def getRandomNumber(range: Range): MathNumber = getRandomNumber(range, defaultDecimals)
  def getRandomNumber(range: Range, decimals: Double): MathNumber = {
    val methodRandomizer = NumberGenerator.nextInt(5)
    methodRandomizer match{
      case 0 => getRandomComplexNumber(range, decimals)
      case num if (num > 0) => getRandomRealNumber(range, decimals)
    }
  }

  def getRandomRealNumber: MathRealNumber = getRandomRealNumber(defaultRange)
  def getRandomRealNumber(range: Range): MathRealNumber = getRandomRealNumber(range, defaultDecimals)
  def getRandomRealNumber(range: Range, decimals: Double): MathRealNumber = {
    val methodRandomizer = NumberGenerator.nextInt(4)
    methodRandomizer match{
      case 0 => getRandomApproximateNumber(range, decimals)
      case num if (num > 0) => getRandomExactNumber(range, decimals)
    }
  }

  def getRandomExactNumber: MathExactNumber = getRandomExactNumber(defaultRange)
  def getRandomExactNumber(range: Range): MathExactNumber = getRandomExactNumber(range, defaultDecimals)
  def getRandomExactNumber(range: Range, decimals: Double): MathExactNumber = {
    val methodRandomizer = NumberGenerator.nextInt(3)
    methodRandomizer match{
      case 0 => getRandomInteger(range, decimals)
      case 1 => getRandomFraction(range, decimals)
      case 2 => getRandomDecimal(range, decimals)
    }
  }
  
  def getInt(min: Int, max: Int): MathInteger = {
    MathInteger(NumberGenerator.nextInt(max - min + 1) + min) 
  }

  def getRandomInteger: MathInteger = getRandomInteger(defaultRange, defaultDecimals)
  def getRandomInteger(range: Range): MathInteger = getRandomInteger(range, defaultDecimals)
  def getRandomInteger(range: Range, decimals: Double) = MathInteger(NumberGenerator.nextInt(range.last - range.head + 1) + range.head)

  def getRandomFraction: MathFraction = getRandomFraction(defaultRange)
  def getRandomFraction(range: Range): MathFraction = getRandomFraction(range, range)
  def getRandomFraction(range: Range, range2: Range): MathFraction = getRandomFraction(range, range, defaultDecimals)
  def getRandomFraction(range: Range, decimals: Double): MathFraction = getRandomFraction(range, range, decimals)
  def getRandomFraction(range: Range, range2: Range, decimals: Double) = MathFraction(getRandomInteger(range), getRandomInteger(range2))

  def getRandomDecimal: MathDecimal = getRandomDecimal(defaultRange)
  def getRandomDecimal(range: Range): MathDecimal = getRandomDecimal(range, defaultDecimals)
  def getRandomDecimal(range: Range, decimals: Double): MathDecimal = {
    val decimal = MathDecimal(round((NumberGenerator.nextDouble()*(range.last-range.head) + range.head)*pow(10.0, decimals))/pow(10.0, decimals))
    if(decimal.getValue*pow(10.0, decimals) % 10 == 0) getRandomDecimal(range, decimals) else decimal
  }

  def getRandomApproximateNumber: MathApproximateNumber = getRandomApproximateNumber(defaultRange)
  def getRandomApproximateNumber(range: Range): MathApproximateNumber = getRandomApproximateNumber(range, defaultDecimals)
  def getRandomApproximateNumber(range: Range, decimals: Double) = MathApproximateNumber(NumberGenerator.nextDouble()*(range.last-range.head) + range.head)

  def getRandomComplexNumber: MathComplexNumber = getRandomComplexNumber(defaultRange)
  def getRandomComplexNumber(range: Range): MathComplexNumber = getRandomComplexNumber(range, defaultDecimals)
  def getRandomComplexNumber(range: Range, decimals: Double) = MathComplexNumber(getRandomRealNumber(range), getRandomRealNumber(range))

  def getRandomConstantPi = MathConstantPi()

  def getRandomConstantE = MathConstantE()

  def getRandomVariable: MathVariable = getRandomVariable(defaultVariableArray)
  def getRandomVariable(variableArray: Array[String]) = new MathVariable(variableArray(NumberGenerator.nextInt(variableArray.size)))

  def getRandomSum: MathSum = getRandomSum(defaultRange)
  def getRandomSum(range: Range): MathSum = getRandomSum(range, defaultDecimals)
  def getRandomSum(range: Range, decimals: Double) = MathSum(getRandomExpression(range, decimals), getRandomExpression(range, decimals))

  def getRandomDifference: MathDifference = getRandomDifference(defaultRange)
  def getRandomDifference(range: Range): MathDifference = getRandomDifference(range, defaultDecimals)
  def getRandomDifference(range: Range, decimals: Double) = MathDifference(getRandomExpression(range, decimals), getRandomExpression(range, decimals))

  def getRandomProduct: MathProduct = getRandomProduct(defaultRange)
  def getRandomProduct(range: Range): MathProduct = getRandomProduct(range, defaultDecimals)
  def getRandomProduct(range: Range, decimals: Double) = MathProduct(getRandomExpression(range, decimals), getRandomExpression(range, decimals))

  def getRandomQuotient: MathQuotient = getRandomQuotient(defaultRange)
  def getRandomQuotient(range: Range): MathQuotient = getRandomQuotient(range, defaultDecimals)
  def getRandomQuotient(range: Range, decimals: Double) = MathQuotient(getRandomExpression(range, decimals), getRandomExpression(range, decimals))

  def getRandomExponentiation: MathExponentiation = getRandomExponentiation(defaultRange)
  def getRandomExponentiation(range: Range): MathExponentiation = getRandomExponentiation(range, defaultDecimals)
  def getRandomExponentiation(range: Range, decimals: Double) = MathExponentiation(getRandomExpression(range, decimals), getRandomExpression(range, decimals))

  def getRandomLogarithm: MathLogarithm = getRandomLogarithm(defaultRange)
  def getRandomLogarithm(range: Range): MathLogarithm = getRandomLogarithm(range, defaultDecimals)
  def getRandomLogarithm(range: Range, decimals: Double) = MathLogarithm(getRandomConstant(range, decimals), getRandomExpression(range, decimals))

  def getRandomNaturalLogarithm: MathNaturalLogarithm = getRandomNaturalLogarithm(defaultRange)
  def getRandomNaturalLogarithm(range: Range): MathNaturalLogarithm = getRandomNaturalLogarithm(range, defaultDecimals)
  def getRandomNaturalLogarithm(range: Range, decimals: Double) = MathNaturalLogarithm(getRandomExpression(range, decimals))

  def getRandomBase10Logarithm: MathBase10Logarithm = getRandomBase10Logarithm(defaultRange)
  def getRandomBase10Logarithm(range: Range): MathBase10Logarithm = getRandomBase10Logarithm(range, defaultDecimals)
  def getRandomBase10Logarithm(range: Range, decimals: Double) = MathBase10Logarithm(getRandomExpression(range, decimals))

  def getRandomRoot: MathRoot = getRandomRoot(defaultRange)
  def getRandomRoot(range: Range): MathRoot = getRandomRoot(range, defaultDecimals)
  def getRandomRoot(range: Range, decimals: Double) = MathRoot(getRandomExpression(range, decimals), getRandomExpression(range, decimals))

  def getRandomSquareRoot: MathSquareRoot = getRandomSquareRoot(defaultRange)
  def getRandomSquareRoot(range: Range): MathSquareRoot = getRandomSquareRoot(range, defaultDecimals)
  def getRandomSquareRoot(range: Range, decimals: Double) = MathSquareRoot(getRandomExpression(range, decimals))

  def getRandomCubeRoot: MathCubeRoot = getRandomCubeRoot(defaultRange)
  def getRandomCubeRoot(range: Range): MathCubeRoot = getRandomCubeRoot(range, defaultDecimals)
  def getRandomCubeRoot(range: Range, decimals: Double) = MathCubeRoot(getRandomExpression(range, decimals))

  def getRandomNegation: MathNegation = getRandomNegation(defaultRange)
  def getRandomNegation(range: Range): MathNegation = getRandomNegation(range, defaultDecimals)
  def getRandomNegation(range: Range, decimals: Double) = MathNegation(getRandomExpression(range, decimals))
}