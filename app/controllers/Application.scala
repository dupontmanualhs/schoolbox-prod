package controllers

import play.api._
import forms._
import forms.fields._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction

object Application extends Controller {

  object formTests extends Form{
    //val BooleanField = new BooleanField("Boolean")
    val ChoiceField = new ChoiceFieldOptional("Choice", List(("hi", "hi"),("bye","bye")))
    val DateField = new DateFieldOptional("Date")
    val EmailField = new EmailFieldOptional("Email")
    val NumericField = new NumericFieldOptional[Double]("Double")
    val PasswordField = new PasswordFieldOptional("Password")
    val TextField = new TextFieldOptional("Text")
    val UrlField = new UrlFieldOptional("Url")
    
    val fields = List(ChoiceField, DateField, EmailField, NumericField, PasswordField, TextField, UrlField)
    
  }
  
  def index() = DbAction { implicit req =>
    Ok(views.html.index())
  }

  def stub() = DbAction { implicit req => 
    Ok(views.html.stub())
  }
  
  def formTest() = DbAction { implicit req =>
    if(req.method=="GET") Ok(views.html.formTester(Binding(formTests)))
    else Binding(formTests, req) match {
      case ib: InvalidBinding => Ok(views.html.formTester(ib))
      case vb: ValidBinding => {
        //I'm a little too lazy to do this atm, soo...
        Ok(views.html.index())
      }
    }
  }
  
}
