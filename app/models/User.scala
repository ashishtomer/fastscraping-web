package models

import play.api.libs.json.{Json, OFormat}

case class User(email: String,
                password: String,
                registered: Boolean, // true only after user confirms registration from email link
                firstName: Option[String] = None,
                lastName: Option[String] = None,
                username: Option[String] = None,
                contact: Option[String] = None)

object User extends ((String, String, Boolean, Option[String], Option[String], Option[String], Option[String]) => User) {
  val format: OFormat[User] = Json.format[User]
}
