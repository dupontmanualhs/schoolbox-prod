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
  *   @param user the user whose blogs to display
  */
  def listUserBlogs(user: User) = DbAction { implicit req =>
    Ok(views.html.stub())
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

  /** Show a blog. Check to see if the currently logged-in user is allowed to view the blog
  *
  *   @param blog the blog to be shown
  */
  def showBlogByBlog(blogOpt: Option[Blog]) = DbAction { implicit req =>
    blogOpt match {
      case None => NotFound("This blog does not exist")
      case Some(blog) => {
         implicit val pm: ScalaPersistenceManager = req.pm
         val cand = QPost.candidate()
         val posts: List[Post] = pm.query[Post].filter(cand.blog.eq(blog)).executeList()
         Ok(views.html.blogs.blog(blog, posts))
      }
    }
  }

  /** Show a blog given only its ID. Calls showBlogByBlog. Yay for silly names.
  *
  *   @param id the id of the blog to be shown
  */
  def showBlog(id: Long) = DbAction { implicit req =>
    implicit val pm: ScalaPersistenceManager = req.pm
    val cand = QBlog.candidate()
    val blog = pm.query[Blog].filter(cand.id.eq(id)).executeOption()
    showBlogByBlog(blog)
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
