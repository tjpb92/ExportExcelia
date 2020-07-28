package com.anstel.exportexcelia

import com.anstel.libutilsscala.{ApplicationParameters, ApplicationProperties, DbServer, MailServer}
import com.anstel.mail._

/**
 * Point d'entré du programme
 */
object ExportExcelia extends App {

  val applicationProperties = new ApplicationProperties("ExportExcelia.prop")
  println(applicationProperties)

  val config: Either[String, (ApplicationParameters, DbServer, MailServer)] = for {
    applicationParameters <- ApplicationParameters.builder(args)
    dbServer <- DbServer.builder(applicationParameters.db, "mongodb", applicationProperties)
    mailServer <- MailServer.builder("prod", "mail", applicationProperties)
  } yield (applicationParameters, dbServer, mailServer)


  config match {
    case Left(l) => println(l)
    case Right(r) => {
      Run.aggregateResults(
        r._1,
        r._2
      )
      val filename = r._1.suffix match {
        case None => r._1.filename
        case Some(suffix) => {
          val name = r._1.filename.split("\\.").head
          val extension = r._1.filename.split('.').tail.head
          s"${name}_${suffix}.${extension}"
        }
      }
      send a(new Mail (
        from = r._3.fromAddress -> "MsgSrv",
        to = r._3.toAddress.split(",").toSeq,
        subject = "Extraction DeclarImmo pour Excelia",
        message = s"Bonjour, \n Ci-joint l'extraction des données DeclarImmo du ${r._1.begDate} au ${r._1.endDate} \n Cordialement \n Le Service Informatique \n",
        attachment = new java.io.File(s"${filename}")
      ), r._3)
    }
    System.exit(0)

  }


}
