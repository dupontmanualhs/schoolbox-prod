package models.books
import org.scalatest.FunSuite
import models.books.BookData._
import org.joda.time.LocalDate

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

  test("Test the toString method on Checkout") {
    assert(models.books.BookData.gabb3check.toString() === "Checkout: Copy 9781880184264-200-00200 to Jack Phillips from Some(2015-10-05) to None")
    assert(models.books.BookData.tmicheck.toString() === "Checkout: Copy 9780553345841-200-00500 to Brick House from Some(2013-08-09) to None")
    assert(models.books.BookData.tgcheck.toString() === "Checkout: Copy 0262510375-200-00150 to Laura King from Some(2014-05-12) to None")
    assert(models.books.BookData.semanticscheck.toString() === "Checkout: Copy 9780631200345-200-00099 to Jordan Jorgensen from Some(2012-01-14) to None")
  }
  test("Test the toString method on PurchaseGroup"){
    val currentDate = LocalDate.now()
    assert(models.books.BookData.gabb3pg.toString() === "Purchased "+currentDate+": copies of The Great American Bathroom Book at $59.99 each")
    assert(models.books.BookData.tmipg.toString() === "Purchased "+currentDate+": copies of The Mind's I at $0.99 each")
    assert(models.books.BookData.tgpg.toString() === "Purchased "+currentDate+": copies of Turtle Geometry at $12.59 each")
    assert(models.books.BookData.semanticspg.toString() === "Purchased "+currentDate+": copies of Semantics at $100.00 each")
  }
  test("Test the toString method on LabelQueueSet"){
    assert(models.books.BookData.gabb3queue.toString() === "Copies 2,8,3,7,4 of The Great American Bathroom Book")
    assert(models.books.BookData.tmiqueue.toString() === "Copies 1-10 of The Mind's I")
    assert(models.books.BookData.tgqueue.toString() === "Copies 3,7 of Turtle Geometry")
    assert(models.books.BookData.semanticsqueue.toString() === "Copies 1,3 of Semantics")
  }
  test("Test the toString method on Copy"){
    assert(models.books.BookData.gabb3copy.toString() === "9781880184264-200-00200")
    assert(models.books.BookData.tmicopy.toString() === "9780553345841-200-00500")
    assert(models.books.BookData.tgcopy.toString() === "0262510375-200-00150")
    assert(models.books.BookData.semanticscopy.toString() === "9780631200345-200-00099")
  }
}