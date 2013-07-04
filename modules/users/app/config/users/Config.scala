package config.users

import forms.Call
import scala.xml.NodeSeq
import models.users.Role

trait Config {
  def defaultCall: Call
  def mainTemplate: MainTemplate
  def menuBuilder: (Option[Role] => NodeSeq)
}