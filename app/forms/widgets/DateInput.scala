package forms.widgets

import scala.xml._
import forms.validators.ValidationError
import java.sql.Date

class DateInput(
  required: Boolean,
  attrs: MetaData = Null) extends Widget(required, attrs) {

  def render(name: String, value: Seq[String], attrList: MetaData = Null) = {
    <input type="text" name={ name } value="mm/dd/yyyy" class="datepicker">{ if (value.isEmpty) "" else value(0) }</input>
  }
  
  override def scripts: NodeSeq = 
  <script>
    $(function() {{
      $( '.datepicker' ).datepicker();
    }});
  </script>
}