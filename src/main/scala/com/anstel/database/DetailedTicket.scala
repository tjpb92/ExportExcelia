package com.anstel.database

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.api.{Cursor, MongoConnection}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONArray, BSONDocument, array, document}

import com.anstel.export.CustomReader._
import com.anstel.libutilsscala.{DbServer, ApplicationParameters}

/**
 * Objet de type Models pour les requétes avec la collection DetailedTicket
 *
 * @author William Machi
 * @version 1.0
 */
object DetailedTicket extends Models {

  /**
   * @param connection
   * @param dbServer
   * @return scala future résolu à un object BSONCollection provenant de reactivemongo
   */
  def getCollection(connection: MongoConnection, dbServer: DbServer): Future[BSONCollection] =
    connection.database(dbServer.database).
      map(_.collection("detailedTickets"))


  /**
   * Requéte qui renvoie les tickets ouverts suite à une demande depuis l'application mobile
   *
   * @param collection
   * @param applicationParameters
   * @return scala future résolu à une liste case class de type TicketsOpenedFromSimplifiedRequest
   */
  def getTicketsOpenedFromSimplifiedRequest(collection: BSONCollection, applicationParameters: ApplicationParameters):
    Future[List[TicketsOpenedFromSimplifiedRequest]] = {

    import collection.aggregationFramework.{ Match, Lookup, Project, Unwind, AddFields }

    val query = collection.aggregatorContext[TicketsOpenedFromSimplifiedRequest](
      Match(BSONDocument(
       "$and" -> BSONArray(
         BSONDocument("linkedEntities.callCenterReferences" -> BSONDocument(
           "$eq" -> applicationParameters.callCenter
         )),
         BSONDocument("openedFromSimplifiedRequest" -> BSONDocument(
           "$exists" -> true
         )),
         BSONDocument(
           "created" -> BSONDocument(
             "$gte" -> applicationParameters.begDate,
             "$lt" -> applicationParameters.endDate
           )
         )
       )
      )),
      List(
        AddFields(document("firstEvent" -> document(f"$$arrayElemAt" -> array(f"$$journal", 0)))),
        AddFields(document("firstAgency" -> document(f"$$arrayElemAt" -> array(f"$$linkedEntities.agencies", 0)))),
        Lookup("simplifiedRequest", "openedFromSimplifiedRequest", "uid", "request"),
        Unwind("request", Option("SimplifiedRequestIndex"), Option(true)),
        Lookup("users", "firstEvent.operator.userUid", "uid", "user"),
        Unwind("user", Option("userIndex"), Option(true)),
        Project(BSONDocument(
          //  DETAILED TICKET
          "_id" -> 0,
          "uid" -> 1,
          "created" -> 1,
          "firstAgency.uid" -> 1,
          "firstAgency.name" -> 1,
          //  SIMPLIFIED REQUEST
          "request.uid" -> 1,
          "request.requestDate" -> 1,
          //  USER
          "user.uid" -> 1,
          "user.firstName" -> 1,
          "user.lastName" -> 1,
          "user.job" -> 1,
          "user.userType" -> 1,
        ))
      )
    )

    query.prepared.cursor.collect[List](-1, Cursor.FailOnError[List[TicketsOpenedFromSimplifiedRequest]]())

  }


  /**
   * Requéte qui renvoie les utilisateurs ayant ouvert un ticket suite à une demande depuis l'application mobile
   *
   * @param collection
   * @param applicationParameters
   * @return scala future résolu à une liste case class de type UsersFromTickets
   */
  def getUsersFromTickets(collection: BSONCollection, applicationParameters: ApplicationParameters): Future[List[UsersFromTickets]] = {

    import collection.aggregationFramework.{ Match, Lookup, Project, Unwind, AddFields }

    val query = collection.aggregatorContext[UsersFromTickets](
      Match(BSONDocument(
        "$and" -> BSONArray(
          BSONDocument("linkedEntities.callCenterReferences" -> BSONDocument(
            "$eq" -> applicationParameters.callCenter
          )),
          BSONDocument("openedFromSimplifiedRequest" -> BSONDocument(
            "$exists" -> true
          )),
          BSONDocument("created" -> BSONDocument(
            "$gte" -> applicationParameters.begDate,
            "$lt" -> applicationParameters.endDate
          ))
        )
      )),
      List(
        AddFields(document("firstEvent" -> document(f"$$arrayElemAt" -> array(f"$$journal", 0)))),
        AddFields(document("firstAgency" -> document(f"$$arrayElemAt" -> array(f"$$linkedEntities.agencies", 0)))),
        Lookup("users", "firstEvent.operator.userUid", "uid", "user"),
        Unwind("user", Option("userIndex"), Option(true)),
        Project(BSONDocument(
          //  DETAILED TICKET
          "_id" -> 0,
          "uid" -> 1,
          "firstAgency.uid" -> 1,
          "firstAgency.name" -> 1,
          //  USER
          "user.uid" -> 1,
          "user.firstName" -> 1,
          "user.lastName" -> 1,
          "user.userType" -> 1,
        ))
      )
    )

    query.prepared.cursor.collect[List](-1, Cursor.FailOnError[List[UsersFromTickets]]())

  }


  /**
   * Requéte qui renvoie les utilisateurs ayant ouvert un ticket suite à une demande depuis l'application mobile
   *
   * @param collection
   * @param applicationParameters
   * @return scala future résolu à une liste case class de type UsersFromTickets
   */
  def getUsersFromTicketsNotOpenedFromSimplifiedRequest(collection: BSONCollection, applicationParameters: ApplicationParameters): Future[List[UsersFromTickets]] = {

    import collection.aggregationFramework.{ Match, Lookup, Project, Unwind, AddFields }

    val query = collection.aggregatorContext[UsersFromTickets](
      Match(BSONDocument(
        "$and" -> BSONArray(
          BSONDocument("linkedEntities.callCenterReferences" -> BSONDocument(
            "$eq" -> applicationParameters.callCenter
          )),
          BSONDocument("openedFromSimplifiedRequest" -> BSONDocument(
            "$exists" -> false
          )),
          BSONDocument("created" -> BSONDocument(
            "$gte" -> applicationParameters.begDate,
            "$lt" -> applicationParameters.endDate
          ))
        )
      )),
      List(
        AddFields(document("firstEvent" -> document(f"$$arrayElemAt" -> array(f"$$journal", 0)))),
        AddFields(document("firstAgency" -> document(f"$$arrayElemAt" -> array(f"$$linkedEntities.agencies", 0)))),
        Lookup("users", "firstEvent.operator.userUid", "uid", "user"),
        Unwind("user", Option("userIndex"), Option(true)),
        Project(BSONDocument(
          //  DETAILED TICKET
          "_id" -> 0,
          "uid" -> 1,
          "firstAgency.uid" -> 1,
          "firstAgency.name" -> 1,
          //  USER
          "user.uid" -> 1,
          "user.firstName" -> 1,
          "user.lastName" -> 1,
          "user.userType" -> 1,
        ))
      )
    )

    query.prepared.cursor.collect[List](50, Cursor.FailOnError[List[UsersFromTickets]]())

  }


}

