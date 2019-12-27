package utils

import play.api.libs.json.{Json, OFormat}

case class ApiMessage(success: Option[String] = None, error: Option[String] = None) {
  require(success.isEmpty || error.isEmpty, "ApiMessage can't have both success and error. Provide one.")
  override def toString: String = Json.prettyPrint(Json.toJson(this))
}

object ApiMessage {
  //Success messages
  val LOGIN_SUCCESS = "You've logged in successfully"

  //Client Error messages
  val LOGIN_INCORRECT_MAIL = "User not found with this email"
  val LOGIN_INCORRECT_PASS = "Incorrect password"

  //Server Error messages
  val UNABLE_TO_LOGIN = "We are not able to log you in. Please try after some time"

  implicit val format: OFormat[ApiMessage] = Json.format[ApiMessage]
}
