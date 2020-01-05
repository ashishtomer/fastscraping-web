package utils

import play.api.libs.json.{Json, OFormat}

trait ApiMessage

object ApiMessage extends ApiMessage {
    //Success messages
    val LOGIN_SUCCESS = "You've logged in successfully"

    //Client Error messages
    val LOGIN_INCORRECT_MAIL = "User not found with this email"
    val LOGIN_INCORRECT_PASS = "Incorrect password"

    //Server Error messages
    val UNABLE_TO_LOGIN = "We are not able to log you in. Please try after some time"

  def error(msg: String) =
    s"""
       |{
       |  "error": "$msg"
       |}
       |""".stripMargin

  def success(msg: String) =
    s"""
       |{
       |  "success": "$msg"
       |}
       |""".stripMargin

}

object FsSuccess {
  def apply(success: String): String = ApiMessage.success(success)
}

object FsError {
  def apply(error: String): String = ApiMessage.error(error)
}
