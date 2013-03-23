package forms.widgets

import scala.xml._
import forms.validators.ValidationError

class AutocompleteInput(
  required: Boolean,
  array: String = "",
  uuid: java.util.UUID,
  attrs: MetaData = Null) extends Widget(required, attrs) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" name={ name } class={ "ac" + uuid.toString } value={ if (value.isEmpty) "" else value(0) }></input>
  }

  override def scripts: NodeSeq =
    <script type="text/javascript">
      $(function() {{
		var availableTags = {Unparsed(array.toString)};
  		$({".ac"+uuid.toString}).autoComplete({{
  		  source: availableTags
  		}});
  	  }});
    </script>
}