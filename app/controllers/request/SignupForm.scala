package controllers.request

import play.api.libs.json.{Json, OFormat}

case class SignupForm(email: String, password: String, agreeToTerms: Boolean) {

  def isEmailFormatCorrect = {
    val emailRegex = ".+@[^\\.]+\\.[^\\.]+"
    email.matches(emailRegex)
  }

  def isPasswordFormatCorrect: Boolean = {
    password.length >= 8 && password.length <= 16
  }
}

object SignupForm {
  implicit val format: OFormat[SignupForm] = Json.format[SignupForm]
}
