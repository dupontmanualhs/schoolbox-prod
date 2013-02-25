package math

import org.scalatest.FunSuite

class TestParser extends FunSuite {
  test("numbers 1") {
    assert(Parser("1") === Integer(1))
    assert(Parser("-23") === Integer(-23))
    assert(Parser("0") === Integer(0))
    assert(Parser("1234567890") === Integer(BigInt("1234567890")))
    assert(Parser("1/2") === Fraction(Integer(1), Integer(2)))
    assert(Parser("0.35") === Decimal(0.35))
  }
  
  test("numbers 2") {
    assert(Parser("\u22482") === ApproxNumber(2.0))
    assert(Parser("""\approx2.17""") === ApproxNumber(2.17))
    assert(Parser("-17/-22") === Fraction(Integer(-17), Integer(-22)))
    assert(Parser("\u22483.75") === ApproxNumber(3.75))
    assert(Parser("e") === ConstantE())
    assert(Parser("""\pi""") === ConstantPi())
  }
  
  test("complex numbers") {
    assert(Parser("3-2i") === ComplexNumber(Integer(3), Integer(-2)))
    assert(Parser("2+3i") === ComplexNumber(Integer(2), Integer(3)))
    assert(Parser("7/2+1/3i") === ComplexNumber(Fraction(Integer(7), Integer(2)), Fraction(Integer(1), Integer(3))))
  }
  
  test("complex numbers solo terms") {
    assert(Parser("4i") === ComplexNumber(Integer(0), Integer(4)))
  }
  
  //gotta make these
  test("vars") {}
}