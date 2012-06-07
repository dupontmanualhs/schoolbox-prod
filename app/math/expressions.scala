package math
import scala.collection.immutable.HashMap

//MathExpression subclasses: MathOperation & MathValue (see below),
//                                         MathTerm
trait MathExpression {
	def simplify: MathExpression
	def getPrecedence: Int
	def toLaTeX: String
	def description: String
	def evaluate(variables: HashMap[MathExpression, MathValue]): MathExpression
	override def toString = this.toLaTeX
	def +(operand: MathExpression): MathExpression = {
	  (this, operand) match {
	    case (left: MathConstant, right: MathConstant) => left + right
	    case _ => MathSum(this, operand)
	  }
	}
	
	def -(operand: MathExpression): MathExpression = {
	  (this, operand) match {
	    case (left: MathConstant, right: MathConstant) => left - right
	    case _ => MathDifference(this, operand)
	  }
	}
	
	def *(operand: MathExpression): MathExpression = {
	  (this, operand) match {
	    case (left: MathConstant, right: MathConstant) => left * right
	    case _ => MathProduct(this, operand)
	  }
	}
	
	def /(operand: MathExpression): MathExpression = {
	  (this, operand) match {
	    case (left: MathConstant, right: MathConstant) => left / right
	    case _ => MathQuotient(this, operand)
	  }
	}
	
	def isNegative: Boolean = {
		this match {
			case complex: MathComplexNumber => complex.getReal.getValue < 0
			case constant: MathConstant => constant.getValue < 0
			case term: MathTerm => term.getCoefficient.getValue < 0
			case neg: MathNegation => true
			case basicOp: MathOperation if (basicOp != Nil && basicOp.hasTwoSides) => basicOp.getExpressions.head.isNegative
			case _ => false
		}
	}
	
	//returns true if an operator takes two arguments
	def hasTwoSides: Boolean = {
		this match {
			case sum: MathSum => true
			case dif: MathDifference => true
			case prod: MathProduct => true
			case quot: MathQuotient => true
			case _ => false
		}
	}
	def simplePrecedence: Int = {
		this match {
			case expr: MathOperation if (expr.hasTwoSides) => expr.getPrecedence / 2
			case _ => this.getPrecedence
		}
	}
}

object MathExpression {
	def apply(s: String): Option[MathExpression] = {
		if (!parenthesesAlignIn(s)) {
			None
		} else {
			stringToExpression(s)
		}
	}
	private def stringToExpression(s: String): Option[MathExpression] = {
		val result: Option[MathExpression] = MathNegation(s) orElse MathValue(s) orElse MathOperation(s) orElse MathTerm(s) //orElse MathPolynomial(s)
		if (!result.isDefined && (hasOutsideParens(s) || s.contains(" "))) {
			MathExpression(removeTrivialParts(s))
		} else {
			result
		}
	}
	private def removeTrivialParts(s: String): String = {
		removeFirstPlusIn(removeSpacesIn(removeOutsideParensIn(s)))
	}

	private def hasOutsideParens(s: String): Boolean = {
		s.startsWith("(") && s.endsWith(")") ||
		s.startsWith("{") && s.endsWith("}") ||
		s.startsWith("[") && s.endsWith("]")
	}

	def parenthesesAlignIn(s: String): Boolean = {
		s.count(_ == '(') == s.count(_ == ')') &&
		s.count(_ == '{') == s.count(_ == '}') &&
		s.count(_ == '[') == s.count(_ == ']')
	}
	def removeOutsideParensIn(s: String): String = {
		if (hasOutsideParens(s)) {
			s.substring(1, s.length() - 1)
		} else {
			s
		}
	}
	private def removeSpacesIn(s: String): String = {
		""" """.r.replaceAllIn(s, "")
	}
	private def removeFirstPlusIn(s: String): String = {
		"""^\+""".r.replaceFirstIn(s, "")
	}
	//returns either the original variable/expression or the value it represents in the HashMap
	def checkVar(expr: MathExpression, variables: HashMap[MathExpression, MathValue]): MathExpression = {
	  if(expr.isInstanceOf[MathVariable]){
	    variables.getOrElse(expr.asInstanceOf[MathVariable], expr)
	  } else {
	    expr
	  }
	}
}



//MathValue subclasses: MathConstant (constants.scala)
//                                 MathVariable (vars.scala)
abstract class MathValue extends MathExpression {
	override def getPrecedence: Int = 6
}

object MathValue {
	def apply(s: String): Option[MathValue] = {
		MathConstant(s) orElse MathVariable(s)
	}

	def apply(c: Char): Option[MathVariable] = {
		MathVariable(c)
	}
}
