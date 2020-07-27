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
      send a(new Mail (
        from = r._3.fromAddress -> "Sender",
        to = r._3.toAddress.split(",").toSeq,
        subject = "Test envoi d'extraction de dev",
        message = s"Ci-joint le fichier de l’extraction des données de dev du ${r._1.begDate} au ${r._1.endDate}",
        attachment = new java.io.File(s"${r._1.path}/${r._1.filename}")
      ), r._3)
    }
    System.exit(0)

  }


}
