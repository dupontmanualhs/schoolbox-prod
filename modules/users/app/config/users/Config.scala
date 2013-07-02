package config.users

import forms.Call

trait Config {
  def defaultCall: Call
  def mainTemplate: MainTemplate
}