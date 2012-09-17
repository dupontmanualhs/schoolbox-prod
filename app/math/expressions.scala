package math
import scala.collection.immutable.HashMap

//Expression subclasses: Operation & Value (see below),
//                                       Term
trait Expression {
	def simplify: Expression
	def getPrecedence: Int
	def toLaTeX: String
	def description: String
	def evaluate(variables: HashMap[Expression, Value]): Expression
	override def toString = this.toLaTeX
	def +(operand: Expression): Expression = {
	  (this, operand) match {
	    case (left: Constant, right: Constant) => left + right
	    case _ => Sum(this, operand)
	  }
	}
	
	def -(operand: Expression): Expression = {
	  (this, operand) match {
	    case (left: Constant, right: Constant) => left - right
	    case _ => Difference(this, operand)
	  }
	}
	
	def *(operand: Expression): Expression = {
	  (this, operand) match {
	    case (left: Constant, right: Constant) => left * right
	    case _ => Product(this, operand)
	  }
	}
	
	def /(operand: Expression): Expression = {
	  (this, operand) match {
	    case (left: Constant, right: Constant) => left / right
	    case _ => Quotient(this, operand)
	  }
	}
	
	def isNegative: Boolean = {
		this match {
			case complex: ComplexNumber => complex.getReal.getValue < 0
			case constant: Constant => constant.getValue < 0
			case term: Term => term.getCoefficient.getValue < 0
			case neg: Negation => true
			case basicOp: Operation if (basicOp != Nil && basicOp.hasTwoSides) => basicOp.getExpressions.head.isNegative
			case _ => false
		}
	}
	
	//returns true if an operator takes two arguments
	def hasTwoSides: Boolean = {
		this match {
			case sum: Sum => true
			case dif: Difference => true
			case prod: Product => true
			case quot: Quotient => true
			case _ => false
		}
	}
	def simplePrecedence: Int = {
		this match {
			case expr: Operation if (expr.hasTwoSides) => expr.getPrecedence / 2
			case _ => this.getPrecedence
		}
	}
}

object Expression {
	def apply(s: String): Option[Expression] = {
		if (!parenthesesAlignIn(s)) {
			None
		} else {
			stringToExpression(s)
		}
	}
	private def stringToExpression(s: String): Option[Expression] = {
		val result: Option[Expression] = Negation(s) orElse Value(s) orElse Operation(s) orElse Term(s) //orElse Polynomial(s)
		if (!result.isDefined && (hasOutsideParens(s) || s.contains(" "))) {
			Expression(removeTrivialParts(s))
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
	def checkVar(expr: Expression, variables: HashMap[Expression, Value]): Expression = {
	  if(expr.isInstanceOf[Var]){
	    variables.getOrElse(expr.asInstanceOf[Var], expr)
	  } else {
	    expr
	  }
	}
}



//Value subclasses: Constant (constants.scala)
//                                 Variable (vars.scala)
abstract class Value extends Expression {
	override def getPrecedence: Int = 6
}

object Value {
  def apply(s: String): Option[Value] = {
	Constant(s) match {
	  case Some(c) => Some(c)
	  case None => try {
	    Some(Var(s))
	  } catch {
	    case e: IllegalArgumentException => None
	  }
	}
  }

  def apply(c: Char): Option[Value] = {
	Value(c.toString)
  }
}
