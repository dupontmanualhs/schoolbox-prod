package models.conferences

import models.conferences._
import models.users._
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import org.scalatest.FunSuite

class ConferencesData extends FunSuite {
	var testUser1 = new User("Xx_Ben420Koby_xX", "Benjamin", Some("Scott"), "Koby", Some("Ben"), Gender.MALE, "benkoby420@gmail.com", "swagyolo")
	var testUser2 = new User("o'bizzle", "Todd", Some(""), "O'Bryan", Some("O'Bizzle"), Gender.MALE, "obizzle@aol.com", "password")
	var testStudent1 = new Student(testUser1, "Koby420", "24601", 11, "Dream Team")
	var testTeacher1 = new Teacher(testUser2, "O'Bizzle Fo Shizzle", "666")
	
	var testDate1 = Date.valueOf("1995-03-14")
	var testDate2 = Date.valueOf("1995-04-14")
	
	var testTime1 = Time.valueOf("00:00:00")
	var testTime2 = Time.valueOf("12:00:00")
	var testTime3 = Time.valueOf("05:00:00")
	
	var timeStamp1 = Timestamp.valueOf("1995-03-14 01:00:00")
	var timeStamp2 = Timestamp.valueOf("1995-03-14 05:00:00")
	
	var testEvent = new Event("Winter Conferences", true)
	
	var testSession1 = new Session(testEvent, testDate1, timeStamp2, Some(timeStamp1), testTime1, testTime2, 10)
	var testSession2 = new Session(testEvent, testDate1, timeStamp2, Some(timeStamp1), testTime1, testTime2, 110)
	var testSession3 = new Session(testEvent, testDate1, timeStamp2, Some(timeStamp1), testTime1, testTime2, 1380)
	
	var testSlot1 = new Slot(testSession1, testTeacher1, testStudent1, testTime3, "Bryan Koby", "benkoby420@gmail.com", "502-144-6464", Some(null), Some(null))
	var testSlot2 = new Slot(testSession2, testTeacher1, testStudent1, testTime3, "Bryan Koby", "benkoby420@gmail.com", "502-144-6464", Some(null), Some(null))
	var testSlot3 = new Slot(testSession3, testTeacher1, testStudent1, testTime3, "Bryan Koby", "benkoby420@gmail.com", "502-144-6464", Some(null), Some(null))
	
	//startTime is testTime3 (05:00:00)
	//slotinterval is 10
	test("models.Conferences.slot.calculateEndTime 5:00 + 10 minutes") {
		assert(testSlot1.calculateEndTime() === Time.valueOf("05:10:00"))
	}
	
	test("models.Conferences.slot.calculateEndTime 5:00 + 1:50 hours") {
		assert(testSlot2.calculateEndTime() === Time.valueOf("06:50:00"))
	}
	
	test("models.Conferences.slot.calculateEndTime 5:00 + 23:00 hours") {
		assert(testSlot3.calculateEndTime() === Time.valueOf("04:00:00"))
	}
}