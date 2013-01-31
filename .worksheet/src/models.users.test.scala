package models.users
import models.users.Teacher._
import models.users.Student._
object test {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(130); val res$0 = 
  Student.getByUsername("RASHAH01");System.out.println("""res0: Option[models.users.Student] = """ + $show(res$0))}
}