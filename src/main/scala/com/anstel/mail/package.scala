package com.anstel

import scala.util.{Try, Success, Failure}

import com.anstel.libutilsscala.MailServer

package object mail {

  implicit def stringToSeq(single: String): Seq[String] = Seq(single)
  implicit def liftToOption[T](t: T): Option[T] = Some(t)

  sealed abstract class MailType
  case object Plain extends MailType
  case object Rich extends MailType
  case object MultiPart extends MailType

  case class Mail(
                   from: (String, String), // (email -> name)
                   to: Seq[String],
                   cc: Seq[String] = Seq.empty,
                   bcc: Seq[String] = Seq.empty,
                   subject: String,
                   message: String,
                   richMessage: Option[String] = None,
                   attachment: Option[(java.io.File)] = None
                 )

  object send {
    def a(mail: Mail, mailServer: MailServer) {
      println("sending mail")
      import org.apache.commons.mail._

      val format =
        if (mail.attachment.isDefined) MultiPart
        else if (mail.richMessage.isDefined) Rich
        else Plain

      val commonsMail: Email = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
        case MultiPart => {
          val attachment = new EmailAttachment()
          attachment.setPath(mail.attachment.get.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(mail.attachment.get.getName)
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
        }
      }

      mail.to foreach (commonsMail.addTo(_))
      mail.cc foreach (commonsMail.addCc(_))
      mail.bcc foreach (commonsMail.addBcc(_))

      // CONFIG
      commonsMail.setHostName(mailServer.host)
      commonsMail.setSmtpPort(mailServer.port)

      mailServer.authentication match {
        case "nossl" => commonsMail.setAuthentication(mailServer.username,mailServer.password)
        case "ssl" => {
          commonsMail.setAuthentication(mailServer.username,mailServer.password)
          commonsMail.setSSLOnConnect(true)
        }
        case _ =>
      }

      Try {
        commonsMail.
          setFrom(mail.from._1, mail.from._2).
          setSubject(mail.subject).
          send()
      } match {
        case Success(i) => println("mail sended")
        case Failure(s) => println(s"Failed. Reason: $s")
      }


    }

  }

}