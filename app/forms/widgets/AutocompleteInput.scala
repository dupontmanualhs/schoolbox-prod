package forms.widgets

import scala.xml._
import forms.validators.ValidationError

class AutocompleteInput(
  required: Boolean,
  array: String = "",
  uuid: java.util.UUID,
  attrs: MetaData = Null) extends Widget(required, attrs) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" data-provide="typeahead" data-items="6" data-source={array.toString}></input>
  }

/*  override def scripts: NodeSeq =
    <script type="text/javascript">
      $(function() {{
		var availableTags = {Unparsed(array.toString)};
  		$({"'.ac"+uuid.toString+"'"}).autoComplete({{
  		  source: availableTags
  		}});
  	  }});
    </script>*/
}