package controllers.users

import scala.xml.Elem

class MenuItem(val name: String, val id: String, val link: Option[String], val dropItems: List[MenuItem], val sideItems: List[MenuItem] = Nil) { //only include a sideItems list if it is a drop item
  def asHtml: Elem = if (dropItems.isEmpty && sideItems.isEmpty) {																				 //sideItems are the dropdown within a dropdown 
    <li>
      <a href={ link.getOrElse("#") } id={ id }>{ name }</a>
    </li>
  } else if(!dropItems.isEmpty){
    <li class="dropdown">
      <a class="dropdown-toggle" data-toggle="dropdown" href="#">
        { name }
        <b class="caret"> </b>
      </a>
      <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
          { dropItems.flatMap(_.asHtml) }
      </ul>
   </li >
  } else {
    <li class="dropdown-submenu">
	  <a tabindex="-1" href="#">{name}</a>
	  <ul class="dropdown-menu">
		{sideItems.flatMap(_.asHtml)}
   	  </ul>
    </li>
  }
}

class MenuBar(val menus: List[MenuItem]) {
  def asHtml: Elem = <ul class="nav">{ if(menus.isEmpty) <div></div> else menus.flatMap(_.asHtml) }</ul>
}