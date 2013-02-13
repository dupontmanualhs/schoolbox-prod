package forms
import org.scalatest.FunSuite
import scala.collection.immutable.ListMap
import forms.fields._
import scala.xml.{Elem, NodeSeq}
import scala.xml.Utility.trim
import util.Helpers.xmlEquals
import forms.validators.ValidationError

class PersonForm extends Form {
  val firstName = new TextField("firstName")
  val lastName = new TextField("lastName")
  val age = new NumericField[Int]("age")
  
  def fields = List(firstName, lastName, age)
}
/*
object PersonForm {
  val unbound: Elem = 
<form method="post">
  <table>
    <tr>
      <td><label for="id_firstName">First Name:</label></td>
      <td><input type="text" name="firstName" id="id_firstName" required="required" /></td>
    </tr>
    <tr>
      <td><label for="id_lastName">Last Name:</label></td>
      <td><input id="id_lastName" type="text" name="lastName" required="required" /></td>
    </tr>
    <tr>
      <td><label for="id_age">Age:</label></td>
      <td><input id="id_age" type="text" name="age" required="required" /></td>
    </tr>
  </table>
  <input type="submit" />
</form>
    
  val bound: Elem = 
<form method="post">
  <table>
    <tr>
      <td><label for="id_firstName">First Name:</label></td>
      <td><input type="text" name="firstName" id="id_firstName" required="required" value="John" /></td>
    </tr>
    <tr>
      <td><label for="id_lastName">Last Name:</label></td>
      <td><input id="id_lastName" type="text" name="lastName" required="required" value="Lennon" /></td>
    </tr>
    <tr>
      <td><label for="id_age">Age:</label></td>
      <td><input id="id_age" type="text" name="age" required="required" value="72" /></td>
    </tr>
  </table>
  <input type="submit" />
</form>
    
  val withRequiredErrors: Elem =
<form method="post">
  <table>
    <tr>
      <td><label for="id_firstName">First Name:</label></td>
      <td><input type="text" name="firstName" id="id_firstName" required="required" /></td>
      <td><ul class="errorlist"><li>This field is required.</li></ul></td>
    </tr>
    <tr>
      <td><label for="id_lastName">Last Name:</label></td>
      <td><input id="id_lastName" type="text" name="lastName" required="required" /></td>
      <td><ul class="errorlist"><li>This field is required.</li></ul></td>
    </tr>
    <tr>
      <td><label for="id_age">Age:</label></td>
      <td><input id="id_age" type="text" name="age" required="required" /></td>
      <td><ul class="errorlist"><li>This field is required.</li></ul></td>
    </tr>
  </table>
  <input type="submit" />
</form>
}

class TestForms extends FunSuite {
  test("unbound form") {
    val f = new PersonForm()
    assert(trim(Binding(f).asHtml) === trim(PersonForm.unbound))
  }
  
  test("bound form") {
    val f = new PersonForm()
    val b = Binding(f, Map("firstName" -> "John", "lastName" -> "Lennon", "age" -> "72"))
    assert(b.fieldErrors.isEmpty, "%s should be empty".format(b.fieldErrors))
    assert(b.formErrors.isEmpty, "%s should be empty".format(b.formErrors))
    assert(b.isInstanceOf[ValidBinding])
    val vb: ValidBinding = b.asInstanceOf[ValidBinding]
    assert(vb.valueOf(f.firstName) === "John")
    assert(vb.valueOf(f.lastName) === "Lennon")
    assert(vb.valueOf(f.age) === 72)
    assert(f.firstName.asWidget(b) === 
      <input type="text" name="firstName" value="John" id="id_firstName" required="required" />)
    assert(f.lastName.asWidget(b) === 
      <input type="text" name="lastName" value="Lennon" id="id_lastName" required="required" />)
    assert(f.age.asWidget(b) === 
      <input type="text" name="age" value="72" id="id_age" required="required" />)
    assert(trim(b.asHtml) === trim(PersonForm.bound))
  }
  
  test("bind with empty Map") {
    val f = new PersonForm()
    val b = Binding(f, Map.empty[String, String])
    assert(b.isInstanceOf[InvalidBinding])
    val ib = b.asInstanceOf[InvalidBinding]
    assert(b.fieldErrors(f.firstName) === Some(ValidationError("This field is required.")))
    assert(b.fieldErrors(f.lastName) === Some(ValidationError("This field is required.")))
    assert(b.fieldErrors(f.age) === Some(ValidationError("This field is required.")))
    // if there are errors, boundFields is an empty Map
    assert(trim(b.asHtml) === trim(PersonForm.withRequiredErrors))
  }
  
  test("cleaned data only includes defined fields") {
    val f = new PersonForm()
    val b = Binding(f, Map("firstName" -> "John", "lastName" -> "Lennon", "age" -> "72",
            			   "extra1" -> "hello", "extra2" -> "hello"))
    assert(b.isInstanceOf[ValidBinding])
    val vb: ValidBinding = b.asInstanceOf[ValidBinding]
    assert(vb.valueOf(f.firstName) === "John")
    assert(vb.valueOf(f.lastName) === "Lennon")
    assert(vb.valueOf(f.age) === 72)
  }
  
}*/