package dao

import com.google.inject.Inject
import dao.db.UserTable
import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]  {
  import profile.api._
  private val Users = TableQuery[UserTable]

  def selectOne(email: String): Future[Option[User]] = db.run(Users.filter(_.email === email).result.headOption)

  def selectAll: Future[Seq[User]] = db.run(Users.result)

  def insertOne(user: User): Future[Int] = db.run(Users += user)

  def notExists(email: String) = db.run(Users.filter(_.email === email).countDistinct.result).map(_ == 0)
}
