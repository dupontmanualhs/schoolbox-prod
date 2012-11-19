package math

import scala.util.matching.Regex
import scala.collection.immutable.HashMap

abstract class Operation(expressions: List[Expression]) extends Expression {
    def getExpressions = expressions
    def getOperator: String
    def getClassName: String
    def binarySimplify(left: Expression, right: Expression): Option[Expression] = None
    override def description: String = {
        this.getExpressions.map(_.description).mkString(this.getClassName + "(", ", ", ")")
    }
    override def toLaTeX: String = {
        firstExpressionLaTeX(this.getExpressions.head) + this.getOperator + this.getExpressions.tail.map(expressionLaTeX(_)).mkString(this.getOperator)
    }
    def expressionLaTeX(expression: Expression): String = {
        if (expressionNeedsParentheses(this, expression)) {
            "(" + expression.toLaTeX + ")"
        } else {
            expression.toLaTeX
        }
    }

    def firstExpressionLaTeX(expression: Expression): String = {
        if (expression.simplePrecedence < this.simplePrecedence) {
            "(" + expression.toLaTeX + ")"
        } else {
            expression.toLaTeX
        }
    }

    def expressionNeedsParentheses(outsideExpression: Expression, expressionToTest: Expression): Boolean = {
        ((outsideExpression.isInstanceOf[Difference] || outsideExpression.isInstanceOf[Quotient]) && (expressionToTest.getPrecedence <= outsideExpression.getPrecedence || expressionToTest.isNegative)) ||
            (expressionToTest.getPrecedence < outsideExpression.getPrecedence) ||
            (expressionToTest.isInstanceOf[Negation]) ||
            (expressionToTest.isNegative && (outsideExpression.hasTwoSides || outsideExpression.isInstanceOf[Negation]))
    }

    override def equals(that: Any): Boolean = {
        that match {
            case that: Operation => this.equals(that)
            case _ => false
        }
    }

    private def equals(that: Operation): Boolean = {
        this.getClass == that.getClass &&
            this.getExpressions == that.getExpressions
    }

    def getSimplifiedExpressions(expressions: List[Expression]): List[Expression] = {
        expressions.map(expression => expression.simplify)
    }

    def operationSimplifyExpressions(expressions: List[Expression]): List[Expression] = {
        expressions.size match{
            case 0 => Nil
            case 1 => getSimplifiedExpressions(expressions)
            case _ => binarySimplifyExpressions(expressions)
        }
    }

    private def binarySimplifyExpressions(expressions: List[Expression]): List[Expression] = {
        val expression = expressionToSimplify(expressions.head, operationSimplifyExpressions(expressions.tail))
        if(!expression._1.isDefined) expressions.head :: operationSimplifyExpressions(expressions.tail)
        else expression._1.get :: operationSimplifyExpressions(expressions.tail.slice(0, expressions.tail.indexOf(expression._2)) :::
                                                            expressions.tail.slice(expressions.tail.indexOf(expression._2) + 1,
                                                            expressions.size))
    }

    private def expressionToSimplify(firstExpression: Expression, expressions: List[Expression]): (Option[Expression], Expression) = {
        for(expression <- expressions){
            val potentialSimplify: Option[Expression] = binarySimplify(firstExpression, expression)
            if(potentialSimplify.isDefined) return (potentialSimplify, expression)
        }
        (None, Integer(0))
    }
}
object Operation {
    def apply(s: String): Option[Operation] = {
        Negation(s) orElse Operation.binaryOperation(s) orElse Operation.unaryOperation(s) orElse
            NaturalLogarithm(s) orElse Base10Logarithm(s) orElse SquareRoot(s) orElse CubeRoot(s)
    }
    def binaryOperation(s: String): Option[Operation] = {
        findBinaryOperation(s, """[+-]""".r) orElse findBinaryOperation(s, """[*/]|\\div|\\times|\\cdot""".r) orElse findBinaryOperation(s, """[\^]""".r)
    }

    def findBinaryOperation(s: String, regex: Regex): Option[Operation] = {
        findBinaryOperation(s, regex, 0, allIndicesIn(s, regex))
    }

    def findBinaryOperation(s: String, regex: Regex, i: Int, indices: List[Int]): Option[Operation] = {
        if (i >= indices.length) {
            None
        } else {
            val splitOperation: (String, String) = s.splitAt(indices(i))
            (Expression(splitOperation._1), Expression(regex.replaceFirstIn(splitOperation._2, ""))) match {
                case (Some(leftExpr), Some(rightExpr)) => Operation(regex.findFirstIn(splitOperation._2).get, leftExpr, rightExpr)
                case _ => findBinaryOperation(s, regex, i+1, indices)
            }
        }
    }

    def unaryOperation(s: String): Option[Operation] = {
        findUnaryOperation(s, """\}\{""".r) orElse findUnaryOperation(s, """\]\{""".r)
    }

    def findUnaryOperation(s: String, regex: Regex): Option[Operation] = {
        findUnaryOperation(s, 0, allIndicesIn(s, regex).map(_ + 1))
    }

    def findUnaryOperation(s: String, index: Int, indices: List[Int]): Option[Operation] = {
        if (index >= indices.length) {
            None
        } else {
            val splitOperation: (String, String) = s.splitAt(indices(index))
            val leftExpr: Option[Expression] = Expression("""\\log_|\\sqrt""".r.replaceFirstIn(splitOperation._1, ""))
            val rightExpr: Option[Expression] = Expression(splitOperation._2)
            (leftExpr, rightExpr) match {
                case (Some(left), Some(right)) => Operation(splitOperation._1, left, right)
                case _ => findUnaryOperation(s, index + 1, indices)
            }
        }
    }

    //finds every index where the regex matches in the string
    def allIndicesIn(s: String, regex: Regex): List[Int] = {
        (s.length until 0 by -1).filter((i: Int) => regex.findPrefixMatchOf(s.substring(i)).isDefined).toList
    }

    def apply(operator: String, left: Expression, right: Expression): Option[Operation] = {
        operator match {
            case "+" => Some(Sum(left, right))
            case "-" => Some(Difference(left, right))
            case str if (str == "\\div" || str == "/") => Some(Quotient(left, right))
            case str if (str == "*" || str == "\\times" || str == "\\cdot") => Some(Product(left, right))
            case "^" => Some(Exponentiation(left, right))
            case str if (str.startsWith("\\log_")) => Some(Logarithm(left, right))
            case str if (str.startsWith("\\sqrt")) => Some(Root(left, right))
            case _ => None
        }
    }
}

class Sum(expressions: List[Expression]) extends Operation(expressions) {
    override def getOperator: String = "+"
    override def getClassName: String = "Sum"
    override def getPrecedence: Int = 0
    override def simplify: Expression = {
      val simplifiedExpressions = this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions))
      simplifiedExpressions.size match{
        case 0 => Integer(0)
        case 1 => simplifiedExpressions.head
        case _ => Sum(simplifiedExpressions)
      }
    }
    override def binarySimplify(left: Expression, right: Expression): Option[Expression] = {                          //TODO: decide how to simplify decimals with fractions
        (left, right) match{
            case (left: Constant, right: Constant) => Some(left + right)
            case _ => None
        }
    }
    override def evaluate(variables: HashMap[Expression, Value]): Expression = {
      Sum(this.expressions.map(n => Expression.checkVar(n, variables))).simplify
    }
}

object Sum {
    def apply(expressions: List[Expression]) = new Sum(expressions)
    def apply(expressions: Expression*): Sum = Sum(List[Expression](expressions: _*))
}

class Difference(expressions: List[Expression]) extends Operation(expressions) {
    override def getOperator: String = "-"
    override def getClassName: String = "Difference"
    override def getPrecedence: Int = 1
    override def simplify = Difference(this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions)))
    override def evaluate(variables: HashMap[Expression, Value]): Expression = {
      this
    }
}
object Difference {
    def apply(expressions: List[Expression]) = new Difference(expressions)
    def apply(expressions: Expression*): Difference = Difference(List[Expression](expressions: _*))
}

class Product(expressions: List[Expression]) extends Operation(expressions) {
    override def getOperator: String = "\\cdot"
    override def getClassName: String = "Product"
    override def getPrecedence: Int = 2
    override def simplify = Product(this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions)))
    override def evaluate(variables: HashMap[Expression, Value]): Expression = {
      this
    }
}
object Product {
    def apply(expressions: List[Expression]) = new Product(expressions)
    def apply(expressions: Expression*): Product = Product(List[Expression](expressions: _*))
}

class Quotient(expressions: List[Expression]) extends Operation(expressions) {
    override def getOperator: String = "\\div"
    override def getClassName: String = "Quotient"
    override def getPrecedence: Int = 2
    override def simplify = Quotient(this.operationSimplifyExpressions(getSimplifiedExpressions(getExpressions)))
    override def evaluate(variables: HashMap[Expression, Value]): Expression = {
      this
    }
}
object Quotient {
    def apply(expressions: List[Expression]) = new Quotient(expressions)
    def apply(expressions: Expression*): Quotient = Quotient(List[Expression](expressions: _*))
}

class Exponentiation(expression: Expression, exponent: Expression) extends Operation(List[Expression](expression, exponent)) {
    def getExpression = super.getExpressions.head
    def getExponent = super.getExpressions.last

    override def simplify: Expression = new Exponentiation(this.getExpression, this.getExponent)
    override def getPrecedence: Int = 5
    override def getOperator: String = "^"
    override def getClassName: String = "Exponentiation"
    override def toLaTeX: String = this.expressionLaTeX(this.getExpression) + this.getOperator + "{" + this.getExponent.toLaTeX + "}"
    override def evaluate(variables: HashMap[Expression, Value]): Expression = {
      this
    }
}

object Exponentiation {
    def apply(expression: Expression, exponent: Expression): Exponentiation = new Exponentiation(expression, exponent)
}

class Logarithm(val base: Expression, expression: Expression) extends Operation(List[Expression](base, expression)) {
    def getBase: Expression = super.getExpressions.head
    def getExpression: Expression = super.getExpressions.last
    override def getPrecedence: Int = 4
    override def toLaTeX: String = this.getOperator + "{" + this.expressionLaTeX(this.getExpression) + "}"
    override def getOperator: String = "\\log_{%s}".format(this.getBase.toLaTeX)
    override def getClassName: String = "Logarithm"
    override def simplify: Expression = new Logarithm(this.getBase, this.getExpression)
    override def description: String = "Logarithm(Base: %s, Expression: %s)".format(this.getBase.description, this.getExpression.description)
    override def evaluate(variables: HashMap[Expression, Value]): Expression = {
      this
    }
}

object Logarithm {
    def apply(base: Expression, expression: Expression): Logarithm = new Logarithm(base, expression)
    def apply(s: String): Option[Logarithm] = {
        NaturalLogarithm(s) orElse Base10Logarithm(s) orElse {
            val logRegex = new Regex("""^\\log_\{(.*)\}\{(.*)\}$""", "base", "expression")
            val splitLog = logRegex.findFirstMatchIn(s)
            if (splitLog.isEmpty) {
                None
            } else {
                (Expression(splitLog.get.group("base")), Expression(splitLog.get.group("expression"))) match {
                    case (Some(aBase), Some(anExpr)) => Some(Logarithm(aBase, anExpr))
                    case _ => None
                }
            }
        }
    }
}

class NaturalLogarithm(expression: Expression) extends Logarithm(new ConstantE, expression) {
    override def getOperator: String = "\\ln"
    override def description: String = "NaturalLogarithm(%s)".format(this.getExpression.description)
}

object NaturalLogarithm {
    def apply(expression: Expression): NaturalLogarithm = new NaturalLogarithm(expression)
    def apply(s: String): Option[NaturalLogarithm] = {
        val lnRegex = new Regex("""^\\ln\{(.*)\}$""", "expression")
        val splitLn = lnRegex.findFirstMatchIn(s)
        if (splitLn.isEmpty) {
            None
        } else {
            Expression(splitLn.get.group("expression")) match {
                case Some(expr) => Some(NaturalLogarithm(expr))
                case _ => None
            }
        }
    }
}

class Base10Logarithm(expression: Expression) extends Logarithm(Integer(10), expression) {
    override def getOperator: String = "\\log"
    override def description: String = "Base10Logarithm(%s)".format(this.getExpression.description)
}

object Base10Logarithm {
    def apply(expression: Expression) = new Base10Logarithm(expression)
    def apply(s: String): Option[Base10Logarithm] = {
        val logRegex = new Regex("""^\\log\{(.*)\}$""", "expression")
        val splitLog = logRegex.findFirstMatchIn(s)
        if (splitLog.isEmpty) {
            None
        } else {
            Expression(splitLog.get.group("expression")) match {
                case Some(expr) => Some(Base10Logarithm(expr))
                case _ => None
            }
        }
    }
}

class Root(val index: Expression, val radicand: Expression) extends Exponentiation(radicand, Quotient(Integer(1), index)) {
    def getIndex: Expression = index
    def getRadicand: Expression = radicand
    override def getPrecedence: Int = 4
    override def toLaTeX = "\\sqrt[%s]{%s}".format(this.getIndex.toLaTeX, this.getRadicand.toLaTeX)
    override def getClassName: String = "Root"
    override def description: String = this.getClassName + "(Index: %s, Radicand: %s)".format(this.getIndex.description, this.getRadicand.description)
}

object Root {
    def apply(index: Expression, radicand: Expression) = {
        if (index == Integer(3)) {
            CubeRoot(radicand)
        } else if (index == Integer(2)) {
            SquareRoot(radicand)
        } else {
            new Root(index, radicand)
        }
    }
    def apply(s: String): Option[Root] = {
        SquareRoot(s) orElse CubeRoot(s) orElse {
            val rootRegex = new Regex("""^\\sqrt\[(.*)\]\{(.*)\}$""", "index", "radicand")
            val splitRoot = rootRegex.findFirstMatchIn(s)
            if (splitRoot.isEmpty) {
                None
            } else {
                (Expression(splitRoot.get.group("index")), Expression(splitRoot.get.group("radicand"))) match {
                    case (Some(index), Some(radicand)) => Some(Root(index, radicand))
                    case _ => None
                }
            }
        }
    }
}

class SquareRoot(radicand: Expression) extends Root(Integer(2), radicand) {
    override def toLaTeX: String = "\\sqrt{%s}".format(super.getRadicand.toLaTeX)
    override def getClassName: String = "SquareRoot"
    override def description: String = this.getClassName + "(" + this.getRadicand.description + ")"
}

object SquareRoot {
    def apply(radicand: Expression) = new SquareRoot(radicand)
    def apply(s: String): Option[SquareRoot] = {
        val sqrtRegex = new Regex("""^\\sqrt\{(.*)\}$""", "radicand")
        val splitSqrt = sqrtRegex.findFirstMatchIn(s)
        if (splitSqrt.isEmpty) {
            None
        } else {
            Expression(splitSqrt.get.group("radicand")) match {
                case Some(anExpr) => Some(SquareRoot(anExpr))
                case _ => None
            }
        }
    }
}

class CubeRoot(radicand: Expression) extends Root(Integer(3), radicand) {
    override def toLaTeX: String = "\\sqrt[3]{%s}".format(super.getRadicand.toLaTeX)
    override def getClassName: String = "CubeRoot"
    override def description: String = this.getClassName + "(" + this.getRadicand.description + ")"
}

object CubeRoot {
    def apply(radicand: Expression) = new CubeRoot(radicand)
    def apply(s: String): Option[CubeRoot] = {
        val cubeRootRegex = new Regex("""^\\sqrt\[3\]\{(.*)\}$""", "radicand")
        val splitCubeRoot = cubeRootRegex.findFirstMatchIn(s)
        if (splitCubeRoot.isEmpty) {
            None
        } else {
            Expression(splitCubeRoot.get.group("radicand")) match {
                case Some(anExpr) => Some(CubeRoot(anExpr))
                case _ => None
            }
        }
    }
}

class Negation(expression: Expression) extends Operation(List[Expression](expression)) {
    def getExpression = expression
    override def getOperator: String = "-"
    override def getClassName: String = "Negation"
    override def getPrecedence: Int = Negation.getPrecedence
    override def simplify: Expression = new Negation(this.getExpression)
    override def toLaTeX: String = this.getOperator + this.expressionLaTeX(this.getExpression)
    override def evaluate(variables: HashMap[Expression, Value]): Expression = {
      this
    }
}

object Negation {
    def apply(expression: Expression) = new Negation(expression)
    def apply(s: String): Option[Negation] = {
        val negBasicRegex = new Regex("""^-(.*)$""", "expression")
        val negRegex = new Regex("""^-\((.*)\)$""", "expression")
        val splitBasicNeg = negBasicRegex.findFirstMatchIn(s)
        val splitNeg = negRegex.findFirstMatchIn(s)
        if (splitNeg.isDefined) {
            Expression(splitNeg.get.group("expression")) match {
                case Some(anExpr) => Some(Negation(anExpr))
                case _ => None
            }
        } else if (splitBasicNeg.isDefined) {
            Expression(splitBasicNeg.get.group("expression")) match {
                case Some(anExpr) if (anExpr.getPrecedence >= Negation.getPrecedence) => Some(Negation(anExpr))
                case _ => None
            }
        } else {
            None
        }
    }
    def getPrecedence: Int = 4
}
