package com.anstel.exportexcelia

import com.anstel.libutilsscala.{ApplicationParameters, ApplicationProperties, DbServer, MailServer}

object ExportExcelia extends App {

  val applicationProperties = new ApplicationProperties("ExportExcelia.prop")
  println(applicationProperties)

  for {
    applicationParameters <- ApplicationParameters.builder(args)
    dbServer <- DbServer.builder(applicationParameters.db, "mongodb", applicationProperties)
    mailServer <- MailServer.builder("prod", "mail", applicationProperties)
  } yield println("Traitement terminé")

}
