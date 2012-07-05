package forms
import org.scalatest.FunSuite
import scala.collection.immutable.ListMap
import forms.fields._
import scala.xml.NodeSeq

class PersonForm extends Form {
  def fields: ListMap[String, Field[_]] = ListMap(
      "firstName" -> new TextField(),
      "lastName" -> new TextField(),
      "age" -> new NumericField[Int]()
  )
}

class TestForms extends FunSuite {
  test("unbound form") {
    val f = new PersonForm()
    assert(f.isBound === false)
    assert(f.errors === Map())
    assert(f.isValid === false)
    assert(f.cleanData === None)
    /*assert(f.asHtml === List(<label for="id_first_name">First name:</label> <input type="text" name="first_name" id="id_first_name" />,
        <label for="id_last_name">Last name:</label> <input type="text" name="last_name" id="id_last_name" />,
        <label for="id_birthday">Birthday:</label> <input type="text" name="birthday" id="id_birthday" />))*/
  }
}