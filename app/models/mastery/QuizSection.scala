package models.mastery

import models.mastery._
import scala.util.Random
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import scala.collection.JavaConverters._
import config.users.UsesDataStore

@PersistenceCapable(detachable = "true")
class QuizSection { //a section is a part of the quiz where students would be doing one sort of problem, so you could have a "simplifing exponents" section, and a "fill in the blank" section for a quiz
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _ //DB's id
  def id: Long = _id

  private[this] var _name: String = _ //name of section (i.e. foiling, factoring)
  def name = _name
  def name_=(theName: String) { _name = theName }
  
  private[this] var _instructions: String = _ //instructions for the section (i.e. simplify the following)
  def instructions = _instructions
  def instructions_=(theInstructions: String) { _instructions = theInstructions }
  
  @Persistent
  @Element(types=Array(classOf[QuestionSet]))
  @Join
  private[this] var _questionSets: java.util.List[QuestionSet] = _ //a list of the Questions/QuestionSets that make up a section. this.size = numOfQuestionsInSection
  def questionSets: List[QuestionSet] = _questionSets.asScala.toList
  def questionSets_=(theQuestionSets: List[QuestionSet]) { _questionSets = theQuestionSets.asJava }
  
  def this(theName: String, theInstructions: String, theQuestionSets: List[QuestionSet]) = {
    this()
    name_=(theName)
    instructions_=(theInstructions)
    questionSets_=(theQuestionSets)
  }

  // TODO: does this sometimes get the same question more than once?
  def randomQuestions: List[Question] = {
    Random.shuffle(questionSets.map((qs: QuestionSet) => qs(Random.nextInt(qs.size))))
  }

  override def toString = { _instructions }
}

object QuizSection extends UsesDataStore {
  def getById(id: Long): Option[QuizSection] = {
    dataStore.pm.query[QuizSection].filter(QQuizSection.candidate.id.eq(id)).executeOption()
  }
}

trait QQuizSection extends PersistableExpression[QuizSection] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _name: StringExpression = new StringExpressionImpl(this, "_name")
  def name: StringExpression = _name
  
  private[this] lazy val _instructions: StringExpression = new StringExpressionImpl(this, "_instructions")
  def instructions: StringExpression = _instructions
  
  private[this] lazy val _questionSets: ListExpression[java.util.List[QuestionSet], QuestionSet] = new ListExpressionImpl[java.util.List[QuestionSet], QuestionSet](this, "_questionSets")
  def questions: ListExpression[java.util.List[QuestionSet], QuestionSet] = _questionSets
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