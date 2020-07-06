package com.anstel.exportexcelia

import com.anstel.libutilsscala.{ApplicationParameters, ApplicationProperties, DbServer, MailServer}

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
      System.exit(0)
    }
  }


}
