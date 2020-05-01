package controllers

import java.util.Date

import com.google.inject.Inject
import controllers.actions.OpenActionProvider.OpenAction
import controllers.request.{Login, Signup}
import controllers.service.{EmailService, SessionService}
import dao.{RegistrationStatusDao, UsersDao}
import javax.mail.{MessagingException, SendFailedException}
import models.RegistrationStatus.EMAIL_CONFIRMATION_SENT
import models.{RegistrationStatus, User, UserSession}
import org.postgresql.util.PSQLException
import play.api.Logging
import play.api.http.ContentTypes
import play.api.mvc._
import utils.ApiMessage._
import utils.ResponseUtils.{Error, Success}
import utils._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AuthController @Inject()(usersDao: UsersDao,
                               registartionStatusDao: RegistrationStatusDao,
                               fsEncryption: FSEncryption,
                               sessionService: SessionService,
                               config: FSConfig)
                              (implicit
                               ec: ExecutionContext,
                               cc: ControllerComponents) extends AbstractController(cc) with Logging {

  def logIn = OpenAction.async(parse.json[Login]) { implicit request: Request[Login] =>

    logger.info("The login received")

    val loginData = request.body
    usersDao.selectOne(loginData.email).flatMap {
      case Some(user: User) =>
        if (fsEncryption.checkPassword(loginData.password, user.password)) {
          sessionService.startSession(user.email).map {
            case Some(userSession: UserSession) =>
              logger.info("The login succedded ... sending response back")
              Ok(ApiMessage.success(LOGIN_SUCCESS))
                .withCookies(Cookie("session", userSession.sessionId, maxAge = Some(config.loginActiveTime), path = "/"))
                .as(ContentTypes.JSON)

            case _ => InternalServerError(ApiMessage.error(UNABLE_TO_LOGIN)).as(ContentTypes.JSON)
          }
        } else {
          Future(BadRequest(ApiMessage.error(LOGIN_INCORRECT_PASS)).as(ContentTypes.JSON))
        }
      case None =>
        Future(BadRequest(ApiMessage.error(LOGIN_INCORRECT_MAIL)).as(ContentTypes.JSON))
    }
  }

  /*  def resendConfirmation = Action.async(parse.json[ReconfirmRegistraion]) { implicit request =>
      Future(Ok("Request received"))
    }*/

  def confirmRegistration(registrationId: String): Action[Unit] = OpenAction.async(parse.empty) { _ =>
    val registrationLink = "https://www.fastscraping.com/v1/api/confirm-registration/" + registrationId

    logger.info(s"Going to validate the link $registrationLink")

    registartionStatusDao.getOptional(registrationLink.trim).map(regStatus => {
      if (regStatus.nonEmpty) {
        val regTime: Long = regStatus.get.registrationTime
        val now: Long = new Date().getTime
        val timeDiff = (now - regTime) / (60 * 1000) % 60 //In minutes
        val status = regStatus.get.status
        val email = regStatus.get.email

        if (status == RegistrationStatus.EMAIL_CONFIRMED) {
          Ok(FsSuccess("Your email is already confirmed, please login."))
        } else if (status == RegistrationStatus.EMAIL_CONFIRMATION_SENT && timeDiff <= 30) {
          registartionStatusDao.updateStatus(registrationLink, RegistrationStatus.EMAIL_CONFIRMED)
          usersDao.updateStatus(email, registered = true)

          Ok(FsSuccess("You're registered with us. Please login yourself to the fastscraping"))
        } else {
          BadRequest(FsError("The link has expired. Please re-send the email"))
        }
      } else {
        BadRequest("No such record found in our system. Please register yourself")
      }
    })
  }

  def signUp = OpenAction.async(parse.json[Signup]) { implicit request: Request[Signup] =>
    val signUpForm = request.body

    if (signUpForm.isPasswordFormatCorrect && signUpForm.isEmailFormatCorrect) {
      usersDao.notExists(signUpForm.email).flatMap {
        case userNotFound if !userNotFound => Future(BadRequest("User exists"))
        case _ =>
          try {
            sendSignUpMail(signUpForm.email).flatMap { registrationLink =>
              setRegistrationStatus(RegistrationStatus(signUpForm.email, EMAIL_CONFIRMATION_SENT, registrationLink))
                .flatMap { statusSaved =>
                  if (statusSaved == 1) {
                    val hashedPwd = fsEncryption.hashPassword(signUpForm.password)
                    val newUser = User(signUpForm.email, hashedPwd, registered = false)
                    usersDao.insertOne(newUser).map(insertCount =>
                      if (insertCount == 1) {
                        val message = "Registration done. Check your email and confirm registration."
                        Ok(Success(message).json)
                      } else {
                        InternalServerError(Error("Something went wrong on our end. Please try again later.").json)
                      })
                  } else {
                    Future(InternalServerError(Error("Something went wrong on our end. Please try again later.").json))
                  }
                } recover { //If some SQL error comes during insertion of registration status, handle it
                case NonFatal(ex: PSQLException)
                  if ex.getLocalizedMessage.contains("duplicate key value violates unique constraint") =>
                  val errorMessage = "The email is already registered in our system"
                  BadRequest(Error(errorMessage).json)
                case NonFatal(ex: PSQLException) =>
                  logger.info(s"Unhandled SQL exception ${ex.getLocalizedMessage}")
                  throw ex
                case NonFatal(ex: Exception) => throw ex
              }
            }
          } catch {
            case NonFatal(ex: SendFailedException) =>
              Future(BadRequest("The email you submitted is not legit. Please provide correct email address."))
            case NonFatal(ex: MessagingException) =>
              Future(BadRequest("We are not able to deliver the confirmation email. " + ex.getLocalizedMessage))
            case NonFatal(ex: Exception) => throw ex
          }
      }
    } else if (!signUpForm.isPasswordFormatCorrect) {
      Future(BadRequest("The password format is incorrect"))
    } else if (!signUpForm.isEmailFormatCorrect) {
      Future(BadRequest("The email format is incorrect"))
    } else {
      Future(InternalServerError("Something went wrong. Please report the issue to us at contact@fastscraping.com"))
    }
  }

  private def sendSignUpMail(to: String): Future[String] = Future {
    val registrationLink = EmailService.getRegistarionLink
    Future(EmailService(to, "ashishtomer@zoho.com", "Confirm you registration", registrationLink, "localhost").sendMail)
    registrationLink
  }

  private def setRegistrationStatus(status: RegistrationStatus) = registartionStatusDao.insertOne(status)

}
