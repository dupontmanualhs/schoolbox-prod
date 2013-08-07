package models.blogs

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users.Role

import config.users.UsesDataStore

@PersistenceCapable(detachable="true")
@Unique(members=Array("_owner", "_title"))
class Blog extends UsesDataStore {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  def id: Long = _id

  @Column(allowsNull="false")
  private[this] var _title: String = _
  def title: String = _title
  def title_=(theTitle: String) { _title = theTitle }

  @Persistent(defaultFetchGroup="true")
  @Column(allowsNull="false")
  private[this] var _owner: Role = _
  def owner: Role = _owner

  def this(title: String, owner: Role) = {
    this()
    _title = title
    _owner = owner
  }

  def createPost(title: String, content: String) {
    val p = new Post(title, content, this)
    dataStore.pm.makePersistent(p)
  }
}

trait QBlog extends PersistableExpression[Blog] {
  private[this] lazy val _id: NumericExpression[Long] = new NumericExpressionImpl[Long](this, "_id")
  def id: NumericExpression[Long] = _id
  
  private[this] lazy val _title: StringExpression = new StringExpressionImpl(this, "_title")
  def title: StringExpression = _title
  
  private[this] lazy val _owner: ObjectExpression[Role] = new ObjectExpressionImpl[Role](this, "_owner")
  def owner: ObjectExpression[Role] = _owner
}

object QBlog {
  def apply(parent: PersistableExpression[_], name: String, depth: Int): QBlog = {
    new PersistableExpressionImpl[Blog](parent, name) with QBlog
  }
  
  def apply(cls: Class[Blog], name: String, exprType: ExpressionType): QBlog = {
    new PersistableExpressionImpl[Blog](cls, name, exprType) with QBlog
  }
  
  private[this] lazy val jdoCandidate: QBlog = candidate("this")
  
  def candidate(name: String): QBlog = QBlog(null, name, 5)
  
  def candidate: QBlog = jdoCandidate
  
  def parameter(name: String): QBlog = QBlog(classOf[Blog], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QBlog = QBlog(classOf[Blog], name, ExpressionType.VARIABLE)
}

object Blog extends UsesDataStore {
  def listUserBlogs(role: Role): List[Blog] = {
    Role.getById(role.id) match {
      case None => Nil
      case Some(realRole) => {
         val cand = QBlog.candidate
         dataStore.pm.query[Blog].filter(cand.owner.eq(realRole)).executeList()
      }
    }
  }

  def getById(id: Long): Option[Blog] = {
    val cand = QBlog.candidate
    dataStore.pm.query[Blog].filter(cand.id.eq(id)).executeOption()
  }

  def getPosts(blog: Blog): List[Post] = {
    val cand = QPost.candidate
    dataStore.pm.query[Post].filter(cand.blog.eq(blog)).executeList()
  }
}
