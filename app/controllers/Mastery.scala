package controllers

import scala.xml.Elem
import java.util.UUID
import scala.xml.MetaData
import scala.xml.UnprefixedAttribute
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Null
import scala.xml.transform.BasicTransformer
import forms.Binding
import forms.Form
import forms.InvalidBinding
import forms.ValidBinding
import forms.fields.Field
import forms.fields.TextField
import forms.widgets.{ TextInput, Widget }
import javax.jdo.annotations.PersistenceCapable
import models.mastery.QQuiz
import models.mastery.Question
import models.mastery.QuestionSet
import models.mastery.Quiz
import models.mastery.QuizSection
import play.api.mvc.Controller
import play.api.mvc.PlainResult
import util.Helpers.string2elem
import views.html
import forms.validators.ValidationError
import scala.xml.UnprefixedAttribute
import scala.xml.Text
import util.VisitAction
import scalajdo.DataStore
import models.users.Visit

class BlanksField(question: Question) extends Field[String](question.id.toString) {
  override def widget = new MultiBlankWidget(question.text)

  def asValue(strs: Seq[String]): Either[ValidationError, String] = Right(strs.mkString(", "))
}

class AnswerField(question: Question) extends TextField(question.id.toString) {
  val uuid=java.util.UUID.randomUUID()
  override def widget = new MathWidget(question.text, uuid=uuid)

  override def asValue(s: Seq[String]): Either[ValidationError, String] = s match {
    case Seq(ans) => Right(ans)
    case _ => Right("")
  }
}

class MathWidget(text: String, attrs: MetaData = Null, uuid: java.util.UUID) extends TextInput(false, attrs, "text") {
  val Name:String = uuid.toString()
  override def render(name: String, value: Seq[String], attrList: MetaData = Null): NodeSeq = {
    <span>{ text } { super.render(name, value, attrList.append(new UnprefixedAttribute("onkeyup", "UpdateMath(this.value)", Null))) }
    <div id="MathOutput">
    	You typed: ${{}}$
    </div>
</span>
  }
  override def scripts: NodeSeq = 
    <script type="text/x-mathjax-config">
	  MathJax.Hub.Config({{
		  tex2jax: {{
      inlineMath: [['$','$'],['\\(','\\)']]
		  }}
		  }});
	</script>

	<script type="text/javascript"
		  src="http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS_HTML-full">
	</script>

	<script>
	(function () {{
    var QUEUE = MathJax.Hub.queue; 
    var math = null;                
    
    QUEUE.Push(function () {{
      math = MathJax.Hub.getAllJax('MathOutput')[0];
    }});

    window.UpdateMath = function (TeX) {{
      QUEUE.Push(['Text',math,'\\displaystyle{{'+TeX+'}}']);
    }}
  }})();
</script>
}

class MultiBlankWidget(text: String, attrs: MetaData = Null) extends Widget(false, attrs) {
  val blank = "_{3,}".r

  lazy val numBlanks = blank.findAllIn(text).length
  lazy val qText: Node = {
    val withBlanks = blank.replaceAllIn(text, "<blank/>")
    string2elem("<span>" + withBlanks + "</span>")
  }

  def render(name: String, value: Seq[String], attrList: MetaData = Null): NodeSeq = {
    def transform(n: Node, inputs: Iterator[Node]): Node = n match {
      case <blank/> => inputs.next
      case e: Elem => e.copy(child = e.child.map(transform(_, inputs)))
      case _ => n
    }
    val inputs = (0 until numBlanks).map((i: Int) => {
      val valueAttr = if (i < value.length) new UnprefixedAttribute("value", Text(value(i)), Null) else Null
      <input type="text" name={ "%s[%d]".format(name, i) }/> % attrList % valueAttr
    })
    transform(qText, inputs.iterator)
  }

  override def valueFromDatadict(data: Map[String, Seq[String]], name: String): Seq[String] = {
    (for (i <- 0 until numBlanks) yield data.get("%s[%d]".format(name, i)).map(_.mkString).getOrElse(""))
  }
}

class MasteryForm(sectionsWithQuestions: List[(QuizSection, List[Question])]) extends Form {
  val sectionInstructionList: List[String] =
    sectionsWithQuestions.map((sq: (QuizSection, List[Question])) => sq._1.instructions)

  val instructionsAndFields: List[(String, List[forms.fields.Field[_]])] = {
    sectionsWithQuestions.map((sq: (QuizSection, List[Question])) => {
      (sq._1.instructions, sq._2.map((q: Question) => {
        if (q.text.contains("___")) {
          new BlanksField(q)
        } else {
          new AnswerField(q)
        }
      }))
    })
  }

  val fields: List[forms.fields.Field[_]] = {
    instructionsAndFields.flatMap(_._2)
  }

  override def render(bound: Binding, action: Option[String] = None, legend: Option[String] = None): Elem = {
    <form method={ method } action={ action.map(Text(_)) } autocomplete="off">
      <table class="table">
        { if (bound.formErrors.isEmpty) NodeSeq.Empty else <tr><td></td><td>{ bound.formErrors.render }</td><td></td></tr> }
        {
          instructionsAndFields.flatMap(instrPlusFields => {
            val instructions: String = instrPlusFields._1
            val fields: List[forms.fields.Field[_]] = instrPlusFields._2
            //TODO: Make it so the strings in the list "sectionInstructionList" appear
            <tr>
              <td></td>
              <td><b>{ instructions }</b></td>
              <td></td>
            </tr> ++
              fields.zip(1 to fields.length).flatMap {
                case (f, num) => {
                  val name = f.name
                  val errorList = bound.fieldErrors.get(name).map(_.render)
                  <tr>
                    <td><label>{ "%d. ".format(num) }</label></td>
                    <td>{ f.asWidget(bound) }</td>
                    {
                      if (bound.hasErrors) <td>{ errorList.getOrElse(NodeSeq.Empty) }</td>
                      else NodeSeq.Empty
                    }
                  </tr>
                }
              }
          }).toList
        }
      </table>
      <input type="submit"/>
    </form>
  }
}

object Mastery extends Controller {

  def menuOfTests() = VisitAction { implicit req =>
    DataStore.execute { pm =>
      val cand = QQuiz.candidate()
      val listOfMasteries = pm.query[Quiz].orderBy(cand.name.asc).executeList()
      val hasQuizzes = listOfMasteries.size != 0
      val table: List[NodeSeq] = listOfMasteries.map { q =>
        <tr>
          <td>{ linkToQuiz(q) }</td>
        </tr>
      }

      Ok(html.tatro.mastery.MasteryQuizMenu(table, hasQuizzes)) // this is a fake error -.-
    }
  }

  def linkToQuiz(quiz: Quiz): NodeSeq = {
    val link = controllers.routes.Mastery.getDisplayQuiz(quiz.id)
    <a href={ link.url }>{ quiz.toString }</a>
  }

  def getDisplayQuiz(quizId: Long) = displayQuiz(Quiz.getById(quizId))

  def displayQuiz(maybeQuiz: Option[Quiz]) = VisitAction { implicit request =>
    DataStore.execute { pm =>
      val visit = Visit.getFromRequest(request)
      maybeQuiz match {
        case None => NotFound(views.html.notFound("The quiz of which you are seeking no longer exists."))
        case Some(quiz) => {
          val sections: List[QuizSection] = quiz.sections
          if (sections == null || sections.isEmpty) {
            NotFound(views.html.notFound("There are no sections :("))
          } else {
            if (request.method == "GET") {
              val sectionsWithQuestions: List[(QuizSection, List[Question])] =
                quiz.sections.map(s => (s, s.randomQuestions))
              //MasteryForm uses sectionsWithQuestions
              val form = new MasteryForm(sectionsWithQuestions)
              val idsOfSectionsWithQuestions: List[(Long, List[Long])] =
                sectionsWithQuestions.map((sq: (QuizSection, List[Question])) => {
                  (sq._1.id, sq._2.map(_.id))
                })
              visit.set("quizId", quiz.id)
              visit.set("sectionWithQuestionsId", idsOfSectionsWithQuestions)
              pm.makePersistent(visit)
              Ok(html.tatro.mastery.displayMastery(quiz, Binding(form)))
            } else {
              val idsOfSectionsWithQuestions = visit.getAs[List[(Long, List[Long])]]("sectionWithQuestionsId").get
              val sectionsWithQuestions = idsOfSectionsWithQuestions.map((sq: (Long, List[Long])) => {
                (QuizSection.getById(sq._1).get, sq._2.map(Question.getById(_).get))
              })
              val form = new MasteryForm(sectionsWithQuestions)
              Binding(form, request) match {
                case ib: InvalidBinding => Ok(html.tatro.mastery.displayMastery(quiz, ib)) // there were errors
                case vb: ValidBinding => {
                  visit.set("answers", form.fields.map(vb.valueOf(_)))
                  pm.makePersistent(visit)
                  Redirect(routes.Mastery.checkAnswers())
                }
              }
            }
          }
        }
      }
    }
  }

  def testDataBase() = VisitAction { implicit req =>
    DataStore.execute { pm =>
      val quizCand = QQuiz.candidate()
      val listOfMasteries = pm.query[Quiz].orderBy(quizCand.name.asc).executeList()
      val listOfSections = pm.query[models.mastery.QuizSection].executeList()
      val listOfQSets = pm.query[QuestionSet].executeList()
      val listOfQuestions = pm.query[Question].executeList()
      Ok(html.tatro.mastery.testData(listOfMasteries, listOfSections, listOfQSets, listOfQuestions))
    }
  }

  def checkAnswers() = VisitAction { implicit request =>
    val visit = Visit.getFromRequest(request)
    val quiz = Quiz.getById(visit.getAs[Long]("quizId").get).get
    val idsOfSectionsWithQuestions = visit.getAs[List[(Long, List[Long])]]("sectionWithQuestionsId").get
    val sectionsWithQuestions = idsOfSectionsWithQuestions.map((sq: (Long, List[Long])) => {
      (QuizSection.getById(sq._1).get, sq._2.map(Question.getById(_).get))
    })
    val answerList = visit.getAs[List[String]]("answers").get
    val qsAndAs = sectionsWithQuestions.flatMap((sq: (QuizSection, List[Question])) => sq._2).zip(answerList)
    val totalPointsPossible: Int = qsAndAs.map(qa => qa._1.value).reduce((x, y) => x + y)
    val numCorrect = totalPointsPossible - (0 + qsAndAs.map(qa => if (qa._1.answer.contains(removeMult(removeSpaces(qa._2)))) qa._1.value else 0).reduce((x, y) => x + y))
    //green:  #347235
    //green2: rgb(109,245,140)
    //red:    #A52A2A
    //red2:   #E50A1D
    val table: List[NodeSeq] = qsAndAs.map(qa =>
      if (qa._1.answer.contains(removeMult(removeSpaces(qa._2)))) {
        <tr bgcolor="#5EFB6E">
          <td>{ qa._1.text }</td>
          <td>{ qa._2 }</td>
          <td>{ "correct" }</td>
          <td>{ qa._1.value + "/" + qa._1.value }</td>
        </tr>
      } else {
        <tr bgcolor="#F9966B">
          <td>{ qa._1.text }</td>
          <td>{ qa._2 }</td>
          <td>{ "wrong" }</td>
          <td>{ "0/" + qa._1.value }</td>
        </tr>
      })
    Ok(html.tatro.mastery.displayScore(quiz, totalPointsPossible, numCorrect, table))
  }
  def removeSpaces(s: String) = {
    """ """.r.replaceAllIn(s, "")
  }

  def removeMult(s: String) = {
    """\*""".r.replaceAllIn(s, "")
  }

  def replaceMultiplicationSignsWithDots(s: String) = {
    """\*""".r.replaceAllIn(s, "\\cdot ")
  }
  /*
  //def radToSqrt(s: String) = """rad""".r.replaceAllIn(s, "sqrt")

  //def addMultiplication(s: String) = {
  //  var ns = ""
  //  for (i <- 1 to s.length - 1) {
  //    val c = s.charAt(i)
  //    val pc = s.charAt(i - 1)
  //    if ((c.isLetter && (pc.isLetter || pc.isDigit || pc == ')')) || (c.isDigit && (pc.isLetter || pc == ')')) || (c == '(' && (pc.isLetter || pc.isDigit || pc == ')'))) {
  //      ns = ns + pc + "*"
  //    } else {
  //      ns = ns + pc
  //    }
  //    if (i == s.length - 1) {
  //      ns = ns + c
  //    }
  //  }
  //  ns
  //}



  def changeToInterpreterSyntax(s: String) = {
    var rs = getRidOfSpaces(s)
    rs = encloseExponents(rs)
    rs = changeRadToR(rs)
    rs = getRidOfExtraMultiplication(rs)
    rs
  }
  private[this] var mapIndex: Int = _
  def reorderAnswer(s: String) = { //TODO: this needs some work dealing with parens... I think
    mapIndex = 0
    mapOfreplacements = Map(1.toChar + "" -> 1.toChar)
    var ParentParen = List[String]()
    if (hasPerfectParens(s)) {
      ParentParen = getOnlyThingsInParens(s)
      if (!(ParentParen == Null))
        for (p <- ParentParen) {
          mapParenGroupsToChars(p)
        }
    }
    val ns = replaceParensWithChars(s) // i.e. (x+2) -> a and r(x+3) -> rb
    System.out.println("replace Parens With Chars: \n" + ns)
    val ns2 = replaceRwithChar(ns)
    //TODO: this needs work i.e. rb -> c
    System.out.println("replace r with Chars: \n"+ns2)
    val ns3 = replaceExponentsWithChar(ns2)
    //TODO: this needs work i.e. d^f -> e
    System.out.println("replace Exponents With Chars: \n"+ns3)
    val ns4 = reorderExpression(ns3)
    System.out.println("reorder Expression: \n"+ns4)
    System.out.println("map of substitutions before reorder: \n" + mapOfreplacements)
    mapOfreplacements.keys.foreach{ k=>
    TODO: Fix This
        val split = k.split(])|(?=[()toList
        var remadeKey = ""
        for(q <- split){
          remadeKey = remadeKey + reorderMultiplication(q)
        }
        remadeKey = reorderAddMinus(remadeKey)
        mapOfreplacements += (remadeKey -> mapOfreplacements(k))
        mapOfreplacements -= (k)
    }
    System.out.println("map of substitutions after reorder: \n"+mapOfreplacements)
    val ns5 = replaceCharsWithStr(ns4)
    System.out.println("substitute parens back in: \n"+ns5)
    System.out.println()
    System.out.println()
    System.out.println()
    ns5
  }

  def replaceCharsWithStr(s: String): String = {
    var ns = s
    var tf = true
    while(tf) {
      tf = false
      mapOfreplacements.keys.foreach { str =>
        if (ns.indexOf(mapOfreplacements(str)) != -1) {
          ns = ns.replace(mapOfreplacements(str).toString, str)
          tf = true
        }
      }
    }
    ns
  }

  def replaceExponentsWithChar(s: String) = {
    var ns = s
    for (n <- 1 to s.length - 2) {
      val c = s.charAt(n)
      val nc = s.charAt(n + 1)
      val pc = s.charAt(n - 1)
      if (c == '^') {
        mapOfreplacements += (pc.toString + c.toString + nc.toString -> (mapIndex + 1).toChar)
        mapIndex = mapIndex + 1
        ns.replace(pc.toString + c.toString + nc.toString, mapIndex.toChar.toString)
      }
    }
    ns
  }


  def reorderExpression(s: String) = {
    val splitOnOperands = s.split("((").toList
    var correctMultOrder = ""
    for (s1 <- splitOnOperands) {
      correctMultOrder = correctMultOrder + reorderMultiplication(s1)
    }
    val correctOrder = reorderAddMinus(correctMultOrder)
    correctOrder
    }

  def reorderAddMinus(s: String) = {
    val tempList = s.split("(?=[+\\-])").toList
    var tempList2 = s.split("[+\\-]").toList
    tempList2=tempList2.sorted
    var tempList3 = List[String]()
    for(num <- 0 to tempList2.size-1){
      val str = tempList2.apply(num)
      var opperator = ""
      for(str2 <- tempList){
        if(str2.contains(str) && (str2.contains("+") || str2.contains("-"))){
          opperator = str2.charAt(0).toString
        } else {
          opperator = "+"
        }
      }
      tempList3 = opperator+str :: tempList3
    }
    val str = tempList3.mkString
    val rstr = str.substring(1)
    rstr
  }

  def replaceRwithChar(s: String) = {
    var ns = s
    for (n <- 0 to s.length - 2) {
      val c = s.charAt(n)
      val nc = s.charAt(n + 1)
      if (c == 'r') {
        mapOfreplacements += (c.toString + nc.toString -> (mapIndex + 1).toChar)
        mapIndex = mapIndex + 1
        ns.replace(c.toString + nc.toString, mapIndex.toChar.toString)
      }
    }
    ns
  }

  def reorderMultiplication(s: String) = {
    if (!(s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("(") || s.equals(")"))) {
      var ns = s
      var eachElement = List[String]()
      for (n <- 0 to s.length - 1) {
        if (ns.length != 0) {
          val c = ns.charAt(0)
          if (!c.isDigit) {
            eachElement = c.toString :: eachElement
            ns = ns.substring(1)
          } else {
            var endNumberIndex = 0
            var notFoundEnd = true
            for (i <- 0 to ns.length - 1) {
              val cc = ns.charAt(i)
              if ((!cc.isDigit) && notFoundEnd) {
                endNumberIndex = i - 1
              } else if (i == ns.length - 1) {
                endNumberIndex = i
              }
            }
            val str = ns.substring(0, endNumberIndex + 1)
            eachElement = str :: eachElement
            ns = ns.substring(endNumberIndex + 1)
          }
        }
      }
      eachElement = eachElement.sorted
      val str = eachElement.mkString
      str
    } else s
  }

  object Integer {
    def unapply(s: String) : Option[Int] = try {
      Some(s.toInt)
    } catch {
      case _ : java.lang.NumberFormatException => None
    }
  }

  def isNum(str: String): Boolean = str match {
    case Integer(x) => true
    case _ => false
  }

  def remove[T](elem: T, list: List[T]) = list diff List(elem)

  def replaceParensWithChars(s: String) = {
    var ns = s
    var x = 2
    var tf = true
    for (i <- 1 to mapOfreplacements.size + 10) {
      tf = false
      mapOfreplacements.keys.foreach { str =>
        if (ns.indexOf(str) != -1) {
          ns = ns.replace(ns.substring(ns.indexOf(str), ns.indexOf(str) + (str.length)), mapOfreplacements(str).toString)
          tf = true
        }
      }
    }
    ns
  }

  def splitKeepingFirst(s: String, c: Char) = { //splits into a List that keeps c on the front of each element
    val newList1 = s.split(c).toList
    var newList2 = List[String]()
    newList2 = newList1.apply(0) :: newList2
    for (n <- 1 to newList1.size - 1) {
      val p = newList1.apply(n)
      newList2 = c + p :: newList2
    }
    newList2 = newList2.reverse
    newList2
  }

  def splitKeepingLast(s: String, c: Char) = { //splits into a List that keeps c on the end of each previous element
    val newList1 = s.split(c).toList
    var newList2 = List[String]()
    for (n <- 0 to newList1.size - 2) {
      val p = newList1.apply(n)
      newList2 = p + c :: newList2
    }
    newList2 = newList1.apply(newList1.size - 1) :: newList2
    newList2 = newList2.reverse
    newList2
  }

  private[this] var mapOfreplacements: Map[String, Char] = _

  def hasPerfectParens(s: String) = { //tells if the String expression has perfect parentheses
    var tf = true
    var inParens = 0
    if (s.length <= 1) {
      tf = false
    }
    for (c <- s) {
      if (c == '(') {
        inParens = inParens + 1
      }
      if (c == ')') {
        inParens = inParens - 1
      }
      if (inParens < 0) {
        tf = false
      }
    }
    if (inParens != 0) {
      tf = false
    }
    tf
  }

  def getOnlyThingsInParens(s: String) = { //returns a list of the Parent Parentheses Groups
    var rString1 = ""
    var inParens = 0
    for (c <- s) {
      if (c == '(') {
        inParens = inParens + 1
      }
      if (inParens != 0) {
        rString1 = rString1 + c
      }
      if (c == ')') {
        inParens = inParens - 1
      }
    }
    val ListOfParentParenGroups = splitParentParens(rString1)
    ListOfParentParenGroups.reverse
  }

  def splitParentParens(s: String) = {
    var inParens = 0
    var listOfString = List[String]()
    var start = 0
    for (n <- 0 to s.length() - 1) {
      val c = s.charAt(n)
      if (c == '(') {
        inParens = inParens + 1
        if (inParens == 1) {
          start = n
        }
      }
      if (c == ')') {
        inParens = inParens - 1
        if (inParens == 0) {
          listOfString = s.substring(start, n + 1) :: listOfString
        }
      }
    }
    listOfString
  }

  def mapParenGroupsToChars(s: String) = {
    mapOfreplacements = mapOfreplacements ++ mapParentheses(s)
    mapIndex = mapIndex + mapOfreplacements.size
  }

  def str2parens(s: String): (Parens, String) = {
    def fail = throw new Exception("Wait, this system was made to be unflawed!!! How did you... Just HOW!?")
    if (s(0) != '(') fail
    def parts(s: String, found: Seq[Part] = Vector.empty): (Seq[Part], String) = {
      if (s(0) == ')') (found, s)
      else if (s(0) == '(') {
        val (p, s2) = str2parens(s)
        parts(s2, found :+ p)
      } else {
        val (tx, s2) = s.span(c => c != '(' && c != ')')
        parts(s2, found :+ new TextPart(tx))
      }
    }
    val (inside, more) = parts(s.tail)
    if (more(0) != ')') fail
    (new Parens(inside), more.tail)
  }

  def findParens(p: Parens): Set[Parens] = {
    val inside = p.contents.collect { case q: Parens => findParens(q) }
    inside.foldLeft(Set(p)) { _ | _ }
  }

  def mapParentheses(s: String) = {
    val (p, _) = str2parens(s)
    val pmap = findParens(p).toSeq.sortBy(_.text.length).zipWithIndex.toMap
    val p2c = pmap.mapValues(i => (i + 500 + mapIndex).toChar)
    p2c.map { case (p, c) => (p.mapText(p2c), c) }.toMap
  }*/
}

/*
class Parens(val contents: Seq[Part]) extends Part {
  val text = "(" + contents.mkString + ")"
  def mapText(m: Map[Parens, Char]) = {
    val inside = contents.collect {
      case p: Parens => m(p).toString
      case x => x.toString
    }
    "(" + inside.mkString + ")"
  }
  override def equals(a: Any) = a match {
    case p: Parens => text == p.text
    case _ => false
  }
  override def hashCode = text.hashCode
}
*/