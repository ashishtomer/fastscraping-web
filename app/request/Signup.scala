package request

import play.api.libs.json.{Json, OFormat}

case class Signup(firstName: String, lastName: String, email: String, password: String, agreeToTerms: Boolean) {

  def isEmailFormatCorrect = {
    val emailRegex = ".+@[^\\.]+\\.[^\\.]+"
    email.matches(emailRegex)
  }

  def isPasswordFormatCorrect: Boolean = {
    password.length >= 8 && password.length <= 16
  }
}

object Signup {
  implicit val format: OFormat[Signup] = Json.format[Signup]
}
