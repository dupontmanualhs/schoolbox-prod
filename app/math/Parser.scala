package math

import scala.util.parsing.combinator._

object Parser extends RegexParsers with PackratParsers {
  def apply(input: String): Expression = parseAll(expr, input) match {
    case Success(result, _) => result
    case failure : NoSuccess => scala.sys.error(failure.msg)
  }
  
  lazy val expr: PackratParser[Expression] = (operation | simpExpr | grouping) //make this 
  
  lazy val variable: PackratParser[Var] = ("""[a-df-hj-z]{1}""".r) ^^  { case name => new Var(name) }
  
  lazy val approx: PackratParser[Constant] = approxSign ~> number ^^ { value => ApproxNumber(value.getValue) }
  lazy val approxSign: PackratParser[_] = ("\u2248" | """\approx""")
  
  def grouping: Parser[Expression] = "(" ~> expr <~ ")"
  
  lazy val simpExpr: PackratParser[Expression] = (variable | approx | number | squareRoot | logarithm | naturalLogarithm | negation)
  
  lazy val number: PackratParser[Constant] = (real | fraction | integer | constant) 
  lazy val real: PackratParser[RealNumber] = ( """[-]?\d+\.\d*""".r | """[-]?\d*\.\d+""".r) ^^ { str => Decimal(BigDecimal(str))}
  lazy val fraction: PackratParser[Fraction] = (integer ~ "/" ~ integer) ^^ { case (num ~ "/" ~ denom) => Fraction(num, denom) }
  lazy val integer: PackratParser[Integer] = """[-]?\d+""".r ^^ { digits => Integer(BigInt(digits)) }
  lazy val constant: PackratParser[Constant] = "e" ^^^ ConstantE() | "\\pi" ^^^ ConstantPi() | "[pi]" ^^^ ConstantPi()

  lazy val operation: PackratParser[Operation] = (product | sum | difference | quotient | exponentiation | squareRoot)
  lazy val product: PackratParser[Product] = (expr ~ "*" ~ expr) ^^ { case (left ~ "*" ~ right) => Product(left, right) } |
                                             (expr ~ grouping) ^^ { case (left ~ right) => Product(left, right) }
  lazy val sum: PackratParser[Sum] = (expr ~ "+" ~ expr) ^^ { case (left ~ "+" ~ right) => Sum(left, right) } 
  lazy val difference: PackratParser[Difference] = (expr ~ "-" ~ expr) ^^ { case (left ~ "-" ~ right) => Difference(left, right) }
  lazy val quotient: PackratParser[Quotient] = (expr ~ "/" ~ expr) ^^ { case (left ~ "/" ~ right) => Quotient(left, right) } 
  lazy val exponentiation: PackratParser[Exponentiation] = (expr ~ "^" ~ expr) ^^ { case (base ~ "^" ~ exp) => Exponentiation(base, exp)}
  lazy val squareRoot: PackratParser[SquareRoot] = "sqrt(" ~> expr <~ ")" ^^ { case expr => SquareRoot(expr) }
  lazy val logarithm: PackratParser[Base10Logarithm] = "log(" ~> expr <~ ")" ^^ { case expr => Base10Logarithm(expr) }
  lazy val naturalLogarithm: PackratParser[NaturalLogarithm] = "ln(" ~> expr <~ ")" ^^ { case expr => NaturalLogarithm(expr) }
  lazy val negation: PackratParser[Negation] = "-" ~> expr ^^ { case expr => Negation(expr)} 
}