package app

import org.scalatest.{ BeforeAndAfterAll, FunSuite }
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.selenium.Firefox
import play.api.test.TestServer
import org.openqa.selenium.firefox.FirefoxDriver
import com.gargoylesoftware.htmlunit.BrowserVersion
import play.api.test.TestBrowser
import org.fluentlenium.adapter.FluentTest
import org.openqa.selenium.WebDriver
import org.fluentlenium.core.filter.FilterConstructor._
import org.scalatest.selenium.WebBrowser
import java.util.concurrent.TimeUnit
import scalajdo.DataStore

class TestLogins extends FunSuite with BeforeAndAfterAll with ShouldMatchers with WebBrowser {
  val server = TestServer(3333)
  val baseUrl = s"http://localhost:${server.port}"
  implicit val driver = new FirefoxDriver()
  
  override def beforeAll() {
    models.TestData.load(false)
    server.start()
  }
  
  override def afterAll() {
    close()
    server.stop()
    DataStore.close()
  }
  
  test("home page has 'Log in' link") {
    goTo(s"$baseUrl/")
    eventually { pageTitle should be === "ABCD eSchool" }
    find(linkText("Log in")) should be ('defined)
  }
  
  test("allow a student to log in with the correct username and password") {
    goTo(s"$baseUrl/")
    eventually { pageTitle should be === "ABCD eSchool" }
    clickOn(linkText("Log in"))
    eventually { pageTitle should be === "Login" }
    cssSelector("#id_username").webElement.sendKeys("john")
    cssSelector("#id_password").webElement.sendKeys("kin123")
    clickOn(cssSelector("button[type=submit]"))
    eventually { pageTitle should be === "ABCD eSchool" }
    find(cssSelector(".alert-success")).map(_.text).get should be === "You have successfully logged in."
    clickOn(linkText("John King (Student)"))
    eventually { find(linkText("Log out")) should be ('defined) }
    clickOn(linkText("Log out"))
    eventually { pageTitle should be === "ABCD eSchool" }
    find(cssSelector(".alert-success")).map(_.text).get should be === "You have been logged out."
  }
  
  test("not allow a user to log in with an incorrect password") {
    goTo(s"$baseUrl/login")
    eventually { pageTitle should be === "Login" }
    cssSelector("#id_username").webElement.sendKeys("john")
    cssSelector("#id_password").webElement.sendKeys("notkin123")
    clickOn(cssSelector("button[type=submit]"))
    // should stay on same page, display error, username is kept, password is cleared
    eventually { find(cssSelector(".alert-error")) should be ('defined) }
    pageTitle should be === "Login"
    find(cssSelector(".alert-error")).map(_.text).get should include ("Incorrect username or password.")
    cssSelector("#id_username").webElement.getAttribute("value") should be === "john"
    cssSelector("#id_password").webElement.getAttribute("value") should be === ""
  }
  /*
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


