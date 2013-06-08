package controllers

import play.api.mvc.{ Action, Controller }
import models.blogs._
import play.api.data._
import play.api.data.Forms._
import models.users._
import models.blogs._
import play.api.mvc.Result
import models.users.QPerspective
import forms.fields.TinyMCEField
import forms.fields.TextField
import forms.{ Form, ValidBinding, InvalidBinding, Binding }

import scalajdo.DataStore

//TODO: Actually check permissions where applicable

object Blogs extends Controller {
  val newPost = Form {
    tuple(
      "title" -> nonEmptyText,
      "content" -> text)
  }

  val testEdit = Form {
    "tinymce" -> text
  }

  def editor() = Action { implicit req =>
    Ok(views.html.blogs.editor(testEdit))
  }

  /**
   * List the blogs for a given user. If the user passed is the current user, the user can manage their blog.
   *
   *   @param perspective the user whose blogs to display
   */
  def listUserBlogs(perspectiveOpt: Option[Perspective]) = Action { implicit req =>
    perspectiveOpt match {
      case None => NotFound("That user doesn't exist.")
      case Some(perspective) => DataStore.execute { implicit pm =>
        val cand = QBlog.candidate
        val blogs: List[Blog] = Blog.listUserBlogs(perspective)
        Ok(views.html.blogs.blogs(blogs, perspective))
      }
    }
  }

  def listCurrentUserBlogs() = Action { implicit req =>
    DataStore.execute { pm =>
      Visit.getFromRequest(req).perspective match {
        case Some(per) => {
          Ok(views.html.blogs.blogs(Blog.listUserBlogs(per), per))
        }
        case None => NotFound("You must log in to view your own blogs.")
      }
    }
  }

  def listBlogsByPerspectiveId(id: Long) = Action { implicit req =>
    DataStore.execute { implicit pm =>
      Perspective.getById(id) match {
        case Some(per) => Ok(views.html.blogs.blogs(Blog.listUserBlogs(per), per))
        case None => NotFound("That perspective doesn't exist!")
      }
    }
  }

  class CreatePostForm extends Form {
    val title = new TextField("title")
    val content = new TinyMCEField("content")

    def fields = List(title, content)
  }

  /**
   * Create a new post.
   * If the blog id shown in the url corresponds to a blog, go for it. If not, error.
   */

  def createPost(blogId: Long) = Action { implicit req =>
    val blog = Blog.getById(blogId)
    blog match {
      case None => NotFound("This blog doesn't exist.")
      case Some(b) => {
        Visit.getFromRequest(req).perspective match {
          case Some(p) => {
            if (b.owner.id != p.id) {
              Redirect(routes.Blogs.showBlog(blogId)).flashing("message" -> "You don't have the proper permissions to create a post. Change perspectives?")
            } else {
              val form = new CreatePostForm
              if (req.method == "GET") {
                Ok(views.html.blogs.createPost(b.title, Binding(form)))
              } else {
                checkBindingCreatePostForm(Binding(form, req), b, form)(req)
              }
            }
          }
          case None => Redirect(routes.Users.login()).flashing("message" -> "You must log in to create a blog post.")
        }
      }
    }
  }

  def checkBindingCreatePostForm(binding: Binding, b: Blog, form: CreatePostForm) = Action { implicit req =>
    binding match {
      case ib: InvalidBinding => Ok(views.html.blogs.createPost(b.title, ib))
      case vb: ValidBinding => {
        b.createPost(vb.valueOf(form.title), vb.valueOf(form.content))
        Redirect(routes.Blogs.showBlog(b.id)).flashing("message" -> "New post created!")
      }
    }
  }

  /**
   * Show the control panel for a given blog. Checks to see if the correct user is stored in the session var first.
   *
   *   @param blog the blog to show the control panel for
   */
  def showControlPanel(blog: Blog) = Action { implicit req =>
    Ok(views.html.stub())
  }

  /**
   * Show a post. Check to see if the currently logged-in user is allowed to see the post
   *
   *   @param post the post to be shown
   */
  def showPost(post: Post) = Action { implicit req =>
    Ok(views.html.stub())
  }

  /**
   * Show a blog given only its ID.
   *
   *   @param id the id of the blog to be shown
   */
  def showBlog(id: Long) = Action { implicit req =>
    val blogOpt = Blog.getById(id)
    blogOpt match {
      case None => NotFound("This blog is not found.")
      case Some(blog) => Ok(views.html.blogs.blog(blog, Blog.getPosts(blog)))
    }
  }

  def testSubmit() = Action { implicit req =>
    testEdit.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.blogs.editor(formWithErrors)),
      content => {
        Ok(views.html.blogs.feedback(content))
      })
  }
}

