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
  // TODO: check that fields have unique names?
  def fields: List[Field[_]]
  import play.api._
  import play.api.mvc._
  def validate(data: ValidBinding): ValidationError = new ValidationError(Nil)
  def cancelTo: String = "url"
  def method = "post"
  def autoId: Option[String] = Some("id_%s")
  def prefix: Option[String] = None
  def labelSuffix: String = ":"
    
  def asHtml(bound: Binding, action: String, legend: String = ""): Elem = {
    <form method={ method } class="form-horizontal well" action={ action }>
<fieldset>
    { if (legend != "") <legend>{ legend }</legend> }
    <fieldset>
    { if (bound.formErrors.isEmpty) NodeSeq.Empty else { bound.formErrors.asHtml } }  
    {fields.flatMap(f => {
      val name = f.name
      val label = f.label.getOrElse(camel2TitleCase(f.name))
      val labelName = if (label == "") "" else {
        if (":?.!".contains(label.substring(label.length - 1, label.length))) label
        else label + labelSuffix
      }
      val labelPart = 
        if (labelName != "") f.labelTag(this, Some(labelName)) ++ Text(" ")
        else NodeSeq.Empty
      val errorList = bound.fieldErrors.get(name).map(_.asHtml)
      <div class="control-group">
        { labelPart }
        <div class="controls">{ f.asWidget(bound) }</div>
        { if (bound.hasErrors) { errorList.getOrElse(NodeSeq.Empty) } 
          else NodeSeq.Empty }
      </div>
    }).toList
    }</fieldset>
    <div class="form-actions"> 
    	<button type="submit" class="btn btn-primary">Submit</button>
    	
    	{ if (cancelTo=="url"){
    	    <button type="button" class="btn" onclick="window.location.href=document.URL">Cancel</button>
    	} else {
    		val newLink = "window.location.href='"+cancelTo+"'"
    	    <button type="button" class="btn" onclick={newLink}>Cancel</button>
    	} }
    	
    	<button type="reset" class="btn">Clear Form</button>
    </div>
    </fieldset>
    </form>
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



