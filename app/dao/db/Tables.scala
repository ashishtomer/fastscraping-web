package dao.db

import java.sql.Date

import models.{RegistrationStatus, User}
import slick.jdbc.H2Profile.api._
import slick.lifted.Tag

class UserTable(tag: Tag) extends Table[User](tag, "users") {

  def email: Rep[String] = column[String]("EMAIL", O.PrimaryKey)
  def password: Rep[String] = column[String]("PASSWORD")
  def firstName: Rep[Option[String]] = column[Option[String]]("FIRST_NAME")
  def lastName: Rep[Option[String]] = column[Option[String]]("LAST_NAME")
  def username: Rep[Option[String]] = column[Option[String]]("USERNAME")
  def contact: Rep[Option[String]] = column[Option[String]]("CONTACT")

  override def * = (email, password, firstName, lastName, username, contact) <> (User.tupled, User.unapply)
}

class RegistrationStatusTable(tag: Tag) extends Table[RegistrationStatus](tag, "registration_status") {
  def email: Rep[String] = column[String]("EMAIL")
  def status: Rep[String] = column[String]("STATUS", O.PrimaryKey)
  def registrationLink: Rep[String] = column[String]("REGISTRATION_LINK")
  def registrationTime: Rep[Date] = column[Date]("REGISTRATION_TIME")

  override def * =
    (email, status, registrationLink, registrationTime) <> (RegistrationStatus.tupled, RegistrationStatus.unapply)
}
