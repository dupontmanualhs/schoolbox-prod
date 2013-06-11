/*package util

import scala.xml.{Elem, NodeSeq}

class VerticalMenuItem (val name: String, val id: String, val link: Option[String], val subItems: List[VerticalMenuItem]) {
  def asHtml: Elem = if (subItems.isEmpty) {
    					<li><a href={ link.getOrElse("#") } id={ id }>{ name }</a>
    					</li>
    					}
					else{
                         <li class="dropdown" >
                    	   <a class="dropdown-toggle" data-toggle="dropdown" href="#" >
  								{ name }
  								<b class="caret"></b>
  							</a>
  							<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
  								<li>
  									{ subItems.flatMap(_.asHtml) }
                       		    </li>
                       	   </ul>
                       	</li>
                         }
}

class VerticalMenuBar(val menus: List[VerticalMenuItem]) {
  def asHtml: Elem = <ul class="nav">{ menus.flatMap(_.asHtml) }</ul>
}

object VerticalMenu {
  val home = new VerticalMenuItem("Home", "menu_home", Some(controllers.routes.Grades.home(sectionId).toString), Nil)
  
  def buildVerticalMenu(): Elem = {
    val confr = new VerticalMenuItem("Conferences", "menu_conferences", Some(controllers.routes.Conferences.index().toString), Nil)
    val bar = new VerticalMenuBar(List(confr))
    bar.asHtml
  }
}*/