package controllers

import scala.xml.Elem
import scala.xml.MetaData
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Null
import scala.xml.transform.BasicTransformer
import forms._
import forms.fields._
import forms.widgets._
import javax.jdo.annotations.PersistenceCapable
import models.mastery._
import play.api.mvc.Controller
import play.api.mvc.PlainResult
import util.DbAction
import util.DbRequest
import util.Helpers.string2elem
import util.ScalaPersistenceManager
import views.html
import forms.validators.ValidationError
import scala.xml.UnprefixedAttribute
import scala.xml.Text

object FieldTester extends Controller {

  object BasicFieldsForms extends Form {

    val BooleanFieldTest = new BooleanField("Boolean Field")

    val ChoiceFieldTest = new ChoiceField("Choice Field", List(("choice 1", 1), ("choice 2", 2)))

    val DateFieldTest = new DateField("Date")

    val EmailFieldTest = new EmailField("Email")

    val NumericFieldTest = new NumericField[Int]("Integer")

    val PasswordFieldTest = new PasswordField("Password")

    val TextFieldTest = new TextField("Text Field")

    val TinyMCEFieldTest = new TinyMCEField("TinyMCE")

    val UrlFieldTest = new UrlField("Url")

    val fields = List(BooleanFieldTest, ChoiceFieldTest, DateFieldTest, EmailFieldTest, NumericFieldTest, PasswordFieldTest,
      TextFieldTest, TinyMCEFieldTest, UrlFieldTest)

  }

  def BasicFieldsTest = DbAction { implicit req =>
    if (req.method == "GET") Ok(views.html.formtester(Binding(BasicFieldsForms)))
    else Binding(BasicFieldsForms, req) match {
      case ib: InvalidBinding => Ok(views.html.formtester(ib)) // there were errors
      case vb: ValidBinding => {

        val TheBooleanField: Boolean = vb.valueOf(BasicFieldsForms.BooleanFieldTest)
        val TheChoiceField: String = { 
          val temp:Int = vb.valueOf(BasicFieldsForms.ChoiceFieldTest)
          if(temp==1) "choice 1"
          else if(temp==2) "choice 2"
          else "ERROR: "+temp.toString
        }
                
        val TheDateField: java.sql.Date = vb.valueOf(BasicFieldsForms.DateFieldTest)
        val TheEmail: String = vb.valueOf(BasicFieldsForms.EmailFieldTest)
        val TheNumeric: Int = vb.valueOf(BasicFieldsForms.NumericFieldTest)
        val ThePassword: String = vb.valueOf(BasicFieldsForms.PasswordFieldTest)
        val TheText: String = vb.valueOf(BasicFieldsForms.TextFieldTest)
        val TheTinyMCE: String = vb.valueOf(BasicFieldsForms.TinyMCEFieldTest)
        val TheUrl: String = {
          val temp: java.net.URL = vb.valueOf(BasicFieldsForms.UrlFieldTest)
          temp.toString
        }

        // do whatever you want with the values now (notice they're typesafe!)

        Ok(views.html.formtesteranswers(List(TheBooleanField.toString, TheChoiceField, TheDateField.toString, TheEmail, TheNumeric.toString,
          ThePassword, TheText, TheTinyMCE, TheUrl)))
      }
    }
  }

}