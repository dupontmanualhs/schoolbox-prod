package math

import org.scalatest.FunSuite

class TestValues extends FunSuite {
  test("values") {
    assert(Value("\\pi").get.toString === ConstantPi.symbol)
    assert(Value("x/2") === None)
    assert(Value("5x") === None)
    assert(Value("x8") === None)
    assert(Value("y^{2}") === None)
    assert(Value("4^{z}") === None)
    assert(Value("a+3") === None)
    assert(Value("a+b") === None)
    assert(Value("3-c") === None)
    assert(Value("a-b") === None)
    assert(Value("x*6") === None)
    assert(Value("x/y") === None)

    assert(Value("6").get === Integer(6))
    assert(Value("\\frac{5}{7}").get === Fraction(Integer(5), Integer(7)))
    assert(Value("\\pi").get === ConstantPi())
    assert(Value("e").get === ConstantE())
    assert(Value("7+4i").get === ComplexNumber(Integer(7), Integer(4)))
    assert(Value("x").get === Var("x"))
    assert(Value("4.56").get === Decimal(4.56))
    assert(Value("\\approx5").get === ApproxNumber(5))
    assert(Value("\\approx9").get === ApproxNumber(9))
  }
  
  test("integers") {
    val one: Value = Value("1").get
    assert(one.description === "Integer(1)")
    assert(one.toLaTeX === "1")
    val negative: Value = Value("-1").get
    assert(negative.description === "Integer(-1)")
    assert(negative.toLaTeX === "-1")
    for (i <- List(-3, 0, 1, 4, 9, 12, 51, 95)) {
      assert(Integer(i).isPrime === false, i.toString + " is not prime")
    }
    for (i <- List(2, 3, 5, 7, 11, 13, 97, 1493)) {
      assert(Integer(i).isPrime === true, i.toString + " is prime")
    }
  }

  test("fractions") {
    val four: Value = Value("\\frac{4}{1}").get
    assert(four.description === "Fraction(4, 1)")
    assert(four.toLaTeX === "\\frac{4}{1}")
    val oneHalf = Value("\\frac{1}{2}").get
    assert(oneHalf.description === "Fraction(1, 2)")
    assert(oneHalf.toLaTeX === "\\frac{1}{2}")
    val bigFraction = Value("\\frac{4567890123456}{-1234567890123}").get
    assert(bigFraction.description === "Fraction(BigInt(4567890123456), BigInt(-1234567890123))")
    assert(bigFraction.toLaTeX === "\\frac{4567890123456}{-1234567890123}")
    val fractionLaTeX = Value("\\frac{2}{3}").get
    assert(fractionLaTeX.description === "Fraction(2, 3)")
    assert(fractionLaTeX.toLaTeX === "\\frac{2}{3}")
  }

  test("terminating decimals") {
    val one = Value("1.0").get
    assert(one.description === "Decimal(\"1.0\")")
    assert(one.toLaTeX === "1.0")
    val small = Value("1E-24").get
    assert(small.description === "Decimal(\"1E-24\")")
    assert(small.toLaTeX === "1*10^{-24}")
    val big = Value("2.04E+3").get
    assert(big.description === "Decimal(\"2.04E+3\")")
    assert(big.toLaTeX === "2.04*10^{3}")
  }

  test("approximations") {
    val one = Value("\\approx1").get
    assert(one.description === "ApproxNumber(1)")
    assert(one.toLaTeX === "\\approx1")
    val oneHalf = Value("\\approx0.5").get
    assert(oneHalf.description === "ApproxNumber(0.5)")
    assert(oneHalf.toLaTeX === "\\approx0.5")
  }

  test("complex") {
    val oneI = Expression("0+1i").get
    assert(oneI.description === "ComplexNumber(Integer(0), Integer(1))")
    assert(oneI.toLaTeX === "i")
    val approx = Expression("\\approx(\\frac{3}{5}-\\frac{2}{3}i)").get
    assert(approx.description === "ComplexNumber(ApproxNumber(0.6), ApproxNumber(-0.6666666666666666))")
    assert(approx.toLaTeX === "\\approx(0.6-0.6666666666666666i)")
    val expts = Expression("2E+3-4E-3i").get
    assert(expts.description === "ComplexNumber(Decimal(\"2E+3\"), Decimal(\"-0.004\"))")
    assert(expts.toLaTeX === "2*10^{3}-0.004i")
    val withParens = Expression("(34+6i)").get
    assert(withParens.description === "ComplexNumber(Integer(34), Integer(6))")
    assert(withParens.toLaTeX === "34+6i")
    val neg = Expression("34-6i").get
    assert(neg.description === "ComplexNumber(Integer(34), Integer(-6))")
    assert(neg.toLaTeX === "34-6i")
    val variable = ComplexNumber("radius")
    assert(variable === None)
    val mess = ComplexNumber("xyz+4i")
    assert(mess === None)
    val xVar = ComplexNumber("x")
    assert(xVar === None)
  }

  test("the constant E") {
    val etest1 = ConstantE()
    assert(etest1.description === "ConstantE")
    assert(etest1.toLaTeX === "e")
    val etest2 = Constant("e").get
    assert(etest2.description === "ConstantE")
    assert(etest2.toLaTeX === "e")
    val etest3 = Constant("e").get
    assert(etest3.description === "ConstantE")
    assert(etest3.toLaTeX === "e")
  }

  test("the constant PI") {
    val piTest1 = ConstantPi()
    assert(piTest1.description === "ConstantPi")
    assert(piTest1.toLaTeX === "\\pi")
    val piTest2 = Constant("\\pi").get
    assert(piTest2.description === "ConstantPi")
    assert(piTest2.toLaTeX === "\\pi")
    val piTest3 = Value("\\pi").get
    assert(piTest3.description === "ConstantPi")
    assert(piTest3.toLaTeX === "\\pi")
  }

  test("variables") {
    val xVar = Var("x")
    assert(xVar.description === "Var(x)")
    assert(xVar.toLaTeX === "x")
    val xChar = Var('x')
    assert(xChar.description === "Var(x)")
    assert(xChar.toLaTeX === "x")
    val xVar2 = Value("x").get
    assert(xVar2.description === "Var(x)")
    assert(xVar2.toLaTeX === "x")
    val xChar2 = Value('x').get
    assert(xChar2.description === "Var(x)")
    assert(xChar2.toLaTeX === "x")
    intercept[IllegalArgumentException] {
      Var("pi")
    }
    intercept[IllegalArgumentException] {
      Var("e")
    }
  }
}