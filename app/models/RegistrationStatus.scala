package models

import java.sql.{Date, Timestamp}

import play.api.libs.json.{Json, OFormat}

case class RegistrationStatus(email: String,
                              status: String,
                              registrationLink: String,
                              registrationTime: Long = System.currentTimeMillis())

object RegistrationStatus extends ((String, String, String, Long) => RegistrationStatus) {

  implicit val format: OFormat[RegistrationStatus] = Json.format[RegistrationStatus]

  lazy val EMAIL_CONFIRMATION_SENT = "EMAIL_CONFIRMATION_SENT"
  lazy val EMAIL_CONFIRMED = "EMAIL_CONFIRMED"
}
