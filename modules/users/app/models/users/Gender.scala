package models.users

import org.datanucleus.store.types.converters.TypeConverter
object Gender extends Enumeration {
  type Gender = Value
  val Female = Value(0, "Female")
  val Male = Value(1, "Male")
  val NotListed = Value(2, "Not Listed")
}
