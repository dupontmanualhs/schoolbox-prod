package math

import scala.collection.immutable.TreeMap
import scala.collection.immutable.HashMap

class Vars(val varsWithPowers: TreeMap[Variable, Integer]) {
  
}

class Term(coefficient: Constant, variableSequence: TreeMap[String, Integer]) extends Expression {
  def getCoefficient: Constant = coefficient
  def getVariableSequence: TreeMap[String, Integer] = variableSequence
  override def getPrecedence: Int = 2
  def toOperation: Expression = {
    if (this.getVariableSequence == Nil) {
      this.getCoefficient * Integer(1)
    } else {
      val varPows: List[Exponentiation] = getVariableSequence.map(varPowToExponentiation(_)).toList
      varPows.foldLeft(this.getCoefficient: Expression)((x: Expression, y: Expression) => x.*(y)) match {
        case coef: Constant => coef * Integer(1)
        case anOp: Operation => anOp
      }
    }
  }
  override def toLaTeX: String = this.coefficientLaTeX + this.variableSequenceLaTeX
  override def description: String = "Term(" + this.getCoefficient.description + { if (this.variableSequence == null || this.variableSequence.size == 0) "" else { ", " + variableSequenceDescription } } + ")"
  override def simplify = this
  /*override def simplify: Expression = {
		val coefficientSimplified = this.getCoefficient.simplify
		val varSequenceSimplified = simplifyVarSequence
		(coefficientSimplified, varSequenceSimplified) match {
			case (coef: Constant, varSeq: TreeMap[String, Integer]) => Term(coef, varSeq)
			case _ => Term(this.getCoefficient, this.getVariableSequence)
		}
	}

	private def simplifyVarSequence: TreeMap[String, Integer] = {

	}    */

  private def coefficientLaTeX: String = {
    if (this.getCoefficient.getValue == 1 && this.getVariableSequence != Nil) {
      ""
    } else if (this.getCoefficient.getValue == -1 && this.getVariableSequence != Nil) {
      "-"
    } else if (this.getCoefficient.isInstanceOf[ComplexNumber]) {
      "(%s)".format(this.getCoefficient.toLaTeX)
    } else {
      this.getCoefficient.toLaTeX
    }
  }
  private def varPowToExponentiation(varPow: (String, Integer)): Exponentiation = {
    Exponentiation(Variable(varPow._1).get, varPow._2)
  }
  private def variableSequenceDescription: String = {
    (for ((name: String, pow: Integer) <- this.getVariableSequence) yield {
      "\"%s\" -> %s".format(name, pow.description)
    }).mkString(", ")
  }

  private def variableSequenceLaTeX: String = {
    (for ((name: String, pow: Integer) <- this.getVariableSequence) yield {
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
      case that: Term => this.toOperation == that.toOperation
      case _ => false
    }
  }

  override def evaluate(variables: HashMap[Expression, Value]) = Integer(0)
}

object Term {
  def apply(coefficient: Constant, variableSequence: TreeMap[String, Integer]) = new Term(coefficient, variableSequence)
  //def apply(coefficient: Constant, variableSequence: TreeMap[Variable, Integer]): Term = Term(coefficient, variableSequence.foreach((mathVar, mathInt) => (mathVar.getName, mathInt)))
  def apply(variableSequence: TreeMap[String, Integer]): Term = Term(Integer(1), variableSequence)
  def apply(coefficient: Constant, variableSequence: (String, Integer)*): Term = Term(coefficient, TreeMap[String, Integer](variableSequence: _*))
  def apply(variableSequence: (String, Integer)*): Term = Term(Integer(1), variableSequence: _*)

  def apply(s: String): Option[Term] = {
    val potentialTermSegments: Array[String] = getTermSegments(s)
    if (potentialTermSegments.size < 1) {
      None
    } else {
      getTermFromArray(potentialTermSegments)
    }
  }

  def getTermFromArray(termSegments: Array[String]): Option[Term] = {
    val potentialCoefficient: Option[Constant] = if (termSegments.head == "-") {
      Constant("-1")
    } else if (termSegments.head == "+") {
      Constant("1")
    } else {
      Constant(termSegments.head)
    }
    val variableSegments = arrayWithoutCoefficient(potentialCoefficient, termSegments)
    val potentialVariableSequence: Option[TreeMap[String, Integer]] = extractVarSequenceFromArray(variableSegments)
    val coefficient = potentialCoefficient match {
      case None => Integer(1)
      case Some(mathConstant) => mathConstant
    }
    (coefficient, potentialVariableSequence) match {
      case (_, None) => None
      case (coef, Some(treeMap)) => Some(new Term(coef, treeMap))
    }
  }

  def arrayWithoutCoefficient(coefToRemove: Option[Constant], strings: Array[String]): Array[String] = {
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

  def extractVarSequenceFromArray(strings: Array[String]): Option[TreeMap[String, Integer]] = {
    if (isVariableSequence(strings)) {
      Some(extractVarsPowered(strings))
    } else {
      None
    }
  }

  def isVariableSequence(strings: Array[String]): Boolean = {
    if (strings.isEmpty) true
    else strings.forall(extractVariablePowered(_).isDefined)
  }

  def extractVarsPowered(strings: Array[String]): TreeMap[String, Integer] = {
    TreeMap((for ((s: String) <- strings) yield {
      extractVariablePowered(s).get
    }): _*)
  }

  def extractVariablePowered(s: String): Option[(String, Integer)] = {
    val variableAndPowerSplit = """[\^]""".r.split(s)
    if (variableAndPowerSplit.isEmpty) {
      None
    } else if (variableAndPowerSplit.size == 1) { //string contains a var without a power
      Variable(variableAndPowerSplit.head) match {
        case Some(aVar) => Some(aVar.getName, Integer(1))
        case _ => None
      }
    } else {
      (Variable(variableAndPowerSplit.head), Integer(withoutBracketsAroundPower(variableAndPowerSplit.tail.mkString))) match {
        case (None, _) => None
        case (Some(aVar), None) => None
        case (Some(aVar), Some(aInteger)) => Some((aVar.getName, aInteger))
      }
    }
  }

  def withoutBracketsAroundPower(pow: String): String = {
    """[\{\}]""".r.replaceAllIn(pow, "")
  }

}