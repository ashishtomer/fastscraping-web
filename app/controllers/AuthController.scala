package controllers

import java.util.Date

import com.google.inject.Inject
import controllers.request.{ReconfirmRegistraion, SignupForm}
import controllers.service.EmailService
import dao.{RegistrationStatusDao, UserDao}
import javax.mail.SendFailedException
import models.RegistrationStatus.EMAIL_CONFIRMATION_SENT
import models.{RegistrationStatus, User}
import play.api.Configuration
import play.api.mvc.{AbstractController, ControllerComponents, Request}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class AuthController @Inject()(userDao: UserDao,
                               registartionStatusDao: RegistrationStatusDao,
                               config: Configuration, cc: ControllerComponents)
                              (implicit val ec: ExecutionContext) extends AbstractController(cc) {

  def logIn = Action(parse.json) { implicit request =>
    Ok("")
  }

  def resendConfirmation = Action.async(parse.json[ReconfirmRegistraion])  { implicit request =>
    Future(Ok("Request received"))
  }

  def confirmRegistration(registrationLink: String): Unit = Action.async { request =>
    registartionStatusDao.getOptional(registrationLink).map(status => {
      if(status.nonEmpty) {
        val regTime: Long = status.get.registrationTime.getTime
        val now: Long = new Date().getTime
        val timeDiff = (now - regTime) / (60 * 1000) % 60 //In minutes

        if(timeDiff <= 30) {
          registartionStatusDao.delete(registrationLink) //TODO Set the 'registered' field to true in "users" table
          Ok("You're registered with us. Please login yourself to the fastscraping")
        } else {
          BadRequest("The link has expired. Please re-send the email")
        }
      } else {
        BadRequest("No such record found in our system. Please register yourself")
      }
    })
  }

  def signUp = Action.async(parse.json[SignupForm]) { implicit request: Request[SignupForm] =>
    val signUpForm = request.body

    if (signUpForm.isPasswordFormatCorrect && signUpForm.isEmailFormatCorrect) {
      userDao.notExists(signUpForm.email).flatMap {
        case userNotFound if !userNotFound => Future(BadRequest("User exists"))
        case _ =>
          try {
            sendSignUpMail(signUpForm.email).flatMap { registrationLink =>
              setRegistrationStatus(RegistrationStatus(signUpForm.email, registrationLink, EMAIL_CONFIRMATION_SENT))
                .flatMap(statusSaved =>
                  if (statusSaved == 1) {
                    val newUser = User(signUpForm.email, signUpForm.password)
                    userDao.insertOne(newUser).map(insertCount =>
                      if (insertCount == 1) {
                        val message =
                          """
                            |{
                            |   "message": "You are registered with us. Please check your email and confirm your registration."
                            |}
                            |""".stripMargin

                        Ok(message)
                      } else {
                        InternalServerError("Something went wrong on our end. Please try again later.")
                      })
                  } else {
                    Future(InternalServerError("Something went wrong on our end. Please try again later."))
                  })
            }
          } catch {
            case NonFatal(ex: SendFailedException) =>
              Future(BadRequest("The email you submitted is not legit. Please provide correct email address."))
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
    EmailService(to, "ashishtomer@zoho.com", "Confirm you registration", registrationLink, "").sendMail
    registrationLink
  }

  private def setRegistrationStatus(status: RegistrationStatus) = registartionStatusDao.insertOne(status)
}
