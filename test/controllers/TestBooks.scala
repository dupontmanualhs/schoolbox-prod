package controllers
import org.scalatest.FunSuite

class TestBooks extends FunSuite {
  test("ISBN check digits") {
    assert(Books.checkDigit("059610199") === Some("6"))
    assert(Books.checkDigit("978059610199") === Some("2"))
    assert(Books.checkDigit("abcd") === None)
    assert(Books.checkDigit("053494728") === Some("X"))
    assert(Books.checkDigit("978053494728") === Some("6"))
  }
  
  test("change 10-digit ISBN to 13-digit") {
    assert(Books.makeIsbn13("0596101996") === "9780596101992")
    assert(Books.makeIsbn13("053494728X") === "9780534947286")
  }

}