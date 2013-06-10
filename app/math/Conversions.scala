package math

import scala.language.implicitConversions

object Conversions {
	implicit def bigIntToMathInteger(i: BigInt) = Integer(i)
	implicit def bigDecimalToMathDecimal(i: BigDecimal) = Decimal(i)
}