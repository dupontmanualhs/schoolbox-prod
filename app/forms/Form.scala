package forms

import scala.collection.immutable.ListMap
import scala.xml._
import forms.fields._
import forms.widgets.Widget
import util.Helpers.camel2TitleCase
import forms.validators.ValidationError
import play.api.mvc.Request
import play.api.templates.Html$

abstract class Form {
  // TODO: check that all names are unique
  def fields: List[Field[_]]
  import play.api._
  import play.api.mvc._
  def validate(data: ValidBinding): ValidationError = new ValidationError(Nil)
  def cancelTo: String = "url"
  def method = "post"
  def prefix: Option[String] = None
  def autoId: Option[String] = Some("id_%s")
  def submitText = "Submit"
  def includeCancel = false
  def cancelText = "Cancel"
    
  def render(bound: Binding): Node = {
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
  
  //TODO: don't have these be based off of toStrings maybe?
  def asHtml(bound: Binding): play.api.templates.Html = play.api.templates.Html(this.scripts.toString + asHtml(bound, "").toString)
  
  def scripts: play.api.templates.Html = play.api.templates.Html(fields.flatMap(_.widget.scripts).distinct.map(x=>x.toString).fold("")(_+_))
        
  def addPrefix(fieldName: String): String = {
    prefix.map(p => "%s-%s".format(p, fieldName)).getOrElse(fieldName)
  }
  
  def addInitialPrefix(fieldName: String): String = {
    "initial-%s".format(addPrefix(fieldName))
  }
}



