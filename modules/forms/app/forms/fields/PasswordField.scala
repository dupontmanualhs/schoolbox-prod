package forms.fields

import forms.validators.ValidationError
import forms.widgets.PasswordInput
import scala.xml._
import forms.Binding

/*
 * PasswordField returns the string the user inputs.
 */
/**
 * Creates a new required PasswordField.
 */
class PasswordField(name: String, showStrength: Boolean = false) extends TextField(name) {
  
  
  /**
   * Sets the widget for the password field.
   */
  override def widget = new PasswordInput(this.required)
  
  override def render(bound: Binding): NodeSeq = {
    val errors = bound.fieldErrors.get(name).map(_.render).getOrElse(NodeSeq.Empty)
    <div class={"control-group " + {if(errors.isEmpty) "" else "error"}}>
      { labelElem(bound.form) }
      <div class="controls">
      	{ helpText.map((text: NodeSeq) => <i data-placement="top" title={text} data-html="true" class="formtooltip icon-question-sign"></i>).getOrElse(NodeSeq.Empty) }
        { asWidget(bound) }
        { errors }
        <br />
        {if (showStrength) {
          <script type="text/javascript">
        	jQuery('#id_newPassword').pstrength();
          </script>
        }}
      </div>  
    </div>
  }
  
}

/**
 * Creates a new optional password field.
 */
class PasswordFieldOptional(name: String) extends TextFieldOptional(name) {
  
  /**
   * Sets the widget for the password field.
   */
  override def widget = new PasswordInput(required=false)
  
}