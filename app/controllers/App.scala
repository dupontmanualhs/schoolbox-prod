package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import forms.{ Binding, Form, InvalidBinding, ValidBinding }
import forms.fields._
import forms.widgets._
import forms.validators.ValidationError
import controllers.users.VisitAction
import com.google.inject.{ Inject, Singleton }
import config.Config

@Singleton
class App @Inject()(implicit config: Config) extends Controller {
  def index() = VisitAction { implicit req =>
      Ok(templates.Index(templates.Main))
  }
  
  def stub() = VisitAction { implicit req =>
      Ok(templates.Stub(templates.Main))
  }


  object FormTests extends Form {
    //val BooleanField = new BooleanField("Boolean")
    val ChoiceField = new ChoiceFieldOptional("Choice", List(("hi", "hi"), ("bye", "bye")))
    val DateField = new DateFieldOptional("Date")
    val TimeField = new TimeFieldOptional("Time")
    val TimestampField = new TimestampFieldOptional("Timestamp")
    val EmailField = new EmailFieldOptional("Email")
    val NumericField = new NumericFieldOptional[Double]("Double")
    val PasswordField = new PasswordFieldOptional("Password")
    val TextField = new TextFieldOptional("Text")
    val UrlField = new UrlFieldOptional("Url")
    val PhoneField = new PhoneFieldOptional("Phone")
    val listOfSpectopers = List("Allen", "Zach", "John", "Others")
    val ACField = new AutocompleteFieldOptional("AC", listOfSpectopers)

    val editedTextField = new TextFieldOptional("edited") {
      override def widget = new TextInput(required)

      override def helpText = Some(<p>Please input "lolCats" for true</p><p>here is a reset button: <button type="reset" class="btn">useless</button></p>)

      override def asValue(s: Seq[String]): Either[ValidationError, Option[String]] = {
        s match {
          case Seq() => Right(None)
          case Seq(str) => if (str == "lolCats") Right(Some("true")) else Right(Some("false"))
          case _ => Left(ValidationError("Expected a single value, got multiples."))
        }
      }
    }

    val fields = List(ACField, ChoiceField, DateField, TimeField, TimestampField, EmailField, NumericField, PasswordField, PhoneField, TextField, UrlField, editedTextField)

    override def prefix: Option[String] = None
    override def submitText = "Submit"
    override def includeCancel = true
    override def cancelText = "Cancel"

  }

  /*def formTest() = Action { implicit req =>
    Ok(views.html.formTester(Binding(FormTests)))
  }

  def formTestP() = Action { implicit req =>
    Binding(FormTests, req) match {
      case ib: InvalidBinding => Ok(views.html.formTester(ib))
      case vb: ValidBinding => {
        val TheChoice = vb.valueOf(FormTests.ChoiceField)
        val TheDate = vb.valueOf(FormTests.DateField)
        val TheTime = vb.valueOf(FormTests.TimeField)
        val TheTimestamp = vb.valueOf(FormTests.TimestampField)
        val TheEmail = vb.valueOf(FormTests.EmailField)
        val TheNumeric = vb.valueOf(FormTests.NumericField)
        val ThePassword = vb.valueOf(FormTests.PasswordField)
        val TheText = vb.valueOf(FormTests.TextField)
        val TheUrl = vb.valueOf(FormTests.UrlField)
        val TheEdited = vb.valueOf(FormTests.editedTextField)
        val ThePhone = vb.valueOf(FormTests.PhoneField)
        val ListOfStuff = List(("Choice Field", TheChoice.toString), ("Date Field", TheDate.toString), ("Time Field", TheTime.toString), ("Timestamp Field", TheTimestamp.toString), ("Email Field", TheEmail.toString), ("NumericField", TheNumeric.toString), ("Password Field", ThePassword.toString), ("Phone Field", ThePhone.toString), ("Text Field", TheText.toString), ("Url Field", TheUrl.toString), ("Edited Field", TheEdited.toString))

        Ok(views.html.showResults(ListOfStuff))
      }
    }
  }*/
}


