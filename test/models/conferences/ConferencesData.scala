package models.conferences

import models.conferences._
import models.users._
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import org.scalatest.FunSuite


class ConferencesData {
	var testUser1 = new User("Xx_Ben420Koby_xX", "Benjamin", Some("Scott"), "Koby", Some("Ben"), Gender.MALE, "benkoby420@gmail.com", "swagyolo")
	var testUser2 = new User("o'bizzle", "Todd", Some(""), "O'Bryan", Some("O'Bizzle"), Gender.MALE, "obizzle@aol.com", "password")
	var testStudent1 = new Student(testUser1, "Koby420", "24601", 11, "Dream Team")
	var testTeacher1 = new Teacher(testUser2, "O'Bizzle Fo Shizzle", "666")
	
	var testDate1 = Date.valueOf("03/14/1995")
	var testDate2 = Date.valueOf("04/14/1995")
	
	var testTime1 = Time.valueOf("00:00:00")
	var testTime2 = Time.valueOf("12:00:00")
	var testTime3 = Time.valueOf("05:00:00")
	
	var timeStamp1 = Timestamp.valueOf("03/14/1995 01:00:00")
	var timeStamp2 = Timestamp.valueOf("03/14/1995 05:00:00")
	
	var testEvent = new Event("Winter Conferences", true)
	
	var testSession = new Session(testEvent, testDate1, timeStamp2, Some(timeStamp1), testTime1, testTime2, 10)
	
	var testSlot = new Slot(testSession, testTeacher1, testStudent1, testTime3, "Bryan Koby", "benkoby420@gmail.com", "502-144-6464", Some(null), Some(null))
	
}