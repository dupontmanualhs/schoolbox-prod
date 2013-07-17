package models.users

//import scalajdo.converters.EnumerationConverter
import org.datanucleus.store.types.converters.TypeConverter

object Gender extends Enumeration {
  type Gender = Value
  val Female = Value(0, "Female")
  val Male = Value(1, "Male")
}

//class GenderConverter extends EnumerationConverter(Gender)
class GenderConverter extends TypeConverter[Gender.Value, Int] {
  def toDatastoreType(memberValue: Gender.Value): Int = memberValue.id
  def toMemberType(dataStoreValue: Int): Gender.Value = Gender(dataStoreValue)
}