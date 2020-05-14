package controllers

import akka.stream.Materializer
import helpers.FsTestBed
import models.{RegistrationStatus, User, UserSession}
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{Cookie, Headers, Results}
import play.api.test.FakeRequest
import request.{Login, Signup}
import play.api.test.Helpers._
import org.mockito.Mockito._

import scala.concurrent.Future
import org.mockito.Matchers.any
import utils.{ApiMessage, FSEncryption, TimeBasedId}

class AuthControllerSpec extends PlaySpec with FsTestBed with Results {
  implicit var mat: Materializer = null
  override def beforeAll(): Unit = {
    super.beforeAll()
    mat = testApp.materializer
  }

  "AuthController.signUp" should {

    "400 (Bad Request) when non-json data is sent" in {
      val testAuthController = new AuthController(mockedRegistrationStatusDao, mockedEmailService)
      val signupRequest = FakeRequest("POST", "/v1/api/signup", testHeaders, "signupBody")
      val response = testAuthController.signUp().apply(signupRequest)

      status(response) mustEqual 400
    }

    "400 (Bad Request) when data doesn't have all fields of signup form" in {
      val testAuthController = new AuthController(mockedRegistrationStatusDao, mockedEmailService)
      val signupRequest = FakeRequest("POST", "/v1/api/signup", testHeaders, """{"email": "ashish@email.com"}""")
      val response = testAuthController.signUp().apply(signupRequest)

      status(response) mustEqual 400
    }

    "415 (Unsupported data type) when content type is not JSON" in {
      val testAuthController = new AuthController(mockedRegistrationStatusDao, mockedEmailService)
      val missingHeaders = testHeaders.remove(CONTENT_TYPE)
      val signupRequest = FakeRequest("POST", "/v1/api/signup", missingHeaders, """{"email": "ashish@email.com"}""")
      val response = testAuthController.signUp().apply(signupRequest)

      status(response) mustEqual 415
    }

    "200 when correct data is passed" in {
      val rsd = mockedRegistrationStatusDao
      val ems = mockedEmailService
      val ss = mockedSessionService
      val ud = mockedUsersDao

      val requestData = signupBody

      when(ud.notExists(requestData.email)) thenReturn Future.successful(true)
      when(ud.insertOne(any[User])) thenReturn Future.successful(1)
      when(ems.getRedirectUrl()) thenReturn "/api/v1/register/redirect-url-to-be-user"
      when(ems.sendMail(any[String], any[String], any[String])) thenReturn Future.successful(true)
      when(rsd.insertOne(any[RegistrationStatus])) thenReturn Future.successful(1)

      val testAuthController = new AuthController(rsd, ems)(ss, ud, testConfiguration, ec, stubCC)
      val signupRequest = FakeRequest("POST", "/v1/api/signup", testHeaders, requestData)
      val response = testAuthController.signUp().apply(signupRequest)

      status(response) mustEqual 200
    }
  }

  "AuthController.confirmRegistration" should {
    "confirm registration" in {
      val registrationId = "some-random-registration-id"
      val registrationLink = s"${testConfiguration.get[String]("app.host")}/v1/api/confirm-registration/" + registrationId
      val rsd = mockedRegistrationStatusDao
      val ems = mockedEmailService
      val ss = mockedSessionService
      val ud = mockedUsersDao

      val testRegistrationStatus = RegistrationStatus("name@email.com", RegistrationStatus.EMAIL_CONFIRMATION_SENT, registrationId)

      when(rsd.getOptional(registrationLink)) thenReturn Future.successful(Some(testRegistrationStatus))
      when(rsd.updateStatus(registrationId, testRegistrationStatus.status)) thenReturn Future.successful(1)
      when(ud.updateStatus(testRegistrationStatus.email, true)) thenReturn Future.successful(1)

      val testAuthController = new AuthController(rsd, ems)(ss, ud, testConfiguration, ec, stubCC)
      val registrationReq = FakeRequest("GET", s"/v1/api/confirm-registration/$registrationId")
      val response = testAuthController.confirmRegistration(registrationId).apply(registrationReq)

      status(response) mustEqual 200
      contentAsString(response) contains "You're registered with us"
    }

    "inform if registration already done" in {
      val registrationId = "some-random-registration-id"
      val registrationLink = s"${testConfiguration.get[String]("app.host")}/v1/api/confirm-registration/" + registrationId
      val rsd = mockedRegistrationStatusDao
      val ems = mockedEmailService
      val ss = mockedSessionService
      val ud = mockedUsersDao

      val testRegistrationStatus = RegistrationStatus("name@email.com", RegistrationStatus.EMAIL_CONFIRMED, registrationId)

      when(rsd.getOptional(registrationLink)) thenReturn Future.successful(Some(testRegistrationStatus))

      val testAuthController = new AuthController(rsd, ems)(ss, ud, testConfiguration, ec, stubCC)
      val registrationReq = FakeRequest("GET", s"/v1/api/confirm-registration/$registrationId")
      val response = testAuthController.confirmRegistration(registrationId).apply(registrationReq)

      status(response) mustEqual 200
      contentAsString(response) contains "Your email is already confirmed"
    }
  }

  "AuthController.logIn" should {
    "login user" in {
      val loginData = loginBody
      val hashedPassword = FSEncryption.hashPassword(loginData.password)
      val testUser = User(loginData.email, hashedPassword, false, Some("first"), Some("second"), Some("name"))
      val testSessionId = TimeBasedId.get
      val sessionStartTime = TimeBasedId.getNanoTimestamp(testSessionId)
      val testSession = UserSession(loginData.email, testSessionId, sessionStartTime)
      val testLoginTime = testConfiguration.get[Int]("login.active-time") * 60 * 60
      val expectedCookie = Cookie("session", testSessionId, Some(testLoginTime), "/", Some(".localhost"))

      val rsd = mockedRegistrationStatusDao
      val ems = mockedEmailService
      val ss = mockedSessionService
      val ud = mockedUsersDao

      when(ud.selectOne(loginData.email)) thenReturn Future.successful(Some(testUser))
      when(ss.startSession(loginData.email)) thenReturn Future.successful(Some(testSession))

      val testAuthController = new AuthController(rsd, ems)(ss, ud, testConfiguration, ec, stubCC)
      val loginRequest = FakeRequest("POST", "/v1/api/login", testHeaders, loginData)
      val response = testAuthController.logIn().apply(loginRequest)

      status(response) mustEqual 200
      cookies(response).get("session") mustBe Some(expectedCookie)
      contentAsString(response) contains ApiMessage.LOGIN_SUCCESS
    }
  }

  private def loginBody = Login("name@email.com", "mypassword")

  private def signupBody = {
    Signup("Ashish", "Tomer", "ashishtomer@website.com", "1234567890", true)
  }

  private def testHeaders = {
    Headers("Accept" -> "application/json, text/plain, */*", "Content-Type" -> "application/json",
      "X-Requested-With" -> "XMLHttpRequest")
  }
}
