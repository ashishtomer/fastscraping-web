package utils

import dao.UsersDao
import models.{User, UserSession}
import play.api.Configuration
import play.api.mvc.Request
import service.SessionService
import utils.Exceptions.{FsSessionNotResetException, FsUnauthorizedException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait AccessUtils {

  def ValidateUser(email: String, sentPass: String)(implicit usersDao: UsersDao, ec: ExecutionContext): Future[User] = {
    usersDao.selectOne(email).map {
      case Some(user: User) if FSEncryption.checkPassword(sentPass, user.password) => user
      case _ => throw FsUnauthorizedException("Unauthorized user")
    }
  }

  def ResetOrCreateNewSession(email: String, considerExpiredSession: Boolean = false)
                             (implicit
                              configuration: Configuration,
                              request: Request[_],
                              sessionService: SessionService,
                              ec: ExecutionContext): Future[UserSession] = {

    val sessionIdOpt = request.cookies.get("session").map(_.value)
    val loginActiveDuration = configuration.getOptional[Long]("login.active-time").map(_ * 60 * 60 * 1000)

    val sessionFuture = sessionIdOpt match {
      case Some(sessionId: String) =>

        sessionService.getSession(email, sessionId).flatMap {
          case Some(userSession: UserSession) =>
            if(!considerExpiredSession && !userSession.isExpired(loginActiveDuration)) {
              sessionService.resetSession(userSession)
            } else if(considerExpiredSession && userSession.isExpired(loginActiveDuration)) {
              sessionService.resetSession(userSession)
            } else {
              throw FsUnauthorizedException(s"[email=$email] User's session expired. Can't reset it.")
            }

          case None =>
            sessionService.startSession(email)
              .map(_.getOrElse(throw FsSessionNotResetException(s"[email=$email] Couldn't create session")))
        }

      case None =>
        sessionService.startSession(email)
          .map(_.getOrElse(throw FsSessionNotResetException(s"[email=$email] Couldn't create session")))
    }

    sessionFuture recover {
      case NonFatal(ex) => throw ex
    }
  }

}
