package models.lockers

import models.lockers._
import util.Helpers._

object LockerData {
  def hallSorter(locInfo: String): String = {
    if(locInfo.contains("NE")) "NE"
    else if(locInfo.contains("NW")) "NW"
    else if(locInfo.contains("SE")) "SE"
    else if(locInfo.contains("SW")) "SW"
    else if(locInfo.contains("center West")) "CW"
    else if(locInfo.contains("center East")) "CE"
    else throw new Exception("Not valid location")
  }
  
  def floorSorter(locInfo: String): Int = {
    if(locInfo.contains("1st")) 1
    else if(locInfo.contains("2nd")) 2
    else if(locInfo.contains("3rd")) 3
    else throw new Exception("Not valid location")
  }
  
  def locationCreator(locInfo: String) = LockerLocation(floorSorter(locInfo), hallSorter(locInfo))
  
  def randomCombination() = {
    val rand1 = (scala.math.random * 100).toInt
    val rand2 = (scala.math.random * 100).toInt
    val rand3 = (scala.math.random * 100).toInt
    "%02d-%02d-%02d".format(rand1, rand2, rand3)
  }
  
}