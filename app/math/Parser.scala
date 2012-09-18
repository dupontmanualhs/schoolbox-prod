package math

import scala.util.parsing.combinator._

object Parser extends RegexParsers with PackratParsers {
  def apply(input: String): Expression = parseAll(expr, input) match {
    case Success(result, _) => result
    case failure : NoSuccess => scala.sys.error(failure.msg)
  }

  lazy val addOrSub: PackratParser[(Expression, Expression) => Expression] = ("+" | "-") ^^ { op =>
    (left: Expression, right: Expression) => op match {
      case "+" => Sum(left, right)
      case "-" => Difference(left, right)
    }
  }
  lazy val expr: PackratParser[Expression] = chainl1(term, addOrSub)
  
  lazy val multOrDiv: PackratParser[(Expression, Expression) => Expression] = ("*" | "/") ^^ { op =>
    (left: Expression, right: Expression) => op match {
      case "*" => Product(left, right)
      case "/" => Quotient(left, right)
    }  
  }
  lazy val term: PackratParser[Expression] = 
    (grouping ~ grouping) ^^ { case (left ~ right) => Product(left, right) } |
    (expt ~ grouping) ^^ { case (left ~ right) => Product(left, right) } |
    (grouping ~ expt) ^^ { case (left ~ right) => Product(left, right) } |
    chainl1(expt, multOrDiv)
    
  lazy val toThe: PackratParser[(Expression, Expression) => Expression] = 
    "^" ^^^ { (left: Expression, right: Expression) => Exponentiation(left, right) }
  lazy val expt: PackratParser[Expression] = 
    "-" ~> expt ^^ { case expt => Negation(expt) } |
    (prim <~ "^") ~ expt ^^ { case (base ~ expt) => Exponentiation(base, expt) } |
    prim
    
  lazy val prim: PackratParser[Expression] = (approx | number | squareRoot | logarithm | naturalLogarithm | variable | grouping )
  
  lazy val approx: PackratParser[Constant] = approxSign ~> number ^^ { value => ApproxNumber(value.getValue) }
  lazy val approxSign: PackratParser[_] = ("\u2248" | """\approx""")

  lazy val number: PackratParser[Constant] = (real | fraction | integer | constant) 
  lazy val real: PackratParser[RealNumber] = ( """[-]?\d+\.\d*""".r | """[-]?\d*\.\d+""".r) ^^ { str => Decimal(BigDecimal(str))}
  lazy val fraction: PackratParser[Fraction] = (integer ~ "/" ~ integer) ^^ { case (num ~ "/" ~ denom) => Fraction(num, denom) }
  lazy val integer: PackratParser[Integer] = """[-]?\d+""".r ^^ { digits => Integer(BigInt(digits)) }
  lazy val constant: PackratParser[Constant] = "e" ^^^ ConstantE() | "\\pi" ^^^ ConstantPi() | "[pi]" ^^^ ConstantPi()

  lazy val squareRoot: PackratParser[SquareRoot] = "sqrt(" ~> expr <~ ")" ^^ { case expr => SquareRoot(expr) }
  lazy val logarithm: PackratParser[Base10Logarithm] = "log(" ~> expr <~ ")" ^^ { case expr => Base10Logarithm(expr) }
  lazy val naturalLogarithm: PackratParser[NaturalLogarithm] = "ln(" ~> expr <~ ")" ^^ { case expr => NaturalLogarithm(expr) }

  lazy val variable: PackratParser[Var] = ("""[a-df-hj-z]{1}""".r) ^^  { case name => new Var(name) }
  
  lazy val grouping: PackratParser[Expression] = "(" ~> expr <~ ")" | "[" ~> expr <~ "]"
  
}