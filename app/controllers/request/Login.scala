package controllers.request

import play.api.libs.json.{Json, OFormat}

case class Login(email: String, password: String)

object Login extends ((String, String) => Login) {
  implicit val format: OFormat[Login] = Json.format[Login]
}
