package app

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class TestLogins extends Specification {
  "run in a browser" in {
    running(TestServer(3333), HTMLUNIT) { browser =>
      browser.goTo("http://localhost:3333/")
      browser.$("#title").getTexts().get(0) must equalTo("Hello Guest")
    }
  }
}