package models.lockers

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users._
import util._
import util.Helpers._

abstract class Location {
  val validHalls = List("NE","NW","SE","SW","CE","CW","ANNEX")
  val hallMap = Map(
      "Northeast" -> "NE",
      "Northwest" -> "NW",
      "Southeast" -> "SE",
      "Southwest" -> "SW",
      "Central East" -> "CE",
      "Central West" -> "CW",
      "Annex" -> "ANNEX"
      )
  val abbMap = {for((name, abbreviation) <- hallMap) yield (abbreviation, name)}.toMap
}
case class LockerLocation(val floor: Int, val hall: String) extends Location {
  require(validHalls.contains(hall), throw new Exception("Not a valid hall."))
  require(1 <= floor && floor <= 3, throw new Exception("Not a valid floor."))
  override def toString = "Floor " + floor + "- " + abbMap(hall)
}
case class RoomLocation(val floor: Int, val hall: String, val number: Int) extends Location {
  require(validHalls.contains(hall), throw new Exception("Not a valid hall."))
  require(1 <= floor && floor <= 4, throw new Exception("Not a valid floor."))
  require(number / 100 == floor, throw new Exception("Number and floor do not match."))
  override def toString = "Room " + number + "- " + abbMap(hall)
}