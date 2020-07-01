package com.anstel.database


import scala.concurrent.Future
import reactivemongo.api.MongoConnection
import reactivemongo.api.bson.collection.BSONCollection

import com.anstel.libutilsscala.DbServer

/**
 *  Trait étendu par les objets qui implementent les requétes
 *
 * @author William Machi
 * @version 1.0
 */
trait Models {
  /**
   *
   * @param connection
   * @return scala future résolu à un object BSONCollection provenant de reactivemongo
   */
  def getCollection(connection: MongoConnection, dbServer: DbServer): Future[BSONCollection]
}
