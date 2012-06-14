package controllers

import play.api.mvc.Controller
import util.DbAction
//import models.blogs._
import util.ScalaPersistenceManager
import util.DbRequest
import play.api.data._
import play.api.data.Forms._


object Blogs extends Controller {
  val newPost = Form {
    tuple(
      "title" -> nonEmptyText,
      "content" -> text,
      "tags" -> text
    )
  }

  val testEdit = Form {
    "tinymce" -> text
  }

  def editor() = DbAction { implicit req =>
    Ok(views.html.blogs.editor(testEdit))
  }

  def testSubmit() = DbAction { implicit req =>
    testEdit.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.blogs.editor(formWithErrors)),
      content => {
        Ok(views.html.blogs.feedback(content))
      }
    )
  }
}
