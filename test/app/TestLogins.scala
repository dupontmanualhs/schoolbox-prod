package app

import org.junit.Test
import org.openqa.selenium.chrome._
import play.api.test._
import play.api.test.Helpers._
import org.openqa.selenium.{WebDriver, By}
import org.specs2.mutable.Specification
import org.fluentlenium.core.filter.FilterConstructor._

class TestLogins extends Specification { 
  "in a browser" in {
    running(TestServer(3333), FIREFOX) { browser =>
      browser.goTo("http://localhost:3333/")
      browser.$("title").first.getText must equalTo("JCPS eSchool")
      browser.$("a", withText("Log in")).get(1).click
      browser.$("title").first.getText must equalTo("Login")
      browser.$("#id_username").text("john")
      browser.$("#id_password").text("kin123")
      browser.$("#id_password").submit
      browser.$("title").first.getText must equalTo("JCPS eSchool")
      browser.$("p").first.getText must startWith("You are logged in as John King (Student).")
    }
  }
}