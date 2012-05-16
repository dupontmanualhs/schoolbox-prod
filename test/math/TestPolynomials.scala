package math

import org.scalatest.FunSuite
import scala.collection.immutable.TreeMap

class TestPolynomials extends FunSuite {
  test("terms") {
    val sixX2 = MathTerm(MathInteger(6), TreeMap[String, MathInteger]("x" -> MathInteger(2)))
    assert(sixX2.toLaTeX === "6x^{2}")
    assert(sixX2.description === "MathTerm(MathInteger(6), \"x\" -> MathInteger(2))")
    assert(sixX2.toMathOperation === MathInteger(6) * MathExponentiation(MathVariable("x").get, MathInteger(2)))
    val fourx3y5z = MathTerm(MathInteger(4), TreeMap[String, MathInteger]("x" -> MathInteger(3), "y" -> MathInteger(5), "z" -> MathInteger(1)))
    assert(fourx3y5z.toLaTeX === "4x^{3}y^{5}z")
    assert(fourx3y5z.toMathOperation === MathInteger(4) * MathExponentiation(MathVariable("x").get, MathInteger(3)) * MathExponentiation(MathVariable("y").get, MathInteger(5)) * MathExponentiation(MathVariable("z").get, MathInteger(1)))
    val ninex0y7 = MathTerm(MathInteger(9), TreeMap[String, MathInteger]("x" -> MathInteger(0), "y" -> MathInteger(7)))
    assert(ninex0y7.toLaTeX === "9y^{7}")
    val eight = MathTerm("8").get
    assert(eight.description === "MathTerm(MathInteger(8))")
    val eightx2 = MathTerm("8x^{2}").get
    assert(eightx2.toLaTeX === "8x^{2}")
    val x2 = MathTerm("x^2").get
    assert(x2.description === "MathTerm(MathInteger(1), \"x\" -> MathInteger(2))")
    assert(x2.toLaTeX === "x^{2}")
    val mess = MathTerm("^^^")
    assert(mess === None)
    val withParens = MathTerm("6(x^{2})(y^{3})")
    assert(withParens === None)
    val x2yz = MathTerm("x^{2}yz").get
    assert(x2yz.description === "MathTerm(MathInteger(1), \"x\" -> MathInteger(2), \"y\" -> MathInteger(1), \"z\" -> MathInteger(1))")
    assert(x2yz.toLaTeX === "x^{2}yz")
  }

  test("polynomials") {
    assert(MathPolynomial("45x^2y^34 - 5x").get === MathPolynomial(List[MathTerm](MathTerm("45x^2y^34").get, MathTerm("-5x").get)))
    assert(MathPolynomial("-45x^{2}y^{34} + 5x").get === MathPolynomial(List[MathTerm](MathTerm("-45x^2y^34").get, MathTerm("5x").get)))
    assert(MathPolynomial("2x^{2}y^{3} - x^{3}y^{4} - 3z^{5}").get === MathPolynomial(List[MathTerm](MathTerm("2x^2y^3").get, MathTerm("-x^3y^4").get, MathTerm("-3z^5").get)))

    val poly = MathPolynomial(List[MathTerm](MathTerm("45x^2y^34").get, MathTerm("-5x").get))
    assert(poly.description === "MathPolynomial(MathTerm(MathInteger(45), \"x\" -> MathInteger(2), \"y\" -> MathInteger(34)), MathTerm(MathInteger(-5), \"x\" -> MathInteger(1)))")
    assert(poly.toLaTeX === "45x^{2}y^{34} - 5x")
    assert(poly.toMathOperation === MathTerm("45x^{2}y^{34}").get.toMathOperation + MathTerm("-5x").get.toMathOperation)
    val negPoly = MathPolynomial(List[MathTerm](MathTerm("-45x^2y^34").get, MathTerm("5x").get))
    assert(negPoly.toLaTeX === "-45x^{2}y^{34} + 5x")
    assert(negPoly.toMathOperation === MathTerm("-45x^{2}y^{34}").get.toMathOperation + MathTerm("5x").get.toMathOperation)
    val longPoly = MathPolynomial(List[MathTerm](MathTerm("2x^2y^3").get, MathTerm("-x^3y^4").get, MathTerm("-3z^5").get))
    assert(longPoly.toLaTeX === "2x^{2}y^{3} - x^{3}y^{4} - 3z^{5}")
    assert(longPoly.toMathOperation === MathTerm("2x^2y^3").get.toMathOperation + MathTerm("-x^3y^4").get.toMathOperation + MathTerm("-3z^5").get.toMathOperation)
  }
}