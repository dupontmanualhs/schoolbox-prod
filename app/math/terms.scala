package math

import scala.collection.immutable.TreeMap
import scala.collection.immutable.HashMap

class MathTerm(coefficient: MathConstant, variableSequence: TreeMap[String, MathInteger]) extends MathExpression {
	def getCoefficient: MathConstant = coefficient
	def getVariableSequence: TreeMap[String, MathInteger] = variableSequence
	override def getPrecedence: Int = 2
	def toMathOperation: MathExpression = {
		if (this.getVariableSequence == Nil) {
			this.getCoefficient * MathInteger(1)
		} else {
			val varPows: List[MathExponentiation] = getVariableSequence.map(varPowToExponentiation(_)).toList
			varPows.foldLeft(this.getCoefficient: MathExpression)((x: MathExpression, y: MathExpression) => x.*(y)) match {
				case coef: MathConstant => coef * MathInteger(1)
				case anOp: MathOperation => anOp
			}
		}
	}
	override def toLaTeX: String = this.coefficientLaTeX + this.variableSequenceLaTeX
	override def description: String = "MathTerm(" + this.getCoefficient.description + {if(this.variableSequence == null || this.variableSequence.size == 0) "" else {", " + variableSequenceDescription}} + ")"
	override def simplify = this
	/*override def simplify: MathExpression = {
		val coefficientSimplified = this.getCoefficient.simplify
		val varSequenceSimplified = simplifyVarSequence
		(coefficientSimplified, varSequenceSimplified) match {
			case (coef: MathConstant, varSeq: TreeMap[String, MathInteger]) => MathTerm(coef, varSeq)
			case _ => MathTerm(this.getCoefficient, this.getVariableSequence)
		}
	}

	private def simplifyVarSequence: TreeMap[String, MathInteger] = {

	}    */

	private def coefficientLaTeX: String = {
		if (this.getCoefficient.getValue == 1 && this.getVariableSequence != Nil) {
			""
		} else if (this.getCoefficient.getValue == -1 && this.getVariableSequence != Nil) {
			"-"
		} else if (this.getCoefficient.isInstanceOf[MathComplexNumber]) {
			"(%s)".format(this.getCoefficient.toLaTeX)
		} else {
			this.getCoefficient.toLaTeX
		}
	}
	private def varPowToExponentiation(varPow: (String, MathInteger)): MathExponentiation = {
		MathExponentiation(MathVariable(varPow._1).get, varPow._2)
	}
	private def variableSequenceDescription: String = {
		(for ((name: String, pow: MathInteger) <- this.getVariableSequence) yield {
			"\"%s\" -> %s".format(name, pow.description)
		}).mkString(", ")
	}

	private def variableSequenceLaTeX: String = {
		(for ((name: String, pow: MathInteger) <- this.getVariableSequence) yield {
			if (pow.getValue != 0) {
				name +
					{ if (pow.getValue != 1) "^{" + pow.toLaTeX + "}" else "" }
			} else {
				""
			}
		}).mkString
	}
	override def equals(that: Any): Boolean = {
		that match {
			case that: MathTerm => this.toMathOperation == that.toMathOperation
			case _ => false
		}
	}
	
	override def evaluate(variables : HashMap[MathExpression, MathValue]) = MathInteger(0)
}

object MathTerm {
	def apply(coefficient: MathConstant, variableSequence: TreeMap[String, MathInteger]) = new MathTerm(coefficient, variableSequence)
	//def apply(coefficient: MathConstant, variableSequence: TreeMap[MathVariable, MathInteger]): MathTerm = MathTerm(coefficient, variableSequence.foreach((mathVar, mathInt) => (mathVar.getName, mathInt)))
	def apply(variableSequence: TreeMap[String, MathInteger]): MathTerm = MathTerm(MathInteger(1), variableSequence)
	def apply(coefficient: MathConstant, variableSequence: (String, MathInteger)*): MathTerm = MathTerm(coefficient, TreeMap[String, MathInteger](variableSequence:_*))
	def apply(variableSequence: (String, MathInteger)*): MathTerm = MathTerm(MathInteger(1), variableSequence: _*)

	def apply(s: String): Option[MathTerm] = {
		val potentialTermSegments: Array[String] = getTermSegments(s)
		if (potentialTermSegments.size < 1) {
			None
		} else {
			getTermFromArray(potentialTermSegments)
		}
	}

	def getTermFromArray(termSegments: Array[String]): Option[MathTerm] = {
		val potentialCoefficient: Option[MathConstant] = {
			if (termSegments.head == "-") {
				MathConstant("-1")
			} else if (termSegments.head == "+") {
				MathConstant("1")
			} else {
				MathConstant(termSegments.head)
			}
		}
		val variableSegments = arrayWithoutCoefficient(potentialCoefficient, termSegments)
		val potentialVariableSequence: Option[TreeMap[String, MathInteger]] = extractVarSequenceFromArray(variableSegments)
		val coefficient = potentialCoefficient match {
			case None => MathInteger(1)
			case Some(mathConstant) => mathConstant
		}
		(coefficient, potentialVariableSequence) match {
			case (_, None) => None
			case (coef, Some(treeMap)) => Some(new MathTerm(coef, treeMap))
		}
	}

	def arrayWithoutCoefficient(coefToRemove: Option[MathConstant], strings: Array[String]): Array[String] = {
		coefToRemove match {
			case None => strings
			case _ => strings.tail
		}
	}

	def getTermSegments(s: String): Array[String] = {
		val splitTermRegex = """(?=([a-hj-zA-DF-Z](\^\{\d+\})?))""".r
		val termSegments = splitTermRegex.split(s)
		if (termSegments.head == "") {
			termSegments.tail
		} else {
			termSegments
		}
	}

	def extractVarSequenceFromArray(strings: Array[String]): Option[TreeMap[String, MathInteger]] = {
		if (isVariableSequence(strings)){
			Some(extractVarsPowered(strings))
		} else {
			None
		}
	}

	def isVariableSequence(strings: Array[String]): Boolean = {
		if (strings.isEmpty) true
		else strings.forall(extractVariablePowered(_).isDefined)
	}

	def extractVarsPowered(strings: Array[String]): TreeMap[String, MathInteger] = {
		TreeMap((for ((s: String) <- strings) yield {
			extractVariablePowered(s).get
		}):_*)
	}

	def extractVariablePowered(s: String): Option[(String, MathInteger)] = {
		val variableAndPowerSplit = """[\^]""".r.split(s)
		if (variableAndPowerSplit.isEmpty) {
			None
		} else if (variableAndPowerSplit.size == 1) { //string contains a var without a power
			MathVariable(variableAndPowerSplit.head) match {
				case Some(aVar) => Some(aVar.getName, MathInteger(1))
				case _ => None
			}
		} else {
			(MathVariable(variableAndPowerSplit.head), MathInteger(withoutBracketsAroundPower(variableAndPowerSplit.tail.mkString))) match {
				case (None, _) => None
				case (Some(aVar), None) => None
				case (Some(aVar), Some(aMathInteger)) => Some((aVar.getName, aMathInteger))
			}
		}
	}

	def withoutBracketsAroundPower(pow: String): String = {
		"""[\{\}]""".r.replaceAllIn(pow, "")
	}

}