package models.lockers

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users._
import models.courses._
import util._
import util.Helpers._

abstract class Location 
object Location {
  val validHalls = List("NE","NW","SE","SW","CE","CW","ANNEX", "YPAS","YA")
  val hallMap = Map(
      "north-east" -> "NE",
      "north-west" -> "NW",
      "south-east" -> "SE",
      "south-west" -> "SW",
      "center-east" -> "CE",
      "center-west" -> "CW",
      "annex" -> "ANNEX",
      "YPAS" -> "YPAS",
      "YPAS annex" -> "YA"
      )
  val abbMap = {for((name, abbreviation) <- hallMap) yield (abbreviation, name)}.toMap
}


case class LockerLocation(val floor: Int, val hall: String) extends Location {
  override def toString = "Floor " + floor + "- " + Location.abbMap(hall)
}
case class RoomLocation(val floor: Int, val hall: String, val name: String) extends Location {
  override def toString = "Room " + name + "- " + Location.abbMap(hall)
  def toLockerLocation = LockerLocation(floor, hall)
}

object RoomLocation {
  def makeRoomLoc(room: String) = {
    if(room.contains("Y")) {
      if(room.contains("A")) {
        RoomLocation(0, "YA", room)
      } else {
        if(room.contains("1")) {
          RoomLocation(1, "YPAS", room)
        } else {
          RoomLocation(1, "YPAS", room)
        }
      }
    }
    val floor = room.toInt / 100
    val roomNumber = room.toInt
    if(floor == 1) {
      if(range(100, 108)(roomNumber)) RoomLocation(1, "SE", room)
      else if(range(106, 119)(roomNumber)) RoomLocation(1, "NE", room)
      else if(range(120, 125)(roomNumber)) RoomLocation(1, "CE", room)
      else if(range(126, 129)(roomNumber)) RoomLocation(1, "CW", room)
      else if(range(130, 134)(roomNumber)) RoomLocation(1, "NW", room)
      else if(range(135, 140)(roomNumber))RoomLocation(1, "SW", room)
      else RoomLocation(1, "ANNEX", room)
    } else if(floor == 2) {
      if(range(200, 208)(roomNumber)) RoomLocation(2, "SE", room)
      else if(range(209, 219)(roomNumber)) RoomLocation(2, "NE", room)
      else if(range(220, 225)(roomNumber)) RoomLocation(2, "CE", room)
      else if(range(226, 229)(roomNumber)) RoomLocation(2, "CW", room)
      else if(range(230, 234)(roomNumber)) RoomLocation(2, "NW", room)
      else if(range(235, 240)(roomNumber)) RoomLocation(2, "SW", room)
      else RoomLocation(2, "ANNEX", room)
    } else if(floor == 3) {
      if(range(300, 308)(roomNumber)) RoomLocation(3, "SE", room)
      else if(range(309, 319)(roomNumber)) RoomLocation(3, "NE", room)
      else if(range(320, 324)(roomNumber)) RoomLocation(3, "CE", room)
      else if(range(325, 329)(roomNumber)) RoomLocation(3, "CW", room)
      else if(range(330, 334)(roomNumber)) RoomLocation(3, "NW", room)
      else if(range(334, 340)(roomNumber)) RoomLocation(3, "SW", room)
      else RoomLocation(3, "ANNEX", room)
    } else if(floor == 4) {
      RoomLocation(4, "CE", room)
    } else {
      RoomLocation(1, "YPAS", room)
    }
  }
  
  def range(start: Int, end: Int): Int => Boolean = 
    (i) => i >= start && i <= end
}