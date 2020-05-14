package helpers

import dao.{RegistrationStatusDao, UsersDao}
import org.scalatestplus.mockito.MockitoSugar
import service.{EmailService, SessionService}

private[helpers] trait MockedClasses extends MockitoSugar {

  //DAOs
  implicit def mockedRegistrationStatusDao: RegistrationStatusDao = mock[RegistrationStatusDao]
  implicit def mockedUsersDao: UsersDao = mock[UsersDao]

  //Services
  implicit def mockedEmailService: EmailService = mock[EmailService]
  implicit def mockedSessionService: SessionService = mock[SessionService]
}
