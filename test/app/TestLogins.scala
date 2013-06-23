package app

import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.selenium.WebBrowser
import play.api.test.TestServer
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import com.gargoylesoftware.htmlunit.BrowserVersion

class TestLogins extends FunSuite with BeforeAndAfter with ShouldMatchers with WebBrowser {
  val server = TestServer(3333)
  implicit val webDriver = new HtmlUnitDriver(BrowserVersion.FIREFOX_17)
  
  before {
    server.start()
    webDriver.setJavascriptEnabled(true)
  }
  
  after {
    server.stop()
  }
  
  test("home page has 'Log in' link") {
    goTo("http://localhost:3333/")
    pageTitle should be === "ABCD eSchool"
  }
  
  /*
  test("allow a student to log in with the correct username and password") {
    running(TestServer(3333), driver) { browser =>
      browser.goTo("http://localhost:3333")
      assert(browser.title === "JCPS eSchool")
      browser.$("a", withText("Log in")).get(1).click
      assert(browser.title === "Login")
      browser.$("#id_username").first.text("john")
      browser.$("#id_password").first.text("kin123")
      browser.$("form").first.submit
      assert(browser.title === "JCPS eSchool")
      assert(browser.$("p").first.getText.startsWith("You are logged in as John King (Student)."))
      DataStore.close()
    }
  }

  test("not allow a user to log in with an incorrect password") {
    running(TestServer(3333), driver) { browser =>
      browser.goTo("http://localhost:3333/login")
      browser.$("#id_username").text("john")
      browser.$("#id_password").text("notkin123")
      browser.$("#id_password").submit
      // should stay on same page, display error, username is kept, password is cleared
      assert(browser.title === "Login")
      assert(browser.$(".errorlist").first.getText.contains("Incorrect username or password."))
      assert(browser.$("#id_username").first.getValue === "john")
      assert(browser.$("#id_password").first.getValue === "")
      DataStore.close()
    }
  }
  
  test("make a user with multiple perspectives choose one") {
    running(TestServer(3333), driver) { browser =>
      browser.goTo("http://localhost:3333/login")
      browser.$("#id_username").text("todd")
      browser.$("#id_password").text("obr123")
      browser.$("#id_password").submit
      assert(browser.title === "Choose Perspective")
      browser.$("#id_perspective").click
      browser.$("#id_perspective").submit
      // return to same page with error
      assert(browser.title === "Choose Perspective")
      assert(browser.$(".errorlist").first.getText.contains("This field is required. Please choose a value."))
      DataStore.close()
    }
  }
  
  test("user can change his/her password") {
    running(TestServer(3333), driver) { browser =>
      browser.goTo("http://localhost:3333/login")
      browser.$("#id_username").text("todd")
      browser.$("#id_password").text("obr123")
      browser.$("#id_password").submit
      assert(browser.title === "Choose Perspective")
      browser.$("#id_perspective").click
      browser.$("#id_perspective").submit
      browser.$("#menu_account").click
      browser.$("#menu_changePassword").click
      assert(browser.title === "Change Your Password")
      browser.$("#id_currentPassword").text("obr123")
      browser.$("#id_newPassword").text("obr456")
      browser.$("#id_verifyNewPassword").text("obr456")
      browser.$("#id_verifyNewPassword").submit
    }
  }
  
  test("user with permission can change others' passwords") {
    
  } 
  */
}


