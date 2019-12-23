package controllers.request

import play.api.libs.json.{Json, OFormat}

case class ReconfirmRegistraion(email: String, password: String)

object ReconfirmRegistraion {
  implicit val format: OFormat[ReconfirmRegistraion] = Json.format[ReconfirmRegistraion]
}
