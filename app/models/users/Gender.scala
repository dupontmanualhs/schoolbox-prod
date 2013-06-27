package models.users

import scalajdo.converters.EnumerationConverter

object Gender extends Enumeration {
  type Gender = Value
  val Female = Value(0, "Female")
  val Male = Value(1, "Male")
}

class GenderConverter extends EnumerationConverter(Gender)