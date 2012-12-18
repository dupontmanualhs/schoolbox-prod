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
  
}