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
import forms.fields.CheckboxFieldOptional
import forms.fields.RadioField

@Singleton
class App @Inject()(implicit config: Config) extends Controller {
  def index() = VisitAction { implicit req =>
      Ok(templates.Index())
  }
  
  def stub() = VisitAction { implicit req =>
      Ok(templates.Stub())
  }

  object FormTests extends Form {
    //val BooleanField = new BooleanField("Boolean")
    val ChoiceField = new ChoiceFieldOptional("Choice", List(("hi", "hi"), ("bye", "bye")))
    val DateField = new DateFieldOptional("Date")
    val TimeField = new TimeFieldOptional("Time")
    val TimestampField = new DateTimeFieldOptional("Timestamp")
    val EmailField = new EmailFieldOptional("Email")
    val NumericField = new NumericFieldOptional[Double]("Double")
    val PasswordField = new PasswordFieldOptional("Password")
    val TextField = new TextFieldOptional("Text")
    val UrlField = new UrlFieldOptional("Url")
    val PhoneField = new PhoneFieldOptional("Phone")
    val listOfSpectopers = List("Allen", "Quadralateral", "Blagoshpere", "Wisconsin", "Zach", "John", "Mack", "Ava", "Sam Fu", "Jim", "Mr. O'Bryan", "Eliza", "Xavier", "Zander Smith", "Others")
    val ACField = new AutocompleteFieldOptional("AC", listOfSpectopers)
    val Checkboxo = new CheckboxFieldOptional("Checkboxes", List(("car", 11) , ("van", 12) , ("truck", 13)))
    val RadioR = new RadioField("Radio", List(("cat",11),("dog",12),("mouse",13), ("Bird", "BIRD BIRD BIRD! BIRD IS THE WORD."), ("Turtle", listOfSpectopers)))
    val MultChoiceField = new CheckboxFieldOptional("Mult Choice", List(("UK", "Kentucky"),("University of Illinois", "Illinois"),("Wash U", "Missouri"),("MIT", "Massachucets")), useSelectInputMult = true)
    val FileField = new FileFieldOptional("File")

    val editedTextField = new TextFieldOptional("edited") {
      override def widget = new TextInput(required)

      override def helpText = Some(<p>Please input "lolCats" for true</p><p>here is a reset button: <button type="reset" class="btn">useless</button></p>)

      override def asValue(s: Seq[String]): Either[ValidationError, Option[String]] = {
        s match {
          case Seq() => Right(None)
          case Seq(str) => if (str == "lolCats") Right(Some("true")) else Right(Some("false"))
          case _ => Left(ValidationError("Expected a single value or none, got multiples."))
        }
      }
    }

    val fields = List(RadioR, FileField, MultChoiceField, Checkboxo, ACField, ChoiceField, DateField, TimeField, TimestampField, EmailField, NumericField, PasswordField, PhoneField, TextField, UrlField, editedTextField)

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
        val TheCheckboxO = vb.valueOf(FormTests.Checkboxo)
        val TheRadioR = vb.valueOf(FormTests.RadioR)
        val TheChoiceMult = vb.valueOf(FormTests.MultChoiceField)
        val TheFile = vb.valueOf(FormTests.FileField)
        val ListOfStuff = List(("Radio", TheRadioR.toString),("File", TheFile.toString),("Choice Field Mult", TheChoiceMult.toString),("Checkbox Optional", TheCheckboxO.toString),("Choice Field", TheChoice.toString), ("Date Field", TheDate.toString), ("Time Field", TheTime.toString), ("Timestamp Field", TheTimestamp.toString), ("Email Field", TheEmail.toString), ("NumericField", TheNumeric.toString), ("Password Field", ThePassword.toString), ("Phone Field", ThePhone.toString), ("Text Field", TheText.toString), ("Url Field", TheUrl.toString), ("Edited Field", TheEdited.toString))

        Ok(views.html.showResults(ListOfStuff))
      }
    }
  }*/
}


