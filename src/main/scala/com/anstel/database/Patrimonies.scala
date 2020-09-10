package com.anstel.database

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{Cursor, MongoConnection}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument}

import com.anstel.export.CustomReader.Patrimony
import com.anstel.libutilsscala.{DbServer, ApplicationParameters}

/**
 * Objet de type Models pour les requétes avec la collection Patrimonies
 *
 * @author William Machi
 *  @version 1.0
 */
object Patrimonies extends Models {

  /**
   * @param connection
   * @param dbServer
   * @return scala future résolu à un object BSONCollection provenant de reactivemongo
   */
  def getCollection(connection: MongoConnection, dbServer: DbServer): Future[BSONCollection] =
      connection.database(dbServer.database).
        map(_.collection("patrimonies"))


  /**
   * Requéte qui renvoie les patrimoines qui ne sont pas géolocalisé
   *
   * @param collection
   * @param applicationParameters
   * @return scala future résolu à une liste case class de type Patrimony
   */
  def getNonGeolocalisePatrimonies(collection: BSONCollection, applicationParameters: ApplicationParameters): Future[List[Patrimony]] = {

    val query = BSONDocument(
      "$and" -> BSONArray (
        BSONDocument("callCenterReferences" -> BSONDocument(
          "$eq" -> applicationParameters.callCenter
        )),
        BSONDocument("complementaryAddresses" -> BSONDocument(
          "$not" -> BSONDocument(
            "$elemMatch" -> BSONDocument(
              "geoLocation" -> BSONDocument(
                "$exists" -> true
              )
            )
          )
        )),
        BSONDocument("addressesFromLots" -> BSONDocument(
          "$not" -> BSONDocument(
            "$elemMatch" -> BSONDocument(
              "geoLocation" -> BSONDocument(
                "$exists" -> true
              )
            )
          )
        )),
      )
    )

    collection.find(query).cursor[Patrimony]().collect[List](maxDocs = -1, err = Cursor.FailOnError[List[Patrimony]]())

  }

  def getPatrimonyByCompanyUid(collection: BSONCollection, applicationParameters: ApplicationParameters): Future[List[Patrimony]] = {

    val query = applicationParameters.clientUuid match {
      case Some(uuid) => BSONDocument(
        BSONDocument("company.uid" -> BSONDocument(
          "$eq" -> uuid
        ))
      )
      case None => {
        println("Identifiant du client pour l'export des patrimoines introuvables")
        BSONDocument("NotExistingFile" -> BSONDocument("$eq" -> 1))
      }
    }

    collection.find(query).cursor[Patrimony]().collect[List](maxDocs = -1, err = Cursor.FailOnError[List[Patrimony]]())

  }

}