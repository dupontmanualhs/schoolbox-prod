package math

import org.scalatest.FunSuite
import scala.collection.immutable.TreeMap

class TestOperations extends FunSuite {
  test("operation strings") {
	assert(Operation("6+7").get === Sum(Integer(6), Integer(7)))
	assert(Operation("(7+8)*3").get === Product(Sum(Integer(7), Integer(8)), Integer(3)))
	assert(Operation("7+8*3").get === Sum(Integer(7), Product(Integer(8), Integer(3))))
	//assert(Operation("4(3)").get === Product(Integer(4), Integer(3)))
	//assert(Operation("(4)3").get === Product(Integer(4), Integer(3)))
	assert(Operation("5-6").get === Difference(Integer(5), Integer(6)))
	//assert(Operation("7/8*(6-4)").get === Product(Fraction(Integer(7), Integer(8)), Difference(Integer(6), Integer(4))))
	//assert(Operation("7/8*6-4").get === Difference(Product(Fraction(Integer(7), Integer(8)), Integer(6)), Integer(4)))
	assert(Operation("7^{6}").get === Exponentiation(Expression("7").get, Expression("6").get))
	assert(Operation("(45+3x)^{3}").get === Exponentiation(Expression("45+3x").get, Expression("3").get))
	assert(Operation("\\log_[3]{4}").get === Logarithm(Expression("3").get, Expression("4").get))
  }
	
  test("products" ){
	val sixTimesSix = Integer(6) * Integer(6)
	assert(sixTimesSix.description === "Integer(36)")
	assert(sixTimesSix.toLaTeX === "36")
	//assert(sixTimesSix.simplify === Integer(36))

	val sixTimesSixTimesTwo = Integer(6) * Integer(6) * Integer(2)
	assert(sixTimesSixTimesTwo.description === "Integer(72)")
	assert(sixTimesSixTimesTwo.toLaTeX === "72")
	//assert(sixTimesSixTimesTwo.simplify === Integer(72))
  }
	
  test("quotients") {
	val nineOverThree = Quotient(Integer(9), Integer(3))
	assert(nineOverThree.description === "Quotient(Integer(9), Integer(3))")
	assert(nineOverThree.toLaTeX === "9\\div3")
	//assert(nineOverFive.simplify === Integer(3)
  }
	
  test("differences") {
	val negEightMinusNegTwelve = Difference(Integer(-8), Integer(-12))
	assert(negEightMinusNegTwelve.description === "Difference(Integer(-8), Integer(-12))")
	assert(negEightMinusNegTwelve.toLaTeX === "-8-(-12)")
	//assert(negEightMinusNegTwelve.simplify === Integer(4))
  }
  
  test("sums") {
	val fourTimesSixPlusSevenTimesTwo = Sum(Product(Integer(4), Integer(6)), Product(Integer(7), Integer(2)))
	assert(fourTimesSixPlusSevenTimesTwo.description === "Sum(Product(Integer(4), Integer(6)), Product(Integer(7), Integer(2)))")
	assert(fourTimesSixPlusSevenTimesTwo.toLaTeX === "4\\cdot6+7\\cdot2")
	//assert(fourTimesSixPlusSevenTimesTwo.simplify.description === Integer(38))
  }

  test("roots") {
	val sqrtTwoTimesSqrtSeven = Product(SquareRoot(Integer(3)), SquareRoot(Integer(7)))
	assert(sqrtTwoTimesSqrtSeven.toLaTeX === "\\sqrt{3}\\cdot\\sqrt{7}")
	assert(sqrtTwoTimesSqrtSeven.description === "Product(SquareRoot(Integer(3)), SquareRoot(Integer(7)))")
	//assert(sqrtTwoTimesSqrtSeven.simplify === Exponentiation(Integer(14), Fraction(Integer(1), Integer(2))))
	val sqrtNegFiveTimesCubeRootFour = Product(SquareRoot(Integer(-5)), CubeRoot(Integer(4)))
	assert(sqrtNegFiveTimesCubeRootFour.toLaTeX === "\\sqrt{-5}\\cdot\\sqrt[3]{4}")
    assert(sqrtNegFiveTimesCubeRootFour.description === "Product(SquareRoot(Integer(-5)), CubeRoot(Integer(4)))")

	val sqrtThreeTimesSqrtFour = Product(SquareRoot(Integer(3)), SquareRoot(Integer(4)))
	assert(sqrtThreeTimesSqrtFour.toLaTeX === "\\sqrt{3}\\cdot\\sqrt{4}")
	assert(sqrtThreeTimesSqrtFour.description === "Product(SquareRoot(Integer(3)), SquareRoot(Integer(4)))")
	//assert(sqrtThreeTimesSqrtFour.simplify === Product(Integer(2), Exponentiation(Integer(3), Fraction(Integer(1), Integer(2)))))
	val fourthRoot16 = Root(Integer(4), Integer(16))
	assert(fourthRoot16.description === "Root(Index: Integer(4), Radicand: Integer(16))")
	assert(fourthRoot16.toLaTeX === "\\sqrt[4]{16}")
	//assert(fourthRoot16.simplify === Integer(2))
	val xthRootx2 = Root(Variable("x").get, Exponentiation(Variable("x").get, Integer(2)))
	assert(xthRootx2.description === "Root(Index: Variable(x), Radicand: Exponentiation(Variable(x), Integer(2)))")
	assert(xthRootx2.toLaTeX === "\\sqrt[x]{x^{2}}")
	//assert(xthRootx2.simplify === Integer(2))
  }

  test("negations") {
	val negFive = Negation(Integer(5))
	assert(negFive.description === "Negation(Integer(5))")
	assert(negFive.toLaTeX === "-5")
	//assert(negFive.simplify === Negation(Integer(5))
	val negOfNegFive = Negation(Integer(-5))
	assert(negOfNegFive.description === "Negation(Integer(-5))")
	assert(negOfNegFive.toLaTeX === "-(-5)")
	//assert(negOfNegFive.simplify === Integer(5))
	val negTerm = Negation(Term(Integer(-6), TreeMap[String, Integer]("x" -> Integer(2), "y" -> Integer(5))))
	assert(negTerm.description === "Negation(Term(Integer(-6), \"x\" -> Integer(2), \"y\" -> Integer(5)))")
	assert(negTerm.toLaTeX === "-(-6x^{2}y^{5})")
	//assert(negTerm.simplify === Term(Integer(-6), TreeMap[String, Integer]("x" -> Integer(2), "y" -> Integer(5))))
  }
}