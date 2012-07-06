package forms

import scala.collection.immutable.ListMap
import scala.xml._
import forms.fields.Field
import forms.widgets.Widget
import util.Helpers.camel2TitleCase
import forms.validators.ValidationError

class BoundField[T](val form: Form, val field: Field[T], val name: String) {
  val htmlName = form.addPrefix(name)
  val htmlInitialName = form.addInitialPrefix(name)
  val htmlInitialId = form.addInitialPrefix(autoId.getOrElse(""))
  val label = field.label.getOrElse(camel2TitleCase(name))
  val helpText = field.helpText.getOrElse("")
  
  def asWidget(widget: Widget = field.widget, attrs: MetaData = Null, onlyInitial: Boolean = false): NodeSeq = {
    val idAttr = if (autoId.isDefined && attrs.get("id").isEmpty && widget.attrs.get("id").isEmpty) {
      new UnprefixedAttribute("id", Text(if (!onlyInitial) autoId.get else htmlInitialId), Null)
    } else {
      Null
    }
    widget.render(if (!onlyInitial) htmlName else htmlInitialName, value, attrs.append(idAttr))
  }
  
  def data: Seq[String] = {
    field.widget.valueFromDatadict(form.data, htmlName)
  }
  
  def value: Seq[String] = {
    if (!form.isBound) form.initial.getOrElse(name, Nil)
    else field.boundData(data, form.initial.getOrElse(name, field.initial))
  }
  
  def labelTag(contents: Option[String] = None, attrs: MetaData = Null): NodeSeq = {
    val text = contents.getOrElse(label)
    val widget = field.widget
    val id = widget.attrs.get("id") match {
      case Some(theId) => Some(theId)
      case None => autoId.map(Text(_))
    }
    if (id.isDefined) {
      <label>{ text }</label> % new UnprefixedAttribute("for", id, attrs)
    } else {
      Text(text)
    }    
  }
  
  def autoId: Option[String] = {
    form.autoId.map(id => {
      if (id.contains("%s")) id.format(htmlName)
      else htmlName
    })
  }
}

abstract class Form(
    val autoId: Option[String] = Some("id_%s"),
    val prefix: Option[String] = None,
    val initial: Map[String, Seq[String]] = Map(),
    labelSuffix: String = ":",
    emptyPermitted: Boolean = false) {
  def fields: ListMap[String, Field[_]]
  
  def addPrefix(fieldName: String): String = {
    prefix.map(p => "%s-%s".format(p, fieldName)).getOrElse(fieldName)
  }
  
  def addInitialPrefix(fieldName: String): String = {
    "initial-%s".format(addPrefix(fieldName))
  }

  def isBound: Boolean = false
  def data: Map[String, Seq[String]] = Map()
  def errors: Map[String, ValidationError] = Map()
  def isValid: Boolean = isBound && errors.isEmpty
  def cleanData: Option[Map[String, BoundField[_]]] = None
  
  def getField(name: String): Option[BoundField[_]] = {
    fields.get(name).map(new BoundField(this, _, name))
  }
  
  def asHtml: Elem = {
    <form>{
    fields.flatMap(namePlusField => {
      val name = namePlusField._1
      val field: BoundField[_] = getField(name).get
      val label = field.label
      val labelName = if (label == "") "" else {
        if (":?.!".contains(label.substring(label.length - 1, label.length))) label
        else label + labelSuffix
      }
      val labelPart = 
        if (labelName != "") field.labelTag(Some(labelName)) ++ Text(" ")
        else NodeSeq.Empty
      labelPart ++ field.asWidget()
    }).toList
    }</form>
  }
}