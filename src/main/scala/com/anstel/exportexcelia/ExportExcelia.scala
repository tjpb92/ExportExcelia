package com.anstel.exportexcelia


import com.anstel.database._
import com.anstel.export._
import com.anstel.libutilsscala.{ApplicationParameters, ApplicationProperties, DbServer, MailServer}



object ExportExcelia extends App {

  val applicationProperties = new ApplicationProperties("ExportExcelia.prop")
  println(applicationProperties)

  val config: Either[String, (ApplicationParameters, DbServer, MailServer)] = for {
    applicationParameters <- ApplicationParameters.builder(args)
    dbServer <- DbServer.builder(applicationParameters.db, "mongodb", applicationProperties)
    mailServer <- MailServer.builder("prod", "mail", applicationProperties)
  } yield (applicationParameters, dbServer, mailServer)

  // exemple pour detailedTicket getUsersFromTickets
  config match {
    case Left(l) => println(l)
    case Right(r) => Run.runExport(
      r._1,
      r._2,
      DetailedTicket,
      DetailedTicket.getUsersFromTickets,
      CaseClassReader.UsersFromTicketsReader
    )
  }

}
