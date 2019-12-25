package models

import play.api.libs.json.{Json, OFormat}

//TODO Add a boolean field 'registered'. After user confirms through link in email, Set that to true in DB
case class User(email: String,
                password: String,
                firstName: Option[String] = None,
                lastName: Option[String] = None,
                username: Option[String] = None,
                contact: Option[String] = None)

object User extends ((String, String, Option[String], Option[String], Option[String], Option[String]) => User) {
  val format: OFormat[User] = Json.format[User]
}
