package models.lockers

import models.lockers._
import util.Helpers._

object LockerData {
  def hallSorter(locInfo: String): String = {
    if(locInfo.contains("NE")) "NE"
    if(locInfo.contains("NW")) "NW"
    if(locInfo.contains("SE")) "SE"
    if(locInfo.contains("SW")) "SW"
    if(locInfo.contains("center West")) "CW"
    if(locInfo.contains("center East")) "CE"
    else throw new Exception("Not valid location")
  }
  
  def floorSorter(locInfo: String): Int = {
    if(locInfo.contains("1")) 1
    if(locInfo.contains("2")) 2
    if(locInfo.contains("3")) 3
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