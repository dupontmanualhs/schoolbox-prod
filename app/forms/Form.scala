package forms

import scala.collection.immutable.ListMap
import scala.xml._
import forms.fields._
import forms.widgets.Widget
import util.Helpers.camel2TitleCase
import forms.validators.ValidationError
import play.api.mvc.Request

abstract class Form {
  // TODO: check that all names are unique
  def fields: List[Field[_]]
  def validate(data: ValidBinding): ValidationError = new ValidationError(Nil)
  
  def method = "post"
  def prefix: Option[String] = None
  def autoId: Option[String] = Some("id_%s")
  def submitText = "Submit"
  def includeCancel = false
  def cancelText = "Cancel"
  
  def render(bound: Binding): NodeSeq = {
    <form method={ method } class="form-horizontal">
      { bound.formErrors.render }
      { fields.flatMap(_.render(bound)) }
      { actions }
    </form>
  }
  
  def actions: NodeSeq = {
    <div class="form-actions">
      <button type="submit" class="btn btn-primary">{ submitText }</button>
      { if (includeCancel) <button type="button" class="btn">{ cancelText }</button>
        else NodeSeq.Empty }
    </div>
  }
        
  def addPrefix(fieldName: String): String = {
    prefix.map(p => "%s-%s".format(p, fieldName)).getOrElse(fieldName)
  }
  
  def addInitialPrefix(fieldName: String): String = {
    "initial-%s".format(addPrefix(fieldName))
  }
}



