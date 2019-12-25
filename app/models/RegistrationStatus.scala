package models

import java.sql.Date

import play.api.libs.json.{Json, OFormat}

case class RegistrationStatus(email: String,
                              status: String,
                              registrationLink: String,
                              registrationTime: Date)

object RegistrationStatus extends ((String, String, String, Date) => RegistrationStatus) {

  def apply(email: String, status: String, registrationLink: String, registrationTime: java.util.Date): RegistrationStatus =
    new RegistrationStatus(email, status, registrationLink, new Date(registrationTime.getTime))

  def apply(email: String, status: String, registrationLink: String): RegistrationStatus =
    new RegistrationStatus(email, status, registrationLink, new Date(System.currentTimeMillis()))

  implicit val format: OFormat[RegistrationStatus] = Json.format[RegistrationStatus]

  lazy val EMAIL_CONFIRMATION_SENT = "EMAIL_CONFIRMATION_SENT"
  lazy val EMAIL_CONFIRMED = "EMAIL_CONFIRMED"
}
