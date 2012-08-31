package models.mastery

import models.mastery._
import scala.util.Random
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

@PersistenceCapable(detachable = "true")
class QuizSection { //a section is a part of the quiz where students would be doing one sort of problem, so you could have a "simplifing exponents" section, and a "fill in the blank" section for a quiz
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
  
  override def toString = { "name:\n" + _name + "\ninstructions\n" + _instructions + "\nquestions\n" + _questions }
}

trait QQuizSection extends PersistableExpression[QuizSection] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_name")
  def name: ObjectExpression[String] = _name
  
  private[this] lazy val _instructions: ObjectExpression[String] = new ObjectExpressionImpl[String](this, "_instructions")
  def instructions: ObjectExpression[String] = _instructions
  
  private[this] lazy val _questions: ObjectExpression[List[QuestionSet]] = new ObjectExpressionImpl[List[QuestionSet]](this, "_questions")
  def questions: ObjectExpression[List[QuestionSet]] = _questions
}

object QQuizSection {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QQuizSection = {
    new PersistableExpressionImpl[QuizSection](parent, name) with QQuizSection
  }

  def apply(cls: Class[QuizSection], name: String, exprType: ExpressionType): QQuizSection = {
    new PersistableExpressionImpl[QuizSection](cls, name, exprType) with QQuizSection
  }

  private[this] lazy val jdoCandidate: QQuizSection = candidate("this")

  def candidate(name: String): QQuizSection = QQuizSection(null, name, 5)

  def candidate(): QQuizSection = jdoCandidate

  def parameter(name: String): QQuizSection = QQuizSection(classOf[QuizSection], name, ExpressionType.PARAMETER)

  def variable(name: String): QQuizSection = QQuizSection(classOf[QuizSection], name, ExpressionType.VARIABLE)
}