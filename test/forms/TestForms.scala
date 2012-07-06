package forms
import org.scalatest.FunSuite
import scala.collection.immutable.ListMap
import forms.fields._
import scala.xml.{Elem, NodeSeq}
import scala.xml.Utility.trim
import scala.xml.Equality.compareBlithely

class PersonForm extends Form {
  def fields: ListMap[String, Field[_]] = ListMap(
      "firstName" -> new TextField(),
      "lastName" -> new TextField(),
      "age" -> new NumericField[Int]()
  )
}

object PersonForm {
  val unbound: Elem = 
<form>
  <label for="id_firstName">First name:</label> <input type="text" name="firstName" id="id_firstName" />
  <label for="id_lastName">Last name:</label> <input id="id_lastName" type="text" name="lastName" />
  <label for="id_age">Age:</label> <input id="id_age" type="text" name="age" />
</form>

}

class TestForms extends FunSuite {
  
  test("unbound form") {
    val f = new PersonForm()
    assert(f.isBound === false)
    assert(f.errors === Map())
    assert(f.isValid === false)
    assert(f.cleanData === None)
    assert(f.asHtml == PersonForm.unbound, f.asHtml.diff(PersonForm.unbound))
  }
}