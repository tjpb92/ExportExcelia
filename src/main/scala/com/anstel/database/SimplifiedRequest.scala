package com.anstel.database

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{Cursor, MongoConnection}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.bson.BSONDocument

import com.anstel.export.CustomReader.Request
import com.anstel.libutilsscala.{DbServer, ApplicationParameters}

/**
 * Objet de type Models pour les requétes avec la collection SimplifiedRequest
 *
 * @author William Machi
 *  @version 1.0
 */
object SimplifiedRequest extends Models {

  /**
   * @param connection
   * @param dbServer
   * @return scala future résolu à un object BSONCollection provenant de reactivemongo
   */
  def getCollection(connection: MongoConnection, dbServer: DbServer): Future[BSONCollection] =
    connection.database(dbServer.database).
      map(_.collection("simplifiedRequest"))


  /**
   * Requéte qui renvoie les demandent émises depuis l'application mobile entre une date de début et une date de fin
   *
   * @param collection
   * @param applicationParameters
   * @return scala future résolu à une liste case class de type Request
   */
  def getSimplifiedRequestBetween(collection: BSONCollection, applicationParameters: ApplicationParameters): Future[List[Request]] = {

    import collection.aggregationFramework.{ Match, Lookup, Project }

    val query = collection.aggregatorContext[Request](
      Match(BSONDocument(
        "requestDate" -> BSONDocument(
          "$gte" -> applicationParameters.begDate.toString(),
          "$lt" -> applicationParameters.endDate.toString()
        )
      )),
      List(
        Lookup("patrimonies", "linkedEntities.patrimony.uid", "uid", "patrimony"),
        Project(BSONDocument(
          // REQUEST
          "_id" -> 0,
          "uid" ->  1,
          "requestDate" -> 1,
          "requester.name" -> 1,
          // COMPANY
          "patrimony.company.uid" -> 1,
          "patrimony.company.name" -> 1,
          "patrimony.company.holding" -> 1,
          // AGENCIES
          "patrimony.agencies.uid" -> 1,
          "patrimony.agencies.name" -> 1,
          // PATRIMONY
          "patrimony.uid" -> 1,
          "patrimony.ref" -> 1,
          "patrimony.label" -> 1,
          "patrimony.callCenterReferences" -> 1,
        ))
      )
    )

    query.prepared.cursor.collect[List](-1, Cursor.FailOnError[List[Request]]())

  }

}
