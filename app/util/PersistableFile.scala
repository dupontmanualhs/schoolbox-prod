package util

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._

@PersistenceCapable(detachable="true")
class PersistableFile {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _name: String = _
  @Column(jdbcType="LONGVARBINARY")
  private[this] var _content: Array[Byte] = _
  
  def this(name: String, content: Array[Byte]) = {
    this()
    _name = name
    _content = content
  }
  
  def id: Long = _id
  
  def name: String = _name
  def name_=(theName: String) { _name = theName }
  
  def content: Array[Byte] = _content
  def content_=(theContent: Array[Byte]) { _content = theContent }
}

trait QPersistableFile extends PersistableExpression[PersistableFile] {
  private[this] lazy val _name: StringExpression = {
    new StringExpressionImpl(this, "_name")
  }
  def name: StringExpression = _name
  
  private[this] lazy val _content: ObjectExpression[Array[Byte]] = {
    new ObjectExpressionImpl[Array[Byte]](this, "_content")
  }
  def content: ObjectExpression[Array[Byte]] = _content
}

object QPersistableFile {
  def apply(parent: PersistableExpression[PersistableFile], name: String, depth: Int): QPersistableFile = {
    new PersistableExpressionImpl[PersistableFile](parent, name) with QPersistableFile
  }
  
  def apply(cls: Class[PersistableFile], name: String, exprType: ExpressionType): QPersistableFile = {
    new PersistableExpressionImpl[PersistableFile](cls, name, exprType) with QPersistableFile
  }

  private[this] lazy val jdoCandidate: QPersistableFile = candidate("this")
  
  def candidate(name: String): QPersistableFile = QPersistableFile(null, name, 5)
  
  def candidate(): QPersistableFile = jdoCandidate
  
  def parameter(name: String): QPersistableFile = QPersistableFile(classOf[PersistableFile], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QPersistableFile = QPersistableFile(classOf[PersistableFile], name, ExpressionType.VARIABLE)
}
