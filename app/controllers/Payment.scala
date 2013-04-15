package controllers

import play.api._
import play.api.mvc._
import util.{DataStore, ScalaPersistenceManager}
import util.DbAction
import forms._
import forms.fields._
import forms.widgets._

object Payment extends Controller {
  
  object PaymentForm extends Form {
    val address = new TextField("Address")
    val phone = new NumericField[Int]("Phone")
    val account = new NumericField[Int]("Account")
    val pin = new NumericField[Int]("PIN")
    val fields = List(address, phone, account, pin)
  }

  def payForm() = DbAction { implicit request => 
  	if (request.method == "GET") Ok(views.html.payment.paymentInfo(Binding(PaymentForm)))
  		else Binding(PaymentForm, request) match {
  		case ib: InvalidBinding => Ok(views.html.payment.paymentInfo(ib)) // there were errors
  		case vb: ValidBinding => {
 			val theAddress: String = vb.valueOf(PaymentForm.address)
  			val thePhone: Int = vb.valueOf(PaymentForm.phone)
  			val theAccount: Int = vb.valueOf(PaymentForm.account)
  			val thePIN: Int = vb.valueOf(PaymentForm.pin)
  			// do whatever you want with the values now (notice they're typesafe!)
  		//Redirect(routes.Application.nextPage())
  			}
  		}
	}
}