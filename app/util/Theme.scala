package util

import scala.xml._
import models.users._

object Theme {
  def themePick(user: Option[User]): NodeSeq = user match {
    case None => <link rel="stylesheet" media="screen" href="/assets/stylesheets/tester.css"/>
    case Some(u) => 
      u.theme match {
        case "default" => <link rel="stylesheet" media="screen" href="/assets/stylesheets/tester.css"/>
        case "night" => <link rel="stylesheet" media="screen" href="/assets/stylesheets/night.css"/>
        case _ => <link rel="stylesheet" media="screen" href="/assets/stylesheets/tester.css"/>
      }
  }
}