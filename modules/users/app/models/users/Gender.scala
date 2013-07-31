package models.users

object Gender extends Enumeration {
  type Gender = Value
  val Female = Value(0, "Female")
  val Male = Value(1, "Male")
}
