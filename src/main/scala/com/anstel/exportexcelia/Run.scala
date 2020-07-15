package com.anstel.exportexcelia

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.language.postfixOps

import reactivemongo.api.bson.collection.BSONCollection

import com.anstel.export.CustomReader.AbstractCustomReader
import com.anstel.export.ExcelWriter
import com.anstel.export.Aggregator
import com.anstel.database.{Connect, Models}
import com.anstel.libutilsscala.{ApplicationParameters, DbServer}

import com.anstel.database._
import com.anstel.database.Patrimonies.getNonGeolocalisePatrimonies
import com.anstel.database.SimplifiedRequest.{getSimplifiedRequestBetween, getNonDealtRequestBetween}
import com.anstel.database.DetailedTicket.{getTicketsOpenedFromSimplifiedRequest, getUsersFromTickets, getUsersFromTicketsNotOpenedFromSimplifiedRequest}
import com.anstel.export.CaseClassReader._

/**
 * Objet contenant la function run
 *
 * @author William Machi
 * @version 1.0
 */
object Run {

  val aggregator = new Aggregator()

  /**
   * Funciton aggregateResults Aggrége les résultats des differentes requétes
   *
   * @param applicationParameters
   * @param dbServer
   */
  def aggregateResults(
      applicationParameters: ApplicationParameters,
      dbServer: DbServer,
    ) =
  {

    addFileToExportBuffer(applicationParameters, dbServer, Patrimonies, getNonGeolocalisePatrimonies, patrimonyReader)
    addFileToExportBuffer(applicationParameters, dbServer, SimplifiedRequest, getSimplifiedRequestBetween, requestReader)
    addFileToExportBuffer(applicationParameters, dbServer, SimplifiedRequest, getNonDealtRequestBetween, nonDealtRequestReader)
    addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getTicketsOpenedFromSimplifiedRequest, TicketsOpenedFromSimplifiedRequestReader)
    addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getUsersFromTickets, UsersFromTicketsReader(true, _, _))
    addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getUsersFromTicketsNotOpenedFromSimplifiedRequest, UsersFromTicketsReader(false, _, _))

    ExcelWriter.excelExport(aggregator.getFiles(), applicationParameters)

  }


  /**
   * Funciton run résout les Future un a un et créé le fichier xlsx
   *
   * @param applicationParameters
   * @param dbServer
   * @param model
   * @param query
   * @param caseClassReader
   * @tparam A
   */
  def addFileToExportBuffer[A <: AbstractCustomReader](
    applicationParameters: ApplicationParameters,
    dbServer: DbServer,
    model: Models,
    query: (BSONCollection, ApplicationParameters) => Future[List[A]],
    caseClassReader: (List[A], ApplicationParameters) => List[List[String]]
  ) =
  {
    Connect.connection(dbServer).onComplete {
      case Success(connection) => {
        model.getCollection(connection, dbServer).onComplete {
          case Success(collection) => {
            query(collection, applicationParameters).onComplete {
              case Success(data) => {
                if(applicationParameters.debugMode) {
                  println(s"recuperation des donnes associees a $caseClassReader")

                  println(s"${data.length} resultat ajoute au buffer")
                }
                aggregator.setFiles(caseClassReader(data, applicationParameters))
              }
              case Failure(f) => println("FAILURE" + f.getMessage())
            }
          }
          case Failure(f) => println("FAILURE" + f.getMessage())
        }
      }
      case Failure(f) => println("FAILURE" + f.getMessage())
    }
  }

}
