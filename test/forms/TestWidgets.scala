package forms

import widgets._
import Widget._

import org.scalatest.FunSuite

class TestWidgets extends FunSuite {
  test("TextInput widget") {
    val ti1 = new TextInput(attrs = Map("class"->"foo"))
    assert(ti1.render("entry", Nil) === <input type="text" name="entry" class="foo" />)
    assert(ti1.render("entry", List("abc")) === 
      <input type="text" name="entry" value="abc" class="foo" />)
  }
  
  test("PasswordInput widget") {
    val pi1 = new PasswordInput()
    val pi2 = new PasswordInput(renderValue = true)
    assert(pi1.render("pw", List("pass")) === <input type="password" name="pw" />)
    assert(pi2.render("pw", List("pass")) === <input type="password" name="pw" value="pass" />)
  }
  
  test("HiddenInput widget") {
    val hi1 = new HiddenInput(attrs = Map("class"->"foo"))
    assert(hi1.render("key1", List("value1")) === 
        <input type="hidden" name="key1" value="value1" class="foo" />)
  }
  
  test("Textarea widget") {
    val ta = new Textarea()
    assert(ta.render("words", List("abc")) ===
      <textarea name="words" cols="40" rows="10">abc</textarea>)
    assert(ta.render("moreWords", Nil, Map("rows"->"4")) ===
      <textarea name="moreWords" cols="40" rows="4"></textarea>)
    assert(ta.render("wordsAlso", List("xyz"), Map("cols"->"80")) ===
      <textarea name="wordsAlso" cols="80" rows="10">xyz</textarea>)
  }
  
  test("CheckboxInput widget") {
    val cb = new CheckboxInput()
    assert(cb.render("box", List("true")) ===
      <input type="checkbox" name="box" checked="checked" />)
  }
}