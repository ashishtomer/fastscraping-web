package dao.db

import java.sql.Date

import models.{RegistrationStatus, User}
import slick.jdbc.H2Profile.api._
import slick.lifted.Tag

class UserTable(tag: Tag) extends Table[User](tag, "users") {

  def email: Rep[String] = column[String]("email", O.PrimaryKey)
  def password: Rep[String] = column[String]("password")
  def firstName: Rep[Option[String]] = column[Option[String]]("first_name")
  def lastName: Rep[Option[String]] = column[Option[String]]("last_name")
  def username: Rep[Option[String]] = column[Option[String]]("username", O.Unique)
  def contact: Rep[Option[String]] = column[Option[String]]("contact")

  override def * = (email, password, firstName, lastName, username, contact) <> (User.tupled, User.unapply)
}

class RegistrationStatusTable(tag: Tag) extends Table[RegistrationStatus](tag, "registration_status") {
  def email: Rep[String] = column[String]("email", O.Unique)
  def status: Rep[String] = column[String]("status")
  def registrationLink: Rep[String] = column[String]("registration_link", O.PrimaryKey)
  def registrationTime: Rep[Date] = column[Date]("registration_time")

  override def * =
    (email, status, registrationLink, registrationTime) <> (RegistrationStatus.tupled, RegistrationStatus.unapply)
}
