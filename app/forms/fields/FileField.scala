package forms.fields

import forms.widgets._
import forms.validators.ValidationError
import play.api.mvc.MultipartFormData.FilePart

abstract class BaseFileField[T](name: String)(implicit man: Manifest[T]) extends Field[T](name){
  override def widget = new FileInput(required)
  
  def asValue(s: Seq[String]): Either[ValidationError, T] = Left(ValidationError("How did you even???"))
  
  def cleanFiles(files: Seq[FilePart[_]]): Either[ValidationError, Any]={
    checkFileRequired(files).fold(
        Left(_), validate(_))
  }
  
  def validate(files: Seq[FilePart[_]]): Either[ValidationError, Seq[FilePart[_]]] = Right(files)
  
  def checkFileRequired(files: Seq[FilePart[_]]): Either[ValidationError, Seq[FilePart[_]]]={
    files.filter(_.key == this.name) match{
      case Seq() => if(this.required) Left(ValidationError(errorMessages("required")))
    		  		else Right(Seq())
      case Seq(fs@_*) => Right(fs)
    }
  }
}

class FileField(name: String) extends BaseFileField[Seq[FilePart[_]]](name)

class FileFieldOptional(name: String) extends BaseFileField[Option[Seq[FilePart[_]]]](name) {
  override def required = false
}