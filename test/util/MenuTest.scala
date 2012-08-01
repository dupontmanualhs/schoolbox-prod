package util

import scala.xml.Utility.trim

import org.scalatest.FunSuite

class MenuTest extends FunSuite {  
  test ("correct html") {
    val menu1 = new MenuItem("Menu 1", Some("/page1"), Nil)
    val menu2 = new MenuItem("Menu 2", None, List(menu1))
    val html2 = <li><a href="#">Menu 2</a>
                  <ul><li><a href="/page1">Menu 1</a></li></ul>
              </li>

    assert(trim(menu2.asHtml) === trim(html2))
  }
}