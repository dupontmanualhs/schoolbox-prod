package controllers

import play.api._
import forms._
import forms.fields._
import forms.validators._
import forms.widgets._
import play.api.mvc._
import scalajdo.DataStore

object Application extends Controller {

  object formTests extends Form {
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
    val Checkboxo = new CheckboxFieldOptional("Checkboxes", List(("car", 11) , ("van", 12) , ("truck", 13)))
    val RadioR = new RadioField("Radio", List(("cat",11),("dog",12),("mouse",13), ("Bird", "BIRD BIRD BIRD! BIRD IS THE WORD."), ("Turtle", listOfSpectopers)))

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

    val fields = List(RadioR, Checkboxo, ACField, ChoiceField, DateField, TimeField, TimestampField, EmailField, NumericField, PasswordField, PhoneField, TextField, UrlField, editedTextField)

    override def cancelTo: String = "url"
    override def prefix: Option[String] = None
    override def submitText = "Submit"
    override def includeCancel = true
    override def cancelText = "Cancel"

  }

  def index() = Action { implicit req =>
    DataStore.execute { implicit pm =>
      Ok(views.html.index())
    }
  }

  def stub() = Action { implicit req =>
    DataStore.execute { pm =>
      Ok(views.html.stub())
    }
  }

  def formTest() = Action { implicit req =>
    DataStore.execute { pm =>
      if (req.method == "GET") Ok(views.html.formTester(Binding(formTests)))
      else Binding(formTests, req) match {
        case ib: InvalidBinding => Ok(views.html.formTester(ib))
        case vb: ValidBinding => {
          val TheChoice = vb.valueOf(formTests.ChoiceField)
          val TheDate = vb.valueOf(formTests.DateField)
          val TheTime = vb.valueOf(formTests.TimeField)
          val TheTimestamp = vb.valueOf(formTests.TimestampField)
          val TheEmail = vb.valueOf(formTests.EmailField)
          val TheNumeric = vb.valueOf(formTests.NumericField)
          val ThePassword = vb.valueOf(formTests.PasswordField)
          val TheText = vb.valueOf(formTests.TextField)
          val TheUrl = vb.valueOf(formTests.UrlField)
          val TheEdited = vb.valueOf(formTests.editedTextField)
          val ThePhone = vb.valueOf(formTests.PhoneField)
          val TheCheckboxO = vb.valueOf(formTests.Checkboxo)
          val TheRadioR = vb.valueOf(formTests.RadioR)
          val ListOfStuff = List(("Radio", TheRadioR.toString),("Checkbox Optional", TheCheckboxO.toString),("Choice Field", TheChoice.toString), ("Date Field", TheDate.toString), ("Time Field", TheTime.toString), ("Timestamp Field", TheTimestamp.toString), ("Email Field", TheEmail.toString), ("NumericField", TheNumeric.toString), ("Password Field", ThePassword.toString), ("Phone Field", ThePhone.toString), ("Text Field", TheText.toString), ("Url Field", TheUrl.toString), ("Edited Field", TheEdited.toString))

          Ok(views.html.showResults(ListOfStuff))
        }
      }
    }
  }
}


