package models.conferences

import javax.jdo.annotations._

@PersistenceCapable(detachable="true")
class Event {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _isActive: Boolean = _
  @Unique
  private[this] var _name: String = _
  
  def this(name: String, isActive: Boolean) = {
    this()
    _name = name
    _isActive = isActive
  }
  
  def name: String = _name
  def name_=(theName: String) {_name = theName}
  
  def isActive: Boolean = _isActive
  def isActive_=(theActivation: Boolean) {_isActive = theActivation}
}

//TODO: OB Boilerplate stuff