package forms.widgets

import scala.xml._

class SelectInput(
    required: Boolean,
    val options: List[String],
    attrs: MetaData = Null,
    val allowMultiple: Boolean = false) extends Widget(required, attrs) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    // if allowMultiple is false, only allow one value
    val limitedVal = if (allowMultiple) value else if (value.isEmpty) Nil else List(value.head)
    val multAttr = if (allowMultiple) new UnprefixedAttribute("multiple", Text("multiple"), Null) else Null
    <select name={ name }>
      { if (limitedVal.isEmpty) <option value="-1"> </option> else NodeSeq.Empty }
      { options.zipWithIndex.map { 
        case (disp, i) => {
          val selAttr = if (limitedVal.contains(i.toString)) new UnprefixedAttribute("selected", Text("selected"), Null) else Null
          <option value={ i.toString }>{ disp }</option> % selAttr
        }
      }
    }
    </select> % attrs % multAttr % attrList // <select> doesn't allow a required attribute
  }
}