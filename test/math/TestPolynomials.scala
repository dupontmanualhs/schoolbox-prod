package math

import org.scalatest.FunSuite
import scala.collection.immutable.TreeMap

class TestPolynomials extends FunSuite {
  test("terms") {
    val sixX2 = Term(Integer(6), TreeMap[String, Integer]("x" -> Integer(2)))
    assert(sixX2.toLaTeX === "6x^{2}")
    assert(sixX2.description === "Term(Integer(6), \"x\" -> Integer(2))")
    assert(sixX2.toOperation === Integer(6) * Exponentiation(Variable("x").get, Integer(2)))
    val fourx3y5z = Term(Integer(4), TreeMap[String, Integer]("x" -> Integer(3), "y" -> Integer(5), "z" -> Integer(1)))
    assert(fourx3y5z.toLaTeX === "4x^{3}y^{5}z")
    assert(fourx3y5z.toOperation === Integer(4) * Exponentiation(Variable("x").get, Integer(3)) * Exponentiation(Variable("y").get, Integer(5)) * Exponentiation(Variable("z").get, Integer(1)))
    val ninex0y7 = Term(Integer(9), TreeMap[String, Integer]("x" -> Integer(0), "y" -> Integer(7)))
    assert(ninex0y7.toLaTeX === "9y^{7}")
    val eight = Term("8").get
    assert(eight.description === "Term(Integer(8))")
    val eightx2 = Term("8x^{2}").get
    assert(eightx2.toLaTeX === "8x^{2}")
    val x2 = Term("x^2").get
    assert(x2.description === "Term(Integer(1), \"x\" -> Integer(2))")
    assert(x2.toLaTeX === "x^{2}")
    val mess = Term("^^^")
    assert(mess === None)
    val withParens = Term("6(x^{2})(y^{3})")
    assert(withParens === None)
    val x2yz = Term("x^{2}yz").get
    assert(x2yz.description === "Term(Integer(1), \"x\" -> Integer(2), \"y\" -> Integer(1), \"z\" -> Integer(1))")
    assert(x2yz.toLaTeX === "x^{2}yz")
  }

  test("polynomials") {
    assert(Polynomial("45x^2y^34 - 5x").get === Polynomial(List[Term](Term("45x^2y^34").get, Term("-5x").get)))
    assert(Polynomial("-45x^{2}y^{34} + 5x").get === Polynomial(List[Term](Term("-45x^2y^34").get, Term("5x").get)))
    assert(Polynomial("2x^{2}y^{3} - x^{3}y^{4} - 3z^{5}").get === Polynomial(List[Term](Term("2x^2y^3").get, Term("-x^3y^4").get, Term("-3z^5").get)))

    val poly = Polynomial(List[Term](Term("45x^2y^34").get, Term("-5x").get))
    assert(poly.description === "Polynomial(Term(Integer(45), \"x\" -> Integer(2), \"y\" -> Integer(34)), Term(Integer(-5), \"x\" -> Integer(1)))")
    assert(poly.toLaTeX === "45x^{2}y^{34} - 5x")
    assert(poly.toOperation === Term("45x^{2}y^{34}").get.toOperation + Term("-5x").get.toOperation)
    val negPoly = Polynomial(List[Term](Term("-45x^2y^34").get, Term("5x").get))
    assert(negPoly.toLaTeX === "-45x^{2}y^{34} + 5x")
    assert(negPoly.toOperation === Term("-45x^{2}y^{34}").get.toOperation + Term("5x").get.toOperation)
    val longPoly = Polynomial(List[Term](Term("2x^2y^3").get, Term("-x^3y^4").get, Term("-3z^5").get))
    assert(longPoly.toLaTeX === "2x^{2}y^{3} - x^{3}y^{4} - 3z^{5}")
    assert(longPoly.toOperation === Term("2x^2y^3").get.toOperation + Term("-x^3y^4").get.toOperation + Term("-3z^5").get.toOperation)
  }
}