package math

import scala.util.matching.Regex

abstract class MathOperation(expressions: List[MathExpression]) extends MathExpression {
    def getExpressions = expressions
    def getOperator: String
    def getClassName: String
    def binarySimplify(left: MathExpression, right: MathExpression): Option[MathExpression] = None
    override def description: String = {
        this.getExpressions.map(_.description).mkString(this.getClassName + "(", ", ", ")")
    }
    override def toLaTeX: String = {
        firstExpressionLaTeX(this.getExpressions.head) + this.getOperator + this.getExpressions.tail.map(expressionLaTeX(_)).mkString(this.getOperator)
    }
    def expressionLaTeX(expression: MathExpression): String = {
        if (expressionNeedsParentheses(this, expression)) {
            "(" + expression.toLaTeX + ")"
        } else {
            expression.toLaTeX
        }
    }

    def firstExpressionLaTeX(expression: MathExpression): String = {
        if (expression.simplePrecedence < this.simplePrecedence) {
            "(" + expression.toLaTeX + ")"
        } else {
            expression.toLaTeX
        }
    }

    def expressionNeedsParentheses(outsideExpression: MathExpression, expressionToTest: MathExpression): Boolean = {
        ((outsideExpression.isInstanceOf[MathDifference] || outsideExpression.isInstanceOf[MathQuotient]) && (expressionToTest.getPrecedence <= outsideExpression.getPrecedence || expressionToTest.isNegative)) ||
            (expressionToTest.getPrecedence < outsideExpression.getPrecedence) ||
            (expressionToTest.isInstanceOf[MathNegation]) ||
            (expressionToTest.isNegative && (outsideExpression.is_Sum_or_Difference_or_Product_or_Quotient || outsideExpression.isInstanceOf[MathNegation]))
    }

    override def equals(that: Any): Boolean = {
        that match {
            case that: MathOperation => this.equals(that)
            case _ => false
        }
    }

    private def equals(that: MathOperation): Boolean = {
        this.getClass == that.getClass &&
            this.getExpressions == that.getExpressions
    }

    def getSimplifiedExpressions(expressions: List[MathExpression]): List[MathExpression] = {
        expressions.map(expression => expression.simplify)
    }

    def operationSimplifyExpressions(expressions: List[MathExpression]): List[MathExpression] = {
        expressions.size match{
            case 0 => Nil
            case 1 => getSimplifiedExpressions(expressions)
            case _ => binarySimplifyExpressions(expressions)
        }
    }

    private def binarySimplifyExpressions(expressions: List[MathExpression]): List[MathExpression] = {
        val expression = expressionToSimplify(expressions.head, operationSimplifyExpressions(expressions.tail))
        if(!expression.isDefined) expressions.head :: operationSimplifyExpressions(expressions.tail)
        else expression.get :: operationSimplifyExpressions(expressions.slice(1, expressions.indexOf(expression)) :::
                                                             expressions.slice(expressions.indexOf(expression) + 1,
                                                         expressions.size))
    }

    private def expressionToSimplify(firstExpression: MathExpression, expressions: List[MathExpression]): Option[MathExpression] = {
        for(expression <- expressions){
            val potentialSimplify: Option[MathExpression] = binarySimplify(firstExpression, expression)
            if(potentialSimplify.isDefined) return potentialSimplify
        }
        None
    }
}
object MathOperation {
    def apply(s: String): Option[MathOperation] = {
        MathNegation(s) orElse MathOperation.binaryOperation(s) orElse MathOperation.unaryOperation(s) orElse
            MathNaturalLogarithm(s) orElse MathBase10Logarithm(s) orElse MathSquareRoot(s) orElse MathCubeRoot(s)
    }
    def binaryOperation(s: String): Option[MathOperation] = {
        findBinaryOperation(s, """[+-]""".r) orElse findBinaryOperation(s, """[*/]|\\div|\\times|\\cdot""".r) orElse findBinaryOperation(s, """[\^]""".r)
    }

    def findBinaryOperation(s: String, regex: Regex): Option[MathOperation] = {
        findBinaryOperation(s, regex, 0, allIndicesIn(s, regex))
    }

    def findBinaryOperation(s: String, regex: Regex, i: Int, indices: List[Int]): Option[MathOperation] = {
        if (i >= indices.length) {
            None
        } else {
            val splitOperation: (String, String) = s.splitAt(indices(i))
            (MathExpression(splitOperation._1), MathExpression(regex.replaceFirstIn(splitOperation._2, ""))) match {
                case (Some(leftExpr), Some(rightExpr)) => MathOperation(regex.findFirstIn(splitOperation._2).get, leftExpr, rightExpr)
                case _ => findBinaryOperation(s, regex, i+1, indices)
            }
        }
    }

    def unaryOperation(s: String): Option[MathOperation] = {
        findUnaryOperation(s, """\}\{""".r) orElse findUnaryOperation(s, """\]\{""".r)
    }

    def findUnaryOperation(s: String, regex: Regex): Option[MathOperation] = {
        findUnaryOperation(s, 0, allIndicesIn(s, regex).map(_ + 1))
    }

    def findUnaryOperation(s: String, index: Int, indices: List[Int]): Option[MathOperation] = {
        if (index >= indices.length) {
            None
        } else {
            val splitOperation: (String, String) = s.splitAt(indices(index))
            val leftExpr: Option[MathExpression] = MathExpression("""\\log_|\\sqrt""".r.replaceFirstIn(splitOperation._1, ""))
            val rightExpr: Option[MathExpression] = MathExpression(splitOperation._2)
            (leftExpr, rightExpr) match {
                case (Some(left), Some(right)) => MathOperation(splitOperation._1, left, right)
                case _ => findUnaryOperation(s, index + 1, indices)
            }
        }
    }

    //finds every index where the regex matches in the string
    def allIndicesIn(s: String, regex: Regex): List[Int] = {
        (s.length until 0 by -1).filter((i: Int) => regex.findPrefixMatchOf(s.substring(i)).isDefined).toList
    }

    def apply(operator: String, left: MathExpression, right: MathExpression): Option[MathOperation] = {
        operator match {
            case "+" => Some(MathSum(left, right))
            case "-" => Some(MathDifference(left, right))
            case str if (str == "\\div" || str == "/") => Some(MathQuotient(left, right))
            case str if (str == "*" || str == "\\times" || str == "\\cdot") => Some(MathProduct(left, right))
            case "^" => Some(MathExponentiation(left, right))
            case str if (str.startsWith("\\log_")) => Some(MathLogarithm(left, right))
            case str if (str.startsWith("\\sqrt")) => Some(MathRoot(left, right))
            case _ => None
        }
    }
}

class MathSum(expressions: List[MathExpression]) extends MathOperation(expressions) {
    override def getOperator: String = "+"
    override def getClassName: String = "MathSum"
    override def getPrecedence: Int = 0
    override def simplify = MathSum(this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions)))
    override def binarySimplify(left: MathExpression, right: MathExpression): Option[MathExpression] = {                          //TODO: decide how to simplify decimals with fractions
        (left, right) match{
            case (left: MathConstant, right: MathConstant) => Some(left + right)
            case _ => None
        }
    }
}

object MathSum {
    def apply(expressions: List[MathExpression]) = new MathSum(expressions)
    def apply(expressions: MathExpression*): MathSum = MathSum(List[MathExpression](expressions: _*))
}

class MathDifference(expressions: List[MathExpression]) extends MathOperation(expressions) {
    override def getOperator: String = "-"
    override def getClassName: String = "MathDifference"
    override def getPrecedence: Int = 1
    override def simplify = MathDifference(this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions)))
}
object MathDifference {
    def apply(expressions: List[MathExpression]) = new MathDifference(expressions)
    def apply(expressions: MathExpression*): MathDifference = MathDifference(List[MathExpression](expressions: _*))
}

class MathProduct(expressions: List[MathExpression]) extends MathOperation(expressions) {
    override def getOperator: String = "\\cdot"
    override def getClassName: String = "MathProduct"
    override def getPrecedence: Int = 2
    override def simplify = MathProduct(this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions)))
}
object MathProduct {
    def apply(expressions: List[MathExpression]) = new MathProduct(expressions)
    def apply(expressions: MathExpression*): MathProduct = MathProduct(List[MathExpression](expressions: _*))
}

class MathQuotient(expressions: List[MathExpression]) extends MathOperation(expressions) {
    override def getOperator: String = "\\div"
    override def getClassName: String = "MathQuotient"
    override def getPrecedence: Int = 2
    override def simplify = MathQuotient(this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions)))
}
object MathQuotient {
    def apply(expressions: List[MathExpression]) = new MathQuotient(expressions)
    def apply(expressions: MathExpression*): MathQuotient = MathQuotient(List[MathExpression](expressions: _*))
}

class MathExponentiation(expression: MathExpression, exponent: MathExpression) extends MathOperation(List[MathExpression](expression, exponent)) {
    def getExpression = super.getExpressions.head
    def getExponent = super.getExpressions.last

    override def simplify: MathExpression = new MathExponentiation(this.getExpression, this.getExponent)
    override def getPrecedence: Int = 5
    override def getOperator: String = "^"
    override def getClassName: String = "MathExponentiation"
    override def toLaTeX: String = this.expressionLaTeX(this.getExpression) + this.getOperator + "{" + this.getExponent.toLaTeX + "}"
}

object MathExponentiation {
    def apply(expression: MathExpression, exponent: MathExpression): MathExponentiation = new MathExponentiation(expression, exponent)
}

class MathLogarithm(val base: MathExpression, expression: MathExpression) extends MathOperation(List[MathExpression](base, expression)) {
    def getBase: MathExpression = super.getExpressions.head
    def getExpression: MathExpression = super.getExpressions.last
    override def getPrecedence: Int = 4
    override def toLaTeX: String = this.getOperator + "{" + this.expressionLaTeX(this.getExpression) + "}"
    override def getOperator: String = "\\log_{%s}".format(this.getBase.toLaTeX)
    override def getClassName: String = "MathLogarithm"
    override def simplify: MathExpression = new MathLogarithm(this.getBase, this.getExpression)
    override def description: String = "MathLogarithm(Base: %s, Expression: %s)".format(this.getBase.description, this.getExpression.description)
}

object MathLogarithm {
    def apply(base: MathExpression, expression: MathExpression): MathLogarithm = new MathLogarithm(base, expression)
    def apply(s: String): Option[MathLogarithm] = {
        MathNaturalLogarithm(s) orElse MathBase10Logarithm(s) orElse {
            val logRegex = new Regex("""^\\log_\{(.*)\}\{(.*)\}$""", "base", "expression")
            val splitLog = logRegex.findFirstMatchIn(s)
            if (splitLog.isEmpty) {
                None
            } else {
                (MathExpression(splitLog.get.group("base")), MathExpression(splitLog.get.group("expression"))) match {
                    case (Some(aBase), Some(anExpr)) => Some(MathLogarithm(aBase, anExpr))
                    case _ => None
                }
            }
        }
    }
}

class MathNaturalLogarithm(expression: MathExpression) extends MathLogarithm(new MathConstantE, expression) {
    override def getOperator: String = "\\ln"
    override def description: String = "MathNaturalLogarithm(%s)".format(this.getExpression.description)
}

object MathNaturalLogarithm {
    def apply(expression: MathExpression): MathNaturalLogarithm = new MathNaturalLogarithm(expression)
    def apply(s: String): Option[MathNaturalLogarithm] = {
        val lnRegex = new Regex("""^\\ln\{(.*)\}$""", "expression")
        val splitLn = lnRegex.findFirstMatchIn(s)
        if (splitLn.isEmpty) {
            None
        } else {
            MathExpression(splitLn.get.group("expression")) match {
                case Some(expr) => Some(MathNaturalLogarithm(expr))
                case _ => None
            }
        }
    }
}

class MathBase10Logarithm(expression: MathExpression) extends MathLogarithm(MathInteger(10), expression) {
    override def getOperator: String = "\\log"
    override def description: String = "MathBase10Logarithm(%s)".format(this.getExpression.description)
}

object MathBase10Logarithm {
    def apply(expression: MathExpression) = new MathBase10Logarithm(expression)
    def apply(s: String): Option[MathBase10Logarithm] = {
        val logRegex = new Regex("""^\\log\{(.*)\}$""", "expression")
        val splitLog = logRegex.findFirstMatchIn(s)
        if (splitLog.isEmpty) {
            None
        } else {
            MathExpression(splitLog.get.group("expression")) match {
                case Some(expr) => Some(MathBase10Logarithm(expr))
                case _ => None
            }
        }
    }
}

class MathRoot(val index: MathExpression, val radicand: MathExpression) extends MathExponentiation(radicand, MathQuotient(MathInteger(1), index)) {
    def getIndex: MathExpression = index
    def getRadicand: MathExpression = radicand
    override def getPrecedence: Int = 4
    override def toLaTeX = "\\sqrt[%s]{%s}".format(this.getIndex.toLaTeX, this.getRadicand.toLaTeX)
    override def getClassName: String = "MathRoot"
    override def description: String = this.getClassName + "(Index: %s, Radicand: %s)".format(this.getIndex.description, this.getRadicand.description)
}

object MathRoot {
    def apply(index: MathExpression, radicand: MathExpression) = {
        if (index == MathInteger(3)) {
            MathCubeRoot(radicand)
        } else if (index == MathInteger(2)) {
            MathSquareRoot(radicand)
        } else {
            new MathRoot(index, radicand)
        }
    }
    def apply(s: String): Option[MathRoot] = {
        MathSquareRoot(s) orElse MathCubeRoot(s) orElse {
            val rootRegex = new Regex("""^\\sqrt\[(.*)\]\{(.*)\}$""", "index", "radicand")
            val splitRoot = rootRegex.findFirstMatchIn(s)
            if (splitRoot.isEmpty) {
                None
            } else {
                (MathExpression(splitRoot.get.group("index")), MathExpression(splitRoot.get.group("radicand"))) match {
                    case (Some(index), Some(radicand)) => Some(MathRoot(index, radicand))
                    case _ => None
                }
            }
        }
    }
}

class MathSquareRoot(radicand: MathExpression) extends MathRoot(MathInteger(2), radicand) {
    override def toLaTeX: String = "\\sqrt{%s}".format(super.getRadicand.toLaTeX)
    override def getClassName: String = "MathSquareRoot"
    override def description: String = this.getClassName + "(" + this.getRadicand.description + ")"
}

object MathSquareRoot {
    def apply(radicand: MathExpression) = new MathSquareRoot(radicand)
    def apply(s: String): Option[MathSquareRoot] = {
        val sqrtRegex = new Regex("""^\\sqrt\{(.*)\}$""", "radicand")
        val splitSqrt = sqrtRegex.findFirstMatchIn(s)
        if (splitSqrt.isEmpty) {
            None
        } else {
            MathExpression(splitSqrt.get.group("radicand")) match {
                case Some(anExpr) => Some(MathSquareRoot(anExpr))
                case _ => None
            }
        }
    }
}

class MathCubeRoot(radicand: MathExpression) extends MathRoot(MathInteger(3), radicand) {
    override def toLaTeX: String = "\\sqrt[3]{%s}".format(super.getRadicand.toLaTeX)
    override def getClassName: String = "MathCubeRoot"
    override def description: String = this.getClassName + "(" + this.getRadicand.description + ")"
}

object MathCubeRoot {
    def apply(radicand: MathExpression) = new MathCubeRoot(radicand)
    def apply(s: String): Option[MathCubeRoot] = {
        val cubeRootRegex = new Regex("""^\\sqrt\[3\]\{(.*)\}$""", "radicand")
        val splitCubeRoot = cubeRootRegex.findFirstMatchIn(s)
        if (splitCubeRoot.isEmpty) {
            None
        } else {
            MathExpression(splitCubeRoot.get.group("radicand")) match {
                case Some(anExpr) => Some(MathCubeRoot(anExpr))
                case _ => None
            }
        }
    }
}

class MathNegation(expression: MathExpression) extends MathOperation(List[MathExpression](expression)) {
    def getExpression = expression
    override def getOperator: String = "-"
    override def getClassName: String = "MathNegation"
    override def getPrecedence: Int = MathNegation.getPrecedence
    override def simplify: MathExpression = new MathNegation(this.getExpression)
    override def toLaTeX: String = this.getOperator + this.expressionLaTeX(this.getExpression)
}

object MathNegation {
    def apply(expression: MathExpression) = new MathNegation(expression)
    def apply(s: String): Option[MathNegation] = {
        val negBasicRegex = new Regex("""^-(.*)$""", "expression")
        val negRegex = new Regex("""^-\((.*)\)$""", "expression")
        val splitBasicNeg = negBasicRegex.findFirstMatchIn(s)
        val splitNeg = negRegex.findFirstMatchIn(s)
        if (splitNeg.isDefined) {
            MathExpression(splitNeg.get.group("expression")) match {
                case Some(anExpr) => Some(MathNegation(anExpr))
                case _ => None
            }
        } else if (splitBasicNeg.isDefined) {
            MathExpression(splitBasicNeg.get.group("expression")) match {
                case Some(anExpr) if (anExpr.getPrecedence >= MathNegation.getPrecedence) => Some(MathNegation(anExpr))
                case _ => None
            }
        } else {
            None
        }
    }
    def getPrecedence: Int = 4
}
