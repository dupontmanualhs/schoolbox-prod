package models.books

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._

@PersistenceCapable(detachable="true")
class Title {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _name: String = _
  private[this] var _author: String = _
  private[this] var _publisher: String = _
  @Unique
  private[this] var _isbn: String = _
  private[this] var _numPages: Int = _
  private[this] var _dimensions: String = _
  private[this] var _weight: Double = _
  // TODO: Add image field
  private[this] var _verified: Boolean = _
  private[this] var _lastModified: java.sql.Date = _
  
  def this(name: String, author: String, publisher: String, isbn: String, numPages: Int,
    dimensions: String, weight: Double, verified: Boolean, lastModified: java.sql.Date) = {
    this()
    _name = name
    _author = author
    _publisher = publisher
    _isbn = isbn
    _numPages = numPages
    _dimensions = dimensions
    _weight = weight
    // _image = image
    _verified = verified
    _lastModified = lastModified
  }

  def id: Long = _id

  def name: String = _name
  def name_=(theName: String) { _name = theName }

  def author: String = _author
  def author_=(theAuthor: String) { _author = theAuthor }

  def publisher: String = _publisher
  def publisher_=(thePublisher: String) { _publisher = thePublisher }

  def isbn: String = _isbn
  def isbn_=(theIsbn: String) { _isbn = theIsbn }

  def numPages: Int = _numPages
  def numPages_=(theNumPages: Int) { _numPages = theNumPages }

  def dimensions: String = _dimensions
  def dimensions_=(theDimensions: String) { _dimensions = theDimensions }

  def weight: Double = _weight
  def weight_=(theWeight: Double) { _weight = theWeight }

  def verified: Boolean = _verified
  def verified_(theVerified: Boolean) { _verified = theVerified }

  def lastModified: java.sql.Date = _lastModified
  def lastModified_=(theLastModified: java.sql.Date) { _lastModified = theLastModified }
}
