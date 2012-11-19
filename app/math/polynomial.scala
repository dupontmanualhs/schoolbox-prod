package math

import scala.collection.immutable.HashMap

class Polynomial(terms: List[Term]) extends Expression {
	def getTerms: List[Term] = terms
	def simplify: Expression = new Polynomial(this.getTerms)
	def getPrecedence: Int = 1
	def toOperation: Expression = {
		val termOperations: List[Expression] = this.getTerms.map(_.toOperation).toList
		if (termOperations.size >= 1) {
			termOperations.tail.foldLeft(termOperations.head)((x: Expression, y: Expression) => x.+(y))
		} else {
			Term("0").get.toOperation
		}
	}
	def toLaTeX: String = {
		if (this.unrefinedLaTeX.startsWith("+ ")) {
			this.unrefinedLaTeX.substring(2)
		} else { //first term is negative
			this.unrefinedLaTeX.substring(0, 1) + this.unrefinedLaTeX.substring(2) //get rid of space in "- <mathterm>"
		}
	}
	def unrefinedLaTeX: String = this.getTerms.map(monomialLaTeX(_)).mkString(" ")
	def monomialLaTeX(monomial: Term): String = {
		val coefficient = monomial.getCoefficient
		 if (coefficient != null && !coefficient.isNegative) {
			 "+ %s".format(monomial.toLaTeX)
		 } else {
			 "- %s".format(withoutNegativeSign(monomial.toLaTeX))
		 }
	}
	private def withoutNegativeSign(str: String): String = {
		"""-""".r.replaceFirstIn(str, "")
	}
	def description: String = this.getTerms.map(_.description).mkString("Polynomial(", ", ", ")")
	override def equals(that: Any): Boolean = {
		that match {
			case that: Polynomial => this.toOperation == that.toOperation
			case _ => false
		}
	}
	
	override def evaluate(variables : HashMap[Expression, Value]) = throw new UnsupportedOperationException()
}

object Polynomial {
	def apply(terms: List[Term]) = new Polynomial(terms)
	def apply(str: String): Option[Polynomial] = {
		val s = removeAllSpacesIn(str)
		val terms: List[String] = getAllTerms(s)
		if (allTermsAreValid(terms)) {
			val mathTerms: List[Term] = convertToTerms(terms)
			Some(Polynomial(mathTerms))
		} else {
			None
		}
	}
	def removeAllSpacesIn(s: String): String = {
		""" """.r.replaceAllIn(s, "")
	}
	def getAllTerms(s: String): List[String] = {
		val regexSplit = """(?<=\S)(?=[+-])""".r
		regexSplit.split(s).toList
	}
	def allTermsAreValid(terms: List[String]): Boolean = {
		terms.forall(Term(_) isDefined)
	}
	def convertToTerms(strings: List[String]): List[Term] = {
		(for (s <- strings) yield {
			Term(s).get
		}).toList
	}
}













