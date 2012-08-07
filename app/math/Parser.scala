package math

import scala.util.parsing.combinator.RegexParsers

object Parser extends RegexParsers {
  def apply(input: String): Expression = parseAll(expr, input) match {
    case Success(result, _) => result
    case failure : NoSuccess => scala.sys.error(failure.msg)
  }
  
  def expr: Parser[Expression] = (approx | number)
  
  def approx: Parser[Constant] = approxSign ~> number ^^ { value => ApproxNumber(value.getValue) }
  def approxSign: Parser[_] = ("\u2248" | """\approx""")
  
  def number: Parser[Constant] = (real | fraction | integer | constant) 
  def real: Parser[RealNumber] = ( """[-]?\d+\.\d*""".r | """[-]?\d*\.\d+""".r) ^^ { str => Decimal(BigDecimal(str))}
  def fraction: Parser[Fraction] = (integer ~ "/" ~ integer) ^^ { case (num ~ "/" ~ denom) => Fraction(num, denom) }
  def integer: Parser[Integer] = """[-]?\d+""".r ^^ { digits => Integer(BigInt(digits)) }
  def constant: Parser[Constant] = "e" ^^^ ConstantE() | "\\pi" ^^^ ConstantPi()

}