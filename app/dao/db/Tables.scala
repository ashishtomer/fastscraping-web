package dao.db

import models.{RegistrationStatus, User, UserSession}
import slick.jdbc.H2Profile.api._
import slick.lifted.Tag
import slick.sql.SqlProfile.ColumnOption.NotNull

class UsersTable(tag: Tag) extends Table[User](tag, "users") {

  def email: Rep[String] = column[String]("email", O.PrimaryKey)
  def password: Rep[String] = column[String]("password")
  def registered: Rep[Boolean] = column[Boolean]("registered")
  def firstName: Rep[Option[String]] = column[Option[String]]("first_name")
  def lastName: Rep[Option[String]] = column[Option[String]]("last_name")
  def username: Rep[Option[String]] = column[Option[String]]("username", O.Unique)
  def contact: Rep[Option[String]] = column[Option[String]]("contact")

  override def * = (email, password, registered, firstName, lastName, username, contact) <> (User.tupled, User.unapply)
}

class RegistrationStatusTable(tag: Tag) extends Table[RegistrationStatus](tag, "registration_status") {
  def email: Rep[String] = column[String]("email", O.Unique)
  def status: Rep[String] = column[String]("status")
  def registrationLink: Rep[String] = column[String]("registration_link", O.PrimaryKey)
  def registrationTime: Rep[Long] = column[Long]("registration_time")

  override def * =
    (email, status, registrationLink, registrationTime) <> (RegistrationStatus.tupled, RegistrationStatus.unapply)
}

class UserSessionTable(tag: Tag) extends Table[UserSession](tag, "user_session") {
  def email: Rep[String] = column[String]("email", NotNull)
  def sessionId: Rep[String] = column[String]("session_id", O.PrimaryKey)
  def startAt: Rep[Long] = column[Long]("start_at", NotNull)
  def endAt: Rep[Option[Long]] = column[Option[Long]]("end_at")

  override def * = (email, sessionId, startAt, endAt) <> (UserSession.tupled, UserSession.unapply)
}
