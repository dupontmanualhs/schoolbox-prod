package models.grades

import java.sql.Date
import javax.jdo.annotations._
import org.datanucleus.api.jdo.query._
import org.datanucleus.query.typesafe._
import util.ScalaPersistenceManager
import util.PersistableFile
import util.DataStore

@PersistenceCapable(detachable="true")
class Assignment {
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
	private[this] var _id: Long = _
	private[this] var _dateDue: java.sql.Date = _
	private[this] var _datePosted: java.sql.Date = _
	private[this] var _name: String = _
	private[this] var _points: Int = _
}