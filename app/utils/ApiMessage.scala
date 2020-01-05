package utils

import play.api.libs.json.Json

object ApiMessage {
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
