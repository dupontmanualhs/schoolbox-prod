package app

import org.specs2.mutable._
import org.openqa.selenium.chrome._

import play.api.test._
import play.api.test.Helpers._

class TestLogins extends Specification {
  "run in a browser" in {
    System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, "/opt/chromedriver")
    running(TestServer(3333), classOf[ChromeDriver]) { browser =>
      browser.goTo("http://localhost:3333/")
      browser.$("title").first.getText must equalTo("JCPS eSchool")
    }
  }
}