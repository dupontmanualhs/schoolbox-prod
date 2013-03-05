package math

import scala.collection.immutable.HashMap

class Var(val name: String) extends Value {
	require(name.length == 1 && name.charAt(0).isLetter && !name.equalsIgnoreCase("e") && name != "i", "%s is not a valid Variable. Variable cannot be \"e\", \"E\", or \"i\"".format(name))
	def getName: String = name
	override def simplify: Expression = new Var(this.getName)
	override def getPrecedence: Int = 6
	override def toLaTeX: String = this.getName
	override def description: String = "Var(" + this.getName + ")"
	override def toString = this.getName
	override def equals(that: Any): Boolean = {
		that match {
			case that: Var => (that.getName == this.getName)
			case _ => false
		}
	}
	override def evaluate(variables : HashMap[Expression, Value]) = {
	  Expression.checkVar(this, variables)
	}
}

object Var {
	def apply(name: String) = new Var(name)
	def apply(name: Char) = new Var(name.toString)
}