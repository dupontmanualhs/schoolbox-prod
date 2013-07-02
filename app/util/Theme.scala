package util

import scala.xml._
import models.users._
import models.users.User

object Theme {
  def themePick(user: Option[User]): NodeSeq = user match {
    case None => <link rel="stylesheet" media="screen" href="/assets/stylesheets/tester.css"/>
    case Some(u) => 
      u.theme match {
        case "default" => <link rel="stylesheet" media="screen" href="/assets/stylesheets/tester.css"/>
        case "night" => <link rel="stylesheet" media="screen" href="/assets/stylesheets/night.css"/>
        case "cyborg" => <link rel="stylesheet" media="screen" href="assets/stylesheets/cyborg.css"/>
        case _ => <link rel="stylesheet" media="screen" href="/assets/stylesheets/tester.css"/>
      }
  }
}