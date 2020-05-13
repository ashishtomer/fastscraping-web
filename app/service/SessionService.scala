package service

import java.util.UUID

import dao.UserSessionDao
import javax.inject.Inject
import models.UserSession
import org.postgresql.util.PSQLException
import play.api.Logging
import utils.Exceptions.{FsDatabaseError, FsSessionNotCreatedException}
import utils.TimeBasedId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class SessionService @Inject()(sessionDao: UserSessionDao)(implicit ec: ExecutionContext) extends Logging {

  def startSession(email: String, retryLimit: Int = 3, retries: Int = 0): Future[Option[UserSession]] = {
    val sessionId = TimeBasedId.get
    val startAt = TimeBasedId.getNanoTimestamp(sessionId)
    val userSession = UserSession(email, sessionId, startAt)

    sessionDao.insertOne(userSession)
      .recoverWith {
        case NonFatal(ex: PSQLException)
          if ex.getLocalizedMessage.contains("duplicate key value violates unique constrain") && retries < retryLimit =>
          logger.warn(s"There has been error in storing session for $email. Session Id not unique. Trying again")
          startSession(email, retryLimit, retries + 1)

        case NonFatal(exception: Exception) =>
          throw FsSessionNotCreatedException(s"Couldn't create a session for $email", exception)
      }
  }

  def getSession(email: String, sessionId: String): Future[Option[UserSession]] = {
    sessionDao.selectOne(email, sessionId)
  }

  def resetSession(userSession: UserSession): Future[UserSession] = {
    val updatedSession = userSession.copy(startAt = System.currentTimeMillis())
    sessionDao.update(updatedSession).map { //Update session creation time
      case updatedRows if updatedRows == 1 => updatedSession
      case _ => throw FsDatabaseError(s"[user=${userSession.email}] [session=${userSession.sessionId}] Couldn't update the session")
    }
  }

}
