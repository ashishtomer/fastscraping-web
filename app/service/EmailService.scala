package service

import java.security.Security
import java.util.{Date, Properties}

import com.google.inject.Inject
import com.sun.net.ssl.internal.ssl.Provider
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, Message, PasswordAuthentication, Session}
import play.api.{Configuration, Logging}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class EmailService @Inject()(configuration: Configuration)(implicit ec: ExecutionContext) extends Logging {
  private val EMAIL_HOST: String = configuration.get[String]("email.host")
  private val EMAIL_PORT: Int = configuration.get[Int]("email.port")
  private val SENDER_EMAIL: String = configuration.get[String]("email.sender");
  private val SENDER_PASSWORD: String = configuration.get[String]("email.auth_pass")

  Security.addProvider(new Provider)

  private val SSL_FACTORY = "javax.net.ssl.SSLSocketFactory"

  private val props = new Properties
  props.put("mail.smtp.host", EMAIL_HOST)
  props.put("mail.smtp.auth", "true")
  props.put("mail.smtp.port", EMAIL_PORT.toString)
  props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY)
  props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY)
  props.setProperty("mail.smtp.socketFactory.fallback", "false")
  props.setProperty("mail.smtp.socketFactory.port", EMAIL_PORT.toString)
  props.put("mail.smtp.startssl.enable", "true")

  private val session = Session.getInstance(props, new Authenticator() {
    override protected def getPasswordAuthentication = new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
  })

  def sendMail(recipient: String, subject: String, body: String): Future[Boolean] = Future{
    try {
      val transport = session.getTransport("smtp")
      transport.connect()

      val msg = new MimeMessage(session)

      msg.setFrom(new InternetAddress(SENDER_EMAIL))
      msg.setRecipients(Message.RecipientType.TO, recipient)
      msg.setSubject(subject)
      msg.setText(body)
      msg.setSentDate(new Date())

      transport.sendMessage(msg, msg.getAllRecipients)
      transport.close()

      true
    } catch {
      case NonFatal(ex: Exception) =>
        logger.error(s"Couldn't send the signup email to $recipient", ex)
        false
    }
  }

}