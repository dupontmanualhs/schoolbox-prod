package models.mastery

import models.mastery._
import scala.util.Random
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

@PersistenceCapable(detachable = "true")
class Section { //a section is a part of the quiz where students would be doing one sort of problem, so you could have a "simplifing exponents" section, and a "fill in the blank" section for a quiz
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _ //DB's id
  private[this] var _name: String = _ //name of section (i.e. foiling, factoring)
  private[this] var _instructions: String = _ //instructions for the section (i.e. simplify the following)
  private[this] var _questions: List[QuestionSet] = _ //a list of the Questions/QuestionSets that make up a section. this.size = numOfQuestionsInSection

  def this(name: String, instructions: String, questions: List[QuestionSet]) = {
    this()
    _name=name
    _instructions=instructions
    _questions=questions
  }
  
  def questionSet = {_questions}
  
  def randomQuestions = {
    var ListOfQuestions = List[Question]()
    val rand = new Random()
    for(qs <- _questions){
      ListOfQuestions = qs.get(rand.nextInt(qs.size+1)) :: ListOfQuestions
    }
    val finL = ListOfQuestions
    finL
  }
  
  override def toString = { _name + "\n" + _instructions }
}

trait QSection extends PersistableExpression[Section] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_name")
  def name: ObjectExpression[String] = _name
  
  private[this] lazy val _instructions: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_instructions")
  def instructions: ObjectExpression[String] = _instructions
  
  private[this] lazy val _questions: ObjectExpression[List[QuestionSet]] = new ObjectExpressionImpl[List[QuestionSet]](this, "_questions")
  def questions: ObjectExpression[List[QuestionSet]] = _questions
}

object QSection {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QSection = {
    new PersistableExpressionImpl[Section](parent, name) with QSection
  }

  def apply(cls: Class[Section], name: String, exprType: ExpressionType): QSection = {
    new PersistableExpressionImpl[Section](cls, name, exprType) with QSection
  }

  private[this] lazy val jdoCandidate: QSection = candidate("this")

  def candidate(name: String): QSection = QSection(null, name, 5)

  def candidate(): QSection = jdoCandidate

  def parameter(name: String): QSection = QSection(classOf[Section], name, ExpressionType.PARAMETER)

  def variable(name: String): QSection = QSection(classOf[Section], name, ExpressionType.VARIABLE)
}