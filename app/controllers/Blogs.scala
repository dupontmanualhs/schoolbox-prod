package controllers

import play.api.mvc.Controller
import util.DbAction
import models.blogs._
import util.ScalaPersistenceManager
import util.DbRequest
import play.api.data._
import play.api.data.Forms._
import models.users._
import models.blogs._
import play.api.mvc.Result
import models.users.QPerspective

//TODO: Actually check permissions where applicable

object Blogs extends Controller {
  val newPost = Form {
    tuple(
      "title" -> nonEmptyText,
      "content" -> text
    )
  }

  val testEdit = Form {
    "tinymce" -> text
  }

  def editor() = DbAction { implicit req =>
    Ok(views.html.blogs.editor(testEdit))
  }


  /** List the blogs for a given user. If the user passed is the current user, the user can manage their blog.
  *
  *   @param perspective the user whose blogs to display
  */
  def listUserBlogs(perspectiveOpt: Option[Perspective]) = DbAction { implicit req =>
    perspectiveOpt match {
      case None => NotFound("That user doesn't exist.")
      case Some(perspective) => {
        implicit val pm: ScalaPersistenceManager = req.pm
        val cand = QBlog.candidate
        val blogs: List[Blog] = Blog.listUserBlogs(perspective)
        Ok(views.html.blogs.blogs(blogs, perspective.user))
      }
    }
  }
  
  def listCurrentUserBlogs() = DbAction { implicit req =>
    val currentUser = User.current
    currentUser match {
      case Some(usr) => {
         implicit val pm: ScalaPersistenceManager = req.pm
         val cand = QPerspective.candidate
         val perspective = pm.query[Perspective].filter(cand.user.eq(usr)).executeList
         perspective match {
           case Nil => NotFound("Somehow, you are logged in as a non-extant user. Welp.")
           case (x :: xs) => Ok(views.html.blogs.blogs(Blog.listUserBlogs(x), x.user))
         }
      }
      case None => NotFound("You must log in to view your own blogs.")
    }
  }

  def listBlogsByPerspectiveId(id: Long) = DbAction { implicit req =>
   implicit val pm: ScalaPersistenceManager = req.pm
   val perOpt = Perspective.getById(id)
   perOpt match {
      case Some(per) => Ok(views.html.blogs.blogs(Blog.listUserBlogs(per), per.user))
      case None => NotFound("That perspective doesn't exist!")
    }
  }
  /** Show the control panel for a given blog. Checks to see if the correct user is stored in the session var first.
  *
  *   @param blog the blog to show the control panel for
  */
  def showControlPanel(blog: Blog) = DbAction { implicit req =>
    Ok(views.html.stub())
  }

  /** Show a post. Check to see if the currently logged-in user is allowed to see the post
  *
  *   @param post the post to be shown
  */
  def showPost(post: Post) = DbAction { implicit req =>
    Ok(views.html.stub())
  }

  /** Show a blog given only its ID.
  *
  *   @param id the id of the blog to be shown
  */
  def showBlog(id: Long) = DbAction {  implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    val blogOpt = Blog.getById(id)
    blogOpt match {
      case None => NotFound("This blog is not found.")
      case Some(blog) => Ok(views.html.blogs.blog(blog, Blog.getPosts(blog)))
    }
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
