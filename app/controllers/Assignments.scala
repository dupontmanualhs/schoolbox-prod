package controllers

import scala.xml.NodeSeq
import play.api.mvc.Controller
import util.DbAction
import models.assignments.{QSource, Source}
import util.ScalaPersistenceManager
import util.DbRequest

object Assignments extends Controller {
  def sources() = DbAction { implicit req =>
    implicit val pm = req.pm
    val sourceVar = QSource.variable("parent")
    val cand = QSource.candidate
    val topLevelSources = pm.query[Source].filter(cand.parent.eq(null.asInstanceOf[Source])).executeList()
    Ok(views.html.assignments.sources(topLevelSources))
  }
  
  def asListItem(source: Source)(implicit req: DbRequest[_]): NodeSeq = {
    implicit val pm = req.pm
    source.children match {
      case Nil => <li>{ source.name }</li>
      case children: List[_] => <li>{ source.name }<ul>{ children.map(asListItem(_)) }</ul></li>
    }
  }


}