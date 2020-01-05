package controllers.service

import java.util.{Date, Properties, UUID}

import javax.mail._
import javax.mail.internet._

class EmailService(to: List[String],
                   cc: List[String] = List.empty[String],
                   bcc: List[String] = List.empty[String],
                   from: String,
                   subject: String,
                   content: String,
                   smtpHost: String) {

  require((to.nonEmpty || cc.nonEmpty || bcc.nonEmpty), "No recipient is passed for email")
  require(from != null && from.trim.length > 0, "Please pass a value for sender")

  private def prepareMessage: Message = {
    val properties = new Properties()
    properties.put("mail.smtp.host", smtpHost)
    val session = Session.getInstance(properties,null)
    new MimeMessage(session)
  }

  private def setRecipients(message: Message): Unit = {
    val toAddresses = InternetAddress.parse(to.mkString(",")).asInstanceOf[Array[Address]]
    val ccAddresses = InternetAddress.parse(cc.mkString(",")).asInstanceOf[Array[Address]]
    val bccAddresses = InternetAddress.parse(bcc.mkString(",")).asInstanceOf[Array[Address]]

    message.setRecipients(Message.RecipientType.TO, toAddresses)
    message.setRecipients(Message.RecipientType.CC, ccAddresses)
    message.setRecipients(Message.RecipientType.BCC, bccAddresses)
  }

  private def setSender(message: Message): Unit = {
    message.setFrom(new InternetAddress(from))
  }

  private def setSentDateSubjectContent(message: Message): Unit = {
    message.setSentDate(new Date())
    message.setSubject(subject)
    message.setText(content)
  }

  def sendMail: Unit = {
    val message = prepareMessage
    setRecipients(message)
    setSender(message)
    setSentDateSubjectContent(message)

    Transport.send(message)
  }
}

object EmailService {
  def apply(to: List[String], from: String, subject: String, content: String, smtpHost: String): EmailService =
    new EmailService(to, List[String](), List[String](), from, subject, content, smtpHost)

  def apply(to: String, from: String, subject: String, content: String, smtpHost: String): EmailService =
    new EmailService(List[String](to), List[String](), List[String](), from, subject, content, smtpHost)

  def getRegistarionLink = "https://www.fastscraping.com/v1/api/confirm-registration/" + UUID.randomUUID().toString

  def registrationConfirmationBody(linkReferral: String) =
    s"""
      |Please follow the following link to confirm your registration: $linkReferral
      |This email will be valid for 30 minutes
      |""".stripMargin
}
