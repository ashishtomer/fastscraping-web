package controllers

import java.util.Date

import actions.OpenActionProvider.OpenAction
import com.google.inject.Inject
import dao.{RegistrationStatusDao, UsersDao}
import javax.mail.{MessagingException, SendFailedException}
import models.RegistrationStatus.EMAIL_CONFIRMATION_SENT
import models.{RegistrationStatus, User}
import org.postgresql.util.PSQLException
import play.api.{Configuration, Logging}
import request.{Login, Signup}
import play.api.mvc._
import service.{EmailService, SessionService}
import utils.ApiMessage._
import utils.Exceptions.SignUpFailedException
import utils.ResponseUtils.{Error, Success}
import utils._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AuthController @Inject()(registartionStatusDao: RegistrationStatusDao,
                               emailService: EmailService)
                              (implicit
                               sessionService: SessionService,
                               usersDao: UsersDao,
                               config: Configuration,
                               ec: ExecutionContext,
                               cc: ControllerComponents) extends AbstractController(cc) with Logging with AccessUtils {
  private val loginActiveTime = config.get[Int]("login.active-time") * 60 * 60

  def logIn = OpenAction.async(parse.json[Login]) { implicit request: Request[Login] =>
    val loginData = request.body

    ValidateUser(loginData.email, loginData.password).flatMap { user =>
      ResetOrCreateNewSession(user.email).map { userSession =>
        Ok(Success(LOGIN_SUCCESS))
          .withCookies(Cookie("session", userSession.sessionId, Some(loginActiveTime), "/", Some(".localhost")))
      }
    }
  }

  /*  def resendConfirmation = Action.async(parse.json[ReconfirmRegistraion]) { implicit request =>
      Future(Ok("Request received"))
    }*/

  def confirmRegistration(registrationId: String): Action[Unit] = OpenAction.async(parse.empty) { _ =>
    val registrationLink = s"${config.get[String]("app.host")}/v1/api/confirm-registration/" + registrationId

    registartionStatusDao.getOptional(registrationLink.trim).map(regStatus => {
      if (regStatus.nonEmpty) {
        val regTime: Long = regStatus.get.registrationTime
        val now: Long = new Date().getTime
        val timeDiff = (now - regTime) / (60 * 1000) % 60 //In minutes
        val status = regStatus.get.status
        val email = regStatus.get.email

        if (status == RegistrationStatus.EMAIL_CONFIRMED) {
          Ok(Success("Your email is already confirmed, please login."))
        } else if (status == RegistrationStatus.EMAIL_CONFIRMATION_SENT && timeDiff <= 30) {
          registartionStatusDao.updateStatus(registrationLink, RegistrationStatus.EMAIL_CONFIRMED)
          usersDao.updateStatus(email, registered = true)

          Ok(Success("You're registered with us. Please login yourself to the fastscraping"))
        } else {
          BadRequest(Error("The link has expired", "Please re-send the email"))
        }
      } else {
        BadRequest(Error("No such record found in our system", "No registration done for this link"))
      }
    })
  }

  def signUp = OpenAction.async(parse.json[Signup]) { implicit request: Request[Signup] =>
    val signUpForm = request.body

    if (signUpForm.isPasswordFormatCorrect && signUpForm.isEmailFormatCorrect) {
      usersDao.notExists(signUpForm.email).flatMap {
        case userNotFound if !userNotFound => Future(BadRequest(Error("User exists")))
        case _ =>
          try {
            sendSignUpMail(s"${signUpForm.firstName} ${signUpForm.lastName}", signUpForm.email)
              .flatMap { registrationLink =>
                setRegistrationStatus(RegistrationStatus(signUpForm.email, EMAIL_CONFIRMATION_SENT, registrationLink))
                  .flatMap { statusSaved =>
                    if (statusSaved == 1) {
                      val hashedPwd = FSEncryption.hashPassword(signUpForm.password)
                      val newUser = User(signUpForm.email, hashedPwd, registered = false)
                      usersDao.insertOne(newUser).map(insertCount =>
                        if (insertCount == 1) {
                          Ok(Success( "Registration done. Check your email and confirm registration"))
                        } else {
                          InternalServerError(Error("Something went wrong on our end. Please try again later."))
                        })
                    } else {
                      Future(InternalServerError(Error("Something went wrong on our end. Please try again later.")))
                    }
                  } recover { //If some SQL error comes during insertion of registration status, handle it
                  case NonFatal(ex: PSQLException)
                    if ex.getLocalizedMessage.contains("duplicate key value violates unique constraint") =>
                    val errorMessage = "The email is already registered in our system"
                    BadRequest(Error(errorMessage))
                  case NonFatal(ex: PSQLException) =>
                    logger.error(s"Unhandled SQL exception ${ex.getLocalizedMessage}")
                    throw ex
                  case NonFatal(ex: Exception) => throw ex
                }
              }
          } catch {
            case NonFatal(ex: SendFailedException) =>
              Future(BadRequest(Error("The email you submitted is not legit. Please provide correct email address.")))
            case NonFatal(ex: MessagingException) =>
              Future(BadRequest(Error("We are not able to deliver the confirmation email. " + ex.getLocalizedMessage)))
            case NonFatal(ex: Exception) => throw ex
          }
      }
    } else if (!signUpForm.isPasswordFormatCorrect) {
      Future(BadRequest(Error("The password format is incorrect")))
    } else if (!signUpForm.isEmailFormatCorrect) {
      Future(BadRequest(Error("The email format is incorrect")))
    } else {
      Future(InternalServerError(Error("Something went wrong. Please report the issue to us at contact@fastscraping.com")))
    }
  }

  private def sendSignUpMail(fullName: String, to: String): Future[String] = {
    val redirectUrl = emailService.getRedirectUrl()
    emailService.sendMail(to, "Confirm your registration", EmailTemplates.confirmSignUp(fullName, redirectUrl))
      .map {
        case false => throw SignUpFailedException(s"Couldn't send the sign up mail to $to")
        case true => redirectUrl
      }
  }

  private def setRegistrationStatus(status: RegistrationStatus) = registartionStatusDao.insertOne(status)

}
