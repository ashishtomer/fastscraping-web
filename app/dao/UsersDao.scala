package dao

import com.google.inject.Inject
import dao.db.UsersTable
import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UsersDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]  {
  import profile.api._
  private val Users = TableQuery[UsersTable]

  def selectOne(email: String): Future[Option[User]] = db.run(Users.filter(_.email === email).result.headOption)

  def selectAll: Future[Seq[User]] = db.run(Users.result)

  def insertOne(user: User): Future[Int] = db.run(Users += user)

  def notExists(email: String) = db.run(Users.filter(_.email === email).distinct.length.result).map(_ == 0)

  def updateStatus(email: String, registered: Boolean): Future[Int] = {
    val query = for(user <- Users if user.email === email) yield user.registered
    db.run(query.update(registered))
  }
}
