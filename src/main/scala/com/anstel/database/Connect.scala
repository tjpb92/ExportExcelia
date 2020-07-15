package com.anstel.database


import scala.concurrent.Future

import reactivemongo.api.{AsyncDriver, MongoConnection}
import reactivemongo.core.nodeset.Authenticate

import com.anstel.libutilsscala.DbServer

/**
 * Classe définissant l'objet Connect
 *
 * @author William Machi
 * @version 1.0
 */
object Connect {

  /**
   * Fait la connection avec la base de données mongo
   *
   * @param dbServer
   * @return scala future résolu à un object MongoConnection provenant de reactivemongo
   */
  def connection(dbServer: DbServer): Future[MongoConnection] = {
    val driver = new AsyncDriver
    driver.connect(
      List(dbServer.host + ":" + dbServer.port.toString() ),
      authentications = List(Authenticate(dbServer.database, dbServer.username, Some(dbServer.password)))
    )
  }

}
