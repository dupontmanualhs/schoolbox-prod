package math

import scala.util.parsing.combinator.RegexParsers

object Parser extends RegexParsers {
  def apply(input: String): Expression = parseAll(expr, input) match {
    case Success(result, _) => result
    case failure : NoSuccess => scala.sys.error(failure.msg)
  }
  
  def expr: Parser[Expression] = (operation | simpExpr) //make this 
  
  def variable: Parser[Variable] = ("""[a-df-hj-z]{1}""".r) ^^  { case name => new Variable(name) }
  
  def approx: Parser[Constant] = approxSign ~> number ^^ { value => ApproxNumber(value.getValue) }
  def approxSign: Parser[_] = ("\u2248" | """\approx""")
  
  //def grouping: Parser[Expression] = "(" ~> expr <~ ")"
  
  def simpExpr: Parser[Expression] = (variable | approx | number | squareRoot | logarithm | naturalLogarithm | negation)
  
  def number: Parser[Constant] = (real | fraction | integer | constant) 
  def real: Parser[RealNumber] = ( """[-]?\d+\.\d*""".r | """[-]?\d*\.\d+""".r) ^^ { str => Decimal(BigDecimal(str))}
  def fraction: Parser[Fraction] = (integer ~ "/" ~ integer) ^^ { case (num ~ "/" ~ denom) => Fraction(num, denom) }
  def integer: Parser[Integer] = """[-]?\d+""".r ^^ { digits => Integer(BigInt(digits)) }
  def constant: Parser[Constant] = "e" ^^^ ConstantE() | "\\pi" ^^^ ConstantPi() | "[pi]" ^^^ ConstantPi()

  def operation: Parser[Operation] = (product | sum | difference | quotient | exponentiation | squareRoot)
  def product: Parser[Product] = (simpExpr ~ "*" ~ expr) ^^ { case (left ~ "*" ~ right) => Product(left, right) } //|
                                 //(grouping ~ grouping) ^^ { case (lGroup ~ rGroup) => Product(lGroup, rGroup) }
  def sum: Parser[Sum] = (simpExpr ~ "+" ~ expr) ^^ { case (left ~ "+" ~ right) => Sum(left, right) } 
  def difference: Parser[Difference] = (simpExpr ~ "-" ~ expr) ^^ { case (left ~ "-" ~ right) => Difference(left, right) }
  def quotient: Parser[Quotient] = (simpExpr ~ "/" ~ expr) ^^ { case (left ~ "/" ~ right) => Quotient(left, right) } 
  def exponentiation: Parser[Exponentiation] = (simpExpr ~ "^" ~ expr) ^^ { case (base ~ "/" ~ exp) => Exponentiation(base, exp)}
  def squareRoot: Parser[SquareRoot] = "sqrt(" ~> number <~ ")" ^^ { case num => SquareRoot(num) }
  def logarithm: Parser[Base10Logarithm] = "log(" ~> number <~ ")" ^^ { case num => Base10Logarithm(num) }
  def naturalLogarithm: Parser[NaturalLogarithm] = "ln(" ~> number <~ ")" ^^ { case num => NaturalLogarithm(num) }
  def negation: Parser[Negation] = "-" ~> simpExpr ^^ { case expression => Negation(expression)} 
}