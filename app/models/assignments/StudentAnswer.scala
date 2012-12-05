package models.assignments

import javax.jdo.annotations._
import util.Format
import util.Helpers.string2elem
import scala.xml.Elem

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
abstract class StudentAnswer {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _format: Int = _
  private[this] var _question: DbQuestion = _
  @Column(jdbcType="CLOB")
  private[this] var _content: String = _
  
  def id: Long = _id
  
  def format: Format = Format.fromInt(_format)
  def format_=(theFormat: Format) { _format = theFormat.toInt }
  
  def question: DbQuestion = _question
  def question_=(theQuestion: DbQuestion) { _question = theQuestion }
  
  protected def content: Elem = string2elem(_content)
  protected def content_=(xml: Elem) {
    _content = xml.toString
  }
}