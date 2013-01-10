package models.grades

import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile
import util.DataStore
import models.users.Student //or just models.users?

@PersistenceCapable(detachable="true")
class Grade {
  @PrimaryKey
  @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
  private[this] var _id: Long = _
  private[this] var _student: Student = _
  private[this] var _assignment: Assignment = _
  private[this] var _points: Double = _
  
  def this(student: Student, assignment: Assignment, points: Double) = {
    this()
    _student = student
    _assignment = assignment
    _points = points
  }
  
  def id: Long = _id
  
  def student: Student = _student
  def student_=(theStudent: Student) { _student = theStudent }
  
  def assignment: Assignment = _assignment
  def assignment_=(theAssignment: Assignment) { _assignment = theAssignment }
  
  def points: Double = _points
  def points_=(thePoints: Double) { _points = thePoints }
}