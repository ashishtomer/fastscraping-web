package controllers.service

import java.sql.Date
import java.util.UUID

import dao.UserSessionDao
import javax.inject.Inject
import models.UserSession
import org.postgresql.util.PSQLException

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SessionService @Inject()(sessionDao: UserSessionDao)(implicit ec: ExecutionContext) {

  def startSession(username: String, retry: Int = 0): Future[Option[UserSession]] = {
    val sessionId = UUID.randomUUID().toString
    val startAt = System.currentTimeMillis()
    val userSession = UserSession(username, sessionId, startAt)

    sessionDao.insertOne(userSession)
      .recoverWith {
        case NonFatal(ex: PSQLException)
          if (ex.getLocalizedMessage.contains("duplicate key value violates unique constrain")) && retry < 3 =>
          println(s"There have been error in storing session for $username. Trying again")
          startSession(username, retry + 1)
        case NonFatal(exception: Exception) => throw exception
      }
  }

}
