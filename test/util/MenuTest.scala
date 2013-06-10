package util

import scala.xml.Utility.trim

import org.scalatest.FunSuite

class MenuTest extends FunSuite {
  val menu1 = new MenuItem("Menu 1", "m1", Some("/page1"), Nil)
  val menu2 = new MenuItem("Menu 2", "m2", None, List(menu1))
  
  val html1 = <li>
  			    <a href="/page1" id="m1">Menu 1</a>
  			  </li>
  
  val html2 = <li class="dropdown" >
                <a class="dropdown-toggle" data-toggle="dropdown" href="#" >
  				  Menu 2
  				  <b class="caret"></b>
  				</a>
  				<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
  				  <li>
  				    <li><a href="/page1" id="m1">Menu 1</a></li>
                  </li>
                </ul>
              </li>

  test ("correct html") {
    assert(trim(menu1.asHtml) === trim(html1))
    assert(trim(menu2.asHtml) === trim(html2))
  }
}