package util

import scala.xml.{Elem, NodeSeq}

class MenuItem(val name: String, val link: Option[String], val subItems: List[MenuItem]) {
  def asHtml: Elem = <li><a href={ link.getOrElse("#") }>{ name }</a>{
                       if (subItems.isEmpty) NodeSeq.Empty
                       else <ul>{ subItems.flatMap(_.asHtml) }</ul>
                     }</li>
}

class MenuBar(val menus: List[MenuItem]) {
  def asHtml: Elem = <ul>{ ... }</ul>
}