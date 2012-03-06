package math

object MathImplicitConversions {
	implicit def bigIntToMathInteger(i: BigInt) = MathInteger(i)
	implicit def bigDecimalToMathDecimal(i: BigDecimal) = MathDecimal(i)
}