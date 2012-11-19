package math

import scala.collection.immutable.HashMap

class Variable(val name: String) extends Value {
	require(name.length == 1 && name.charAt(0).isLetter && !name.equalsIgnoreCase("e") && name != "i", "%s is not a valid Variable. Variable cannot be \"e\", \"E\", or \"i\"".format(name))
	def getName: String = name
	override def simplify: Expression = new Variable(this.getName)
	override def getPrecedence: Int = 6
	override def toLaTeX: String = this.getName
	override def description: String = "Variable(" + this.getName + ")"
	override def toString = this.getName
	override def equals(that: Any): Boolean = {
		that match {
			case that: Variable => (that.getName == this.getName)
			case _ => false
		}
	}
	override def evaluate(variables : HashMap[Expression, Value]) = {
	  Expression.checkVar(this, variables)
	}
}

object Variable {
	def apply(name: String): Option[Variable] = {
		if (name.length == 0 || name.length > 1 || !name.charAt(0).isLetter || name.equalsIgnoreCase("e") || name == "i") None
		else Some(new Variable(name))
	}
	def apply(name: Char): Option[Variable] = Variable(name.toString)
}