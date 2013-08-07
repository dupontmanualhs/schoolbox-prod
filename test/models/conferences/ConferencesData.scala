package models.conferences

import models.conferences._
import models.courses._
import org.joda.time.{ LocalDate, LocalDateTime, LocalTime }
import org.scalatest.FunSuite
import models.users.Gender
import models.users.User

class ConferencesData extends FunSuite {
	val testUser1 = new User("Xx_Ben420Koby_xX", "Benjamin", Some("Scott"), "Koby", Some("Ben"), Gender.Male, "benkoby420@gmail.com", "swagyolo")
	val testUser2 = new User("o'bizzle", "Todd", Some(""), "O'Bryan", Some("O'Bizzle"), Gender.Male, "obizzle@aol.com", "password")
	val testStudent1 = new Student(testUser1, "Koby420", "24601", 11, "Dream Team")
	val testTeacher1 = new Teacher(testUser2, "O'Bizzle Fo Shizzle", "666")
	
	val testDate1 = new LocalDate(1995, 3, 14)
	val testDate2 = new LocalDate(1995, 4, 14)
	
	val testTime1 = new LocalTime(0, 0, 0)
	val testTime2 = new LocalTime(12, 0, 0)
	val testTime3 = new LocalTime(5, 0, 0)
	
	val timeStamp1 = new LocalDateTime(1995, 3, 14, 1, 0, 0)
	val timeStamp2 = new LocalDateTime(1995, 3, 14, 5, 0, 0)
	
	val testEvent = new Event("Winter Conferences", true)
	
	val testSession1 = new Session(testEvent, testDate1, timeStamp2, Some(timeStamp1), testTime1, testTime2)
	val testSession2 = new Session(testEvent, testDate1, timeStamp2, Some(timeStamp1), testTime1, testTime2)
	val testSession3 = new Session(testEvent, testDate1, timeStamp2, Some(timeStamp1), testTime1, testTime2)
	
	/*
	val testSlot1 = new Slot(testSession1, testTeacher1, Set(testStudent1), testTime3, "Bryan Koby", "benkoby420@gmail.com", "502-144-6464", None, None, 10)
	val testSlot2 = new Slot(testSession2, testTeacher1, Set(testStudent1), testTime3, "Bryan Koby", "benkoby420@gmail.com", "502-144-6464", None, None, 110)
	val testSlot3 = new Slot(testSession3, testTeacher1, Set(testStudent1), testTime3, "Bryan Koby", "benkoby420@gmail.com", "502-144-6464", None, None, 1395)
	
	//startTime is testTime3 (05:00:00)
	//slotinterval is 10
	test("models.Conferences.slot.calculateEndTime 5:00 + 10 minutes") {
		assert(testSlot1.endTime === new LocalTime(5, 10, 0))
	}
	
	test("models.Conferences.slot.calculateEndTime 5:00 + 1:50 hours") {
		assert(testSlot2.endTime === new LocalTime(6, 50, 0))
	}
	
	test("models.Conferences.slot.calculateEndTime 5:00 + 23:15 hours") {
		assert(testSlot3.endTime === new LocalTime(4, 15, 0))
	}*/
}