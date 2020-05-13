package dao

import com.google.inject.Inject
import dao.db.UserSessionTable
import models.UserSession
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserSessionDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                               (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]  {
  import profile.api._

  val SessionTable = TableQuery[UserSessionTable]

  def insertOne(session: UserSession) = {
    db.run(SessionTable += session).map(inserted =>
      if(inserted == 1) {
        Some(session)
      } else {
        None
      })
  }

  def selectOne(email: String, sessionId: String): Future[Option[UserSession]] = {
    db.run(SessionTable.filter(user => user.email === email && user.sessionId === sessionId).result.headOption)
  }

  def update(userSession: UserSession): Future[Int] = {
    val query = for(session <- SessionTable if session.sessionId === userSession.sessionId) yield session.startAt
    db.run(query.update(userSession.startAt))
  }

  def deleteOne(email: String): Future[Int] = db.run(SessionTable.filter(_.email === email).delete)
}
