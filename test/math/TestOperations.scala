package math

import org.scalatest.FunSuite
import scala.collection.immutable.TreeMap

class TestOperations extends FunSuite {
  test("operation strings") {
	assert(MathOperation("6+7").get === MathSum(MathInteger(6), MathInteger(7)))
	assert(MathOperation("(7+8)*3").get === MathProduct(MathSum(MathInteger(7), MathInteger(8)), MathInteger(3)))
	assert(MathOperation("7+8*3").get === MathSum(MathInteger(7), MathProduct(MathInteger(8), MathInteger(3))))
	//assert(MathOperation("4(3)").get === MathProduct(MathInteger(4), MathInteger(3)))
	//assert(MathOperation("(4)3").get === MathProduct(MathInteger(4), MathInteger(3)))
	assert(MathOperation("5-6").get === MathDifference(MathInteger(5), MathInteger(6)))
	//assert(MathOperation("7/8*(6-4)").get === MathProduct(MathFraction(MathInteger(7), MathInteger(8)), MathDifference(MathInteger(6), MathInteger(4))))
	//assert(MathOperation("7/8*6-4").get === MathDifference(MathProduct(MathFraction(MathInteger(7), MathInteger(8)), MathInteger(6)), MathInteger(4)))
	assert(MathOperation("7^{6}").get === MathExponentiation(MathExpression("7").get, MathExpression("6").get))
	assert(MathOperation("(45+3x)^{3}").get === MathExponentiation(MathExpression("45+3x").get, MathExpression("3").get))
	assert(MathOperation("\\log_[3]{4}").get === MathLogarithm(MathExpression("3").get, MathExpression("4").get))
  }
	
  test("products" ){
	val sixTimesSix = MathInteger(6) * MathInteger(6)
	assert(sixTimesSix.description === "MathProduct(MathInteger(6), MathInteger(6))")
	assert(sixTimesSix.toLaTeX === "6\\cdot6")
	//assert(sixTimesSix.simplify === MathInteger(36))

	val sixTimesSixTimesTwo = MathInteger(6) * MathInteger(6) * MathInteger(2)
	assert(sixTimesSixTimesTwo.description === "MathProduct(MathProduct(MathInteger(6), MathInteger(6)), MathInteger(2))")
	assert(sixTimesSixTimesTwo.toLaTeX === "6\\cdot6\\cdot2")
	//assert(sixTimesSixTimesTwo.simplify === MathInteger(72))
  }
	
  test("quotients") {
	val nineOverThree = MathQuotient(MathInteger(9), MathInteger(3))
	assert(nineOverThree.description === "MathQuotient(MathInteger(9), MathInteger(3))")
	assert(nineOverThree.toLaTeX === "9\\div3")
	//assert(nineOverFive.simplify === MathInteger(3)
  }
	
  test("differences") {
	val negEightMinusNegTwelve = MathDifference(MathInteger(-8), MathInteger(-12))
	assert(negEightMinusNegTwelve.description === "MathDifference(MathInteger(-8), MathInteger(-12))")
	assert(negEightMinusNegTwelve.toLaTeX === "-8-(-12)")
	//assert(negEightMinusNegTwelve.simplify === MathInteger(4))
  }
  
  test("sums") {
	val fourTimesSixPlusSevenTimesTwo = MathSum(MathProduct(MathInteger(4), MathInteger(6)), MathProduct(MathInteger(7), MathInteger(2)))
	assert(fourTimesSixPlusSevenTimesTwo.description === "MathSum(MathProduct(MathInteger(4), MathInteger(6)), MathProduct(MathInteger(7), MathInteger(2)))")
	assert(fourTimesSixPlusSevenTimesTwo.toLaTeX === "4\\cdot6+7\\cdot2")
	//assert(fourTimesSixPlusSevenTimesTwo.simplify.description === MathInteger(38))
  }

  test("roots") {
	val sqrtTwoTimesSqrtSeven = MathProduct(MathSquareRoot(MathInteger(3)), MathSquareRoot(MathInteger(7)))
	assert(sqrtTwoTimesSqrtSeven.toLaTeX === "\\sqrt{3}\\cdot\\sqrt{7}")
	assert(sqrtTwoTimesSqrtSeven.description === "MathProduct(MathSquareRoot(MathInteger(3)), MathSquareRoot(MathInteger(7)))")
	//assert(sqrtTwoTimesSqrtSeven.simplify === MathExponentiation(MathInteger(14), MathFraction(MathInteger(1), MathInteger(2))))
	val sqrtNegFiveTimesCubeRootFour = MathProduct(MathSquareRoot(MathInteger(-5)), MathCubeRoot(MathInteger(4)))
	assert(sqrtNegFiveTimesCubeRootFour.toLaTeX === "\\sqrt{-5}\\cdot\\sqrt[3]{4}")
    assert(sqrtNegFiveTimesCubeRootFour.description === "MathProduct(MathSquareRoot(MathInteger(-5)), MathCubeRoot(MathInteger(4)))")

	val sqrtThreeTimesSqrtFour = MathProduct(MathSquareRoot(MathInteger(3)), MathSquareRoot(MathInteger(4)))
	assert(sqrtThreeTimesSqrtFour.toLaTeX === "\\sqrt{3}\\cdot\\sqrt{4}")
	assert(sqrtThreeTimesSqrtFour.description === "MathProduct(MathSquareRoot(MathInteger(3)), MathSquareRoot(MathInteger(4)))")
	//assert(sqrtThreeTimesSqrtFour.simplify === MathProduct(MathInteger(2), MathExponentiation(MathInteger(3), MathFraction(MathInteger(1), MathInteger(2)))))
	val fourthRoot16 = MathRoot(MathInteger(4), MathInteger(16))
	assert(fourthRoot16.description === "MathRoot(Index: MathInteger(4), Radicand: MathInteger(16))")
	assert(fourthRoot16.toLaTeX === "\\sqrt[4]{16}")
	//assert(fourthRoot16.simplify === MathInteger(2))
	val xthRootx2 = MathRoot(MathVariable("x").get, MathExponentiation(MathVariable("x").get, MathInteger(2)))
	assert(xthRootx2.description === "MathRoot(Index: MathVariable(x), Radicand: MathExponentiation(MathVariable(x), MathInteger(2)))")
	assert(xthRootx2.toLaTeX === "\\sqrt[x]{x^{2}}")
	//assert(xthRootx2.simplify === MathInteger(2))
  }

  test("negations") {
	val negFive = MathNegation(MathInteger(5))
	assert(negFive.description === "MathNegation(MathInteger(5))")
	assert(negFive.toLaTeX === "-5")
	//assert(negFive.simplify === MathNegation(MathInteger(5))
	val negOfNegFive = MathNegation(MathInteger(-5))
	assert(negOfNegFive.description === "MathNegation(MathInteger(-5))")
	assert(negOfNegFive.toLaTeX === "-(-5)")
	//assert(negOfNegFive.simplify === MathInteger(5))
	val negTerm = MathNegation(MathTerm(MathInteger(-6), TreeMap[String, MathInteger]("x" -> MathInteger(2), "y" -> MathInteger(5))))
	assert(negTerm.description === "MathNegation(MathTerm(MathInteger(-6), \"x\" -> MathInteger(2), \"y\" -> MathInteger(5)))")
	assert(negTerm.toLaTeX === "-(-6x^{2}y^{5})")
	//assert(negTerm.simplify === MathTerm(MathInteger(-6), TreeMap[String, MathInteger]("x" -> MathInteger(2), "y" -> MathInteger(5))))
  }
}