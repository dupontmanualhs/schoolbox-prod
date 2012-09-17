package math

import org.scalatest.FunSuite

class TestParser extends FunSuite {
  test("numbers") {
    assert(Parser("1") === Integer(1))
    assert(Parser("-23") === Integer(-23))
    assert(Parser("0") === Integer(0))
    assert(Parser("1234567890") === Integer(BigInt("1234567890")))
    assert(Parser("1/2") === Fraction(Integer(1), Integer(2)))
    assert(Parser("0.35") === Decimal(0.35))
    assert(Parser("\u22482") === ApproxNumber(2.0))
    assert(Parser("""\approx2.17""") === ApproxNumber(2.17))
    assert(Parser("-17/-22") === Fraction(Integer(-17), Integer(-22)))
    assert(Parser("\u22483.75") === ApproxNumber(3.75))
    assert(Parser("e") === ConstantE())
    assert(Parser("""\pi""") === ConstantPi())
    //assert(Parser("3-2i") === ComplexNumber(Integer(3), Integer(-2)))
    //assert(Parser("2+3i") === ComplexNumber(Integer(2), Integer(3)))
    //assert(Parser("7/2+1/3i))
  }
  
  test("vars") {
    assert(Parser("x") === Var("x"))
    assert(Parser("y") === Var("y"))
    assert(Parser("a") === Var("a"))
  }
  
  test("operations") {
    assert(Parser("x+2") === Sum(Var("x"), Integer(BigInt("2"))))
    assert(Parser("x-2") === Difference(Var("x"), Integer(BigInt("2"))))
    assert(Parser("x*2") === Product(Var("x"), Integer(BigInt("2"))))
    assert(Parser("x/2") === Quotient(Var("x"), Integer(BigInt("2"))))
    assert(Parser("x-1") === Difference(Var("x"), Integer(BigInt("1"))))
    assert(Parser("x^2") === Exponentiation(Var("x"), Integer(BigInt("2"))))
    assert(Parser("(x+2)/y") === Quotient(Sum(Var("x"), Integer(BigInt("2"))), Var("y")))
    assert(Parser("-1") === Integer(BigInt("-1")))
    assert(Parser("(x+2)^(x-1)") === {
        val x = Var("x")
        Exponentiation(Sum(x, Integer(BigInt("2"))), Difference(x, Integer(BigInt("1"))))
      }
    )
    assert(Parser("(x+2)(x-1)") === {
        val x = Var("x")
        Product(Sum(x, Integer(BigInt("2"))), Difference(x, Integer(BigInt("1"))))
      }
    )
    assert(Parser("(x+2)(x-1)") === Parser("(x+2)*(x-1)"))
    //assert(Parser("log(x)") === Base10Logarithm(Var("x")))
    //assert(Parser("ln(x)") === NaturalLogarithm(Var("x")))
    assert(Parser("(x+y)/(15-z)") === {
        val x = Var("x")
        val y = Var("y")
        val z = Var("z")
        val fifteen = Integer(BigInt("15"))
        Quotient(Sum(x, y), Difference(fifteen, z))
      }
    )
    assert(Parser("x/15-1") === Difference(Quotient(Var("x"), Integer(BigInt("15"))), Integer(BigInt("1"))))
    assert(Parser("(x+5)x+1") === {
        val x = Var("x")
        Sum(Product(Sum(x, Integer(BigInt("5"))), x), Integer(BigInt("1")))
        }
      )
  }
}
