package dao

import com.google.inject.Inject
import dao.db.{RegistrationStatusTable, UserTable}
import models.RegistrationStatus
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class RegistrationStatusDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                     (implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val Statuses = TableQuery[RegistrationStatusTable]

  def insertOne(status: RegistrationStatus): Future[Int] = db.run(Statuses += status)

  def getOptional(link: String): Future[Option[RegistrationStatus]] = db.run(Statuses.filter(_.registrationLink === link).result.headOption)

  def delete(link: String): Future[Int] = db.run(Statuses.filter(_.registrationLink === link).delete)
}
