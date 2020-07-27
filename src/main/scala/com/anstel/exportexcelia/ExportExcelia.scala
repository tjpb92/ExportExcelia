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
        from = r._3.fromAddress -> r._3.name,
        to = Seq("wiliam.machi@asfalia.fr", "thierry.baribaud@gmail.com"),
        subject = "Extraction Excalia",
        message = "Ci-joint le fichier de l’extraction des données de production à date",
        attachment = new java.io.File("./Extract.xlsx")
      ), r._3)
    }
    System.exit(0)

  }


}
