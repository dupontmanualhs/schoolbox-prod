package controllers
import org.scalatest.FunSuite

import models.books.Title

class TestBooks extends FunSuite {
  test("ISBN check digits") {
    assert(Title.checkDigit("059610199") === Some("6"))
    assert(Title.checkDigit("978059610199") === Some("2"))
    assert(Title.checkDigit("abcd") === None)
    assert(Title.checkDigit("053494728") === Some("X"))
    assert(Title.checkDigit("978053494728") === Some("6"))
  }
  
  test("change 10-digit ISBN to 13-digit") {
    assert(Title.makeIsbn13("0596101996") === "9780596101992")
    assert(Title.makeIsbn13("053494728X") === "9780534947286")
  }

}