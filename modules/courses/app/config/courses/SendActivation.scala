package config.courses

import com.typesafe.scalalogging.slf4j.Logging
import config.users.UsesDataStore
import models.courses.QGuardian

object SendActivation extends UsesDataStore with Logging {
  import com.typesafe.plugin._
  import play.api.Play.current
  import models.users.{ Activation, User, QUser }
  import models.courses.{ Teacher, QTeacher, TeacherAssignment, QTeacherAssignment, Guardian, Student }

  def toUser(user: User, content: (User, String) => String) {
    user.email match {
      case None => logger.info(s"Activation not sent to ${user.username} because s/he has no email address.")
      case Some(email) => dataStore.execute(pm => {
        logger.info(s"Sending activation email to ${user.username}.")
        val activation = new Activation(user)
        pm.makePersistent(activation)
        val mail = use[MailerPlugin].email
        mail.setSubject("Schoolbox Account Activation")
        mail.addRecipient(email)
        mail.addFrom("support@dupontmanual.org")
        mail.send(content(user, activation.uuid))
      })
    }
  }
  
  def toTeacher(teacher: Teacher) {
    toUser(teacher.user, teacherEmail)
  }
  
  def toAllTeachers() {
    val cand = QTeacher.candidate()
    val taCand = QTeacherAssignment.candidate()
    val userVar = QUser.variable("userVar")
    val teachersWithClasses = dataStore.execute { pm =>
      val teachers = pm.query[Teacher].filter(cand.user.eq(userVar).and(
          userVar.isActive.eq(true))).orderBy(userVar.last.asc, userVar.first.asc).executeList()
      teachers.filterNot(t => pm.query[TeacherAssignment].filter(taCand.teacher.eq(t)).executeList().isEmpty)
    }  
    teachersWithClasses.foreach(t => {
      if (!Activation.getByUser(t.user).isDefined) {
        toTeacher(t)
      }
    })
  }
  
  def toGuardian(guardian: Guardian) {
    toUser(guardian.user, guardianEmail)
  }
  
  def toAllGuardians() {
    val cand = QGuardian.candidate()
    val userVar = QUser.variable("userVar")
    val activeGuardians = dataStore.execute { pm =>
      pm.query[Guardian].filter(cand.user.eq(userVar).and(
        userVar.isActive.eq(true))).orderBy(userVar.last.asc, userVar.first.asc).executeList()
    }
    activeGuardians.foreach { g =>
      if (!Activation.getByUser(g.user).isDefined) {
        toGuardian(g)
      }
    }
  }
  
  def teacherEmail(user: User, uuid: String) =  
    s"""Dear ${user.displayName}:
       |
       |This email is your activation information from Schoolbox, the new
       |conference scheduling system used by duPont Manual High School.
       |
       |To allow parents to schedule conferences using the system, follow
       |the instructions in this email.
       |
       |ACTIVATE YOUR ACCOUNT, CHOOSE PASSWORD, AND LOG IN
       |==================================================
       |
       |1. First, go to this link:
       |
       |https://schoolbox.dupontmanual.org/users/activate/${uuid}
       |
       |This link is specific to your account. Another user's link will not work
       |for you.
       |
       |2. When the page loads, enter your
       |
       |Username: ${user.username}
       |
       |The username is case-sensitive.
       |
       |3. Now enter password you'd like to use into the two fields provided. The 
       |passwords must match and must be at least eight characters long. When
       |you've done this, click the Submit button.
       |
       |4. If all went well, you've been re-directed to the log in page and
       |you've received a message saying that your account has been activated
       |and your password has been set. Enter your username and the password you
       |chose to log in.
       |
       |ACTIVATE CONFERENCES AND RESERVE BREAK TIMES
       |============================================
       |
       |1. Click on the Conferences menu and choose Fall 2013.
       |
       |2. Click on the button labeled "Activate Scheduling". You should see a
       |message indicating that conference scheduling has been activated and you'll
       |see a list of all the times from 7:40-2:20 in ten-minute increments.
       |
       |3. To reserve break times for yourself (lunch and restroom breaks, for
       |example), click the button of the time you'd like to reserve. A new page
       |will open with fields for the appointment. All of these are optional.
       |I suggest you simply put a note (like "Lunch") in the comment field.
       |
       |4. Click on the other times you'd like to reserve. In addition to reserving
       |break time for yourself, you can also schedule actual conferences. Simply
       |use the pull-down list to choose the student and parent you'll be meeting
       |with. (Please do this if parents email you, since they may have trouble
       |accessing the new system.)
       |
       |FINAL NOTES
       |===========
       |
       |At 11:59 PM next Sunday, the system will close. Parents who have not
       |scheduled by then will be unable to schedule a conference using the
       |system. On Monday, you might want to print out your schedule.
       |
       |If you have trouble, email me at todd.obryan@jefferson.kyschools.us
       |and I'll try to help you. If something disastrous happens (like the
       |system goes down), call my room at x2202.
       |
       |If you have suggestions, send those along, too. We may be able to
       |implement some this week, but we'll definitely try to make things
       |better for the spring conferences.
       |
       |Thanks!
       |Todd
     """.stripMargin
  
  def guardianEmail(user: User, uuid: String) =  
    s"""Dear ${user.displayName}:
       |
       |This email is your activation information from Schoolbox, the new
       |conference scheduling system used by duPont Manual High School.
       |
       |Sorry this was so long in coming. This version represents a complete
       |rewrite of the old system. We expect a few glitches, but bear with
       |us.
       |
       |To schedule conferences with your child's teachers, follow
       |the instructions in this email.
       |
       |ACTIVATE YOUR ACCOUNT, CHOOSE PASSWORD, AND LOG IN
       |==================================================
       |
       |1. First, go to this link:
       |
       |https://schoolbox.dupontmanual.org/users/activate/${uuid}
       |
       |This link is specific to your account. Another user's link will not work
       |for you.
       |
       |2. When the page loads, enter your
       |
       |Username: ${user.username}
       |
       |The username is case-sensitive.
       |
       |3. Now enter password you'd like to use into the two fields provided. The 
       |passwords must match and must be at least eight characters long. When
       |you've done this, click the Submit button.
       |
       |4. If all went well, you've been re-directed to the log in page and
       |you've received a message saying that your account has been activated
       |and your password has been set. Enter your username and the password you
       |chose to log in.
       |
       |SCHEDULE CONFERENCES
       |====================
       |
       |1. Click on the Conferences menu and choose Fall 2013.
       |
       |2. You'll see a list of any conferences you've already scheduled, and a list
       |of teachers who aren't using the system.
       |
       |3. At the bottom, you'll see a list of teachers who are using the system,
       |with a button next to each one. Click on the button and choose a time
       |for your conference with that teacher. (Note: only times when both you
       |and the teacher are available appear in the list of choices.)
       |
       |3. Remember to print your schedule before you come to Conference Day.
       |
       |4. Send any support requests to support@dupontmanual.org and we'll try to
       |respond to them as quickly as possible!
       |
       |Thanks!
       |Todd O'Bryan and the Special Topics in Computer Science classes
       |The Schoolbox Team
     """.stripMargin


}
