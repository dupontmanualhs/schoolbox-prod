package models.users

import scala.xml.{ Elem, NodeSeq }

class MenuItem(val name: String, val id: String, val link: Option[String], val subItems: List[MenuItem]) {
  def asHtml: Elem = if (subItems.isEmpty) {
    <li>
      <a href={ link.getOrElse("#") } id={ id }>{ name }</a>
    </li>
  } else {
    <li class="dropdown">
      <a class="dropdown-toggle" data-toggle="dropdown" href="#">
        { name }
        <b class="caret"> </b>
      </a>
      <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
          { subItems.flatMap(_.asHtml) }
      </ul>
   </li >
  }
}

class MenuBar(val menus: List[MenuItem]) {
  def asHtml: Elem = <ul class="nav">{ menus.flatMap(_.asHtml) }</ul>
}
