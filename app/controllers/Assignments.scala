package controllers

import scala.xml.NodeSeq
import play.api.mvc.Controller
import util.DbAction
import models.assignments.{QSource, Source, QSubject, Subject}
import util.ScalaPersistenceManager
import util.DbRequest
import play.api.templates.Html
import play.api.data._
import play.api.data.Forms._


object Assignments extends Controller {
  val newQuestion = Form {
    tuple(
   		"kind" -> nonEmptyText,
    	"question" -> nonEmptyText,
    	"answer" -> nonEmptyText
    )
  }
  
  val options = List(("math", "Math"), ("science", "Science"), ("english", "English"), ("social Studies", "Social Studies"))
  
  def sources() = DbAction { implicit req =>
    implicit val pm = req.pm
    val cand = QSource.candidate
    val topLevelSources = pm.query[Source].filter(cand.parent.eq(null.asInstanceOf[Source])).executeList()
    Ok(views.html.assignments.sources(topLevelSources))
  }
  
  def subjects() = DbAction { implicit req =>
    implicit val pm = req.pm
    val cand = QSubject.candidate
    val topLevelSubjects = pm.query[Subject].filter(cand.parent.eq(null.asInstanceOf[Subject])).executeList()
    Ok(views.html.assignments.subjects(topLevelSubjects))
  }
  
  def asListItem(source: Source)(implicit req: DbRequest[_]): NodeSeq = {
    implicit val pm = req.pm
    source.children match {
      case Nil => <li>{ source.name }</li>
      case children: List[_] => <li>{ source.name }<ul>{ children.map(asListItem(_)) }</ul></li>
    }
  }
  
  def asListItem(subject: Subject)(implicit req: DbRequest[_]): NodeSeq = {
    implicit val pm = req.pm
    subject.children match {
      case Nil => <li>{ subject.name }</li>
      case children: List[_] => <li>{ subject.name }<ul>{ children.map(asListItem(_)) }</ul></li>
    }
  }
  
  def addQuestion() = DbAction { implicit req =>
  	Ok(views.html.questions(newQuestion, options, false))
  } 
  
  def submitQuestion(newKind: Boolean) = DbAction{ implicit req =>
    newQuestion.bindFromRequest.fold(
   	  errors => BadRequest(views.html.questions(errors, options, newKind)),
      value =>
      //add question to the database
      Ok(views.html.questions(newQuestion, options, newKind))
    )
  }
  
  def newKind() = DbAction{ implicit req =>
    Ok(views.html.questions(newQuestion, options, true))
  }


}