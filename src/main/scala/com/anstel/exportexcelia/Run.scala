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
import com.anstel.database.Patrimonies.{getNonGeolocalisePatrimonies, getPatrimonyByCompanyUid}
import com.anstel.database.SimplifiedRequest.{getSimplifiedRequestBetween, getNonDealtRequestBetween}
import com.anstel.database.DetailedTicket.{getTicketsOpenedFromSimplifiedRequest, getUsersFromTickets, getUsersFromTicketsNotOpenedFromSimplifiedRequest, getTicketsByCompanyUid}
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
  def aggregateResults(applicationParameters: ApplicationParameters, dbServer: DbServer) = {

    /*addFileToExportBuffer(applicationParameters, dbServer, Patrimonies, getNonGeolocalisePatrimonies, patrimonyReader(false, _, _))
    if(applicationParameters.patrimony) {
      addFileToExportBuffer(applicationParameters, dbServer, Patrimonies, getPatrimonyByCompanyUid, patrimonyReader(true, _, _))
    }
    addFileToExportBuffer(applicationParameters, dbServer, SimplifiedRequest, getSimplifiedRequestBetween, requestReader)
    addFileToExportBuffer(applicationParameters, dbServer, SimplifiedRequest, getNonDealtRequestBetween, nonDealtRequestReader)
    addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getTicketsOpenedFromSimplifiedRequest, TicketsOpenedFromSimplifiedRequestReader)
    addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getUsersFromTickets, UsersFromTicketsReader(true, _, _))
    addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getUsersFromTicketsNotOpenedFromSimplifiedRequest, UsersFromTicketsReader(false, _, _))
    if(applicationParameters.tickets) {
      addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getTicketsByCompanyUid, TicketsReader)
    }*/

    addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getTicketsByCompanyUid, TicketsReader)
    ExcelWriter.excelExport(aggregator.getFiles(), applicationParameters)

  }


  /*def aggregateResults(applicationParameters: ApplicationParameters, dbServer: DbServer) = {

    val results = for {
      a <- addFileToExportBuffer(applicationParameters, dbServer, Patrimonies, getNonGeolocalisePatrimonies, patrimonyReader(false, _, _))
      b <- addFileToExportBuffer(applicationParameters, dbServer, Patrimonies, getPatrimonyByCompanyUid, patrimonyReader(true, _, _)) if(applicationParameters.patrimony)
      c <- addFileToExportBuffer(applicationParameters, dbServer, SimplifiedRequest, getSimplifiedRequestBetween, requestReader)
      d <- addFileToExportBuffer(applicationParameters, dbServer, SimplifiedRequest, getNonDealtRequestBetween, nonDealtRequestReader)
      e <- addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getTicketsOpenedFromSimplifiedRequest, TicketsOpenedFromSimplifiedRequestReader)
      f <- addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getUsersFromTickets, UsersFromTicketsReader(true, _, _))
      g <- addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getUsersFromTicketsNotOpenedFromSimplifiedRequest, UsersFromTicketsReader(false, _, _))
      h <- addFileToExportBuffer(applicationParameters, dbServer, DetailedTicket, getTicketsByCompanyUid, TicketsReader) if (applicationParameters.tickets)
    } yield List(a, b, c, d, e, f, g, h)

    results.onComplete { value =>
      value match {
        case Success(files) =>
          println("sending data to excel writer")
          ExcelWriter.excelExport(files, applicationParameters)
        case Failure(f) =>
          println("FAILURE" + f.getMessage())
      }
    }
  }*/


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

  /*def addFileToExportBuffer[A <: AbstractCustomReader](
    applicationParameters: ApplicationParameters,
    dbServer: DbServer,
    model: Models,
    query: (BSONCollection, ApplicationParameters) => Future[List[A]],
    caseClassReader: (List[A], ApplicationParameters) => List[List[String]]
  ) = {
    val result = for {
      connection <- Connect.connection(dbServer)
      collection <- model.getCollection(connection, dbServer)
      data <- query(collection, applicationParameters)
      //result <- caseClassReader(data, applicationParameters)
      result <- caseClassReader(data, applicationParameters)
    } yield result

    result

    /*result.onComplete { value =>
      value match {
        case Success(value) =>
          Right(caseClassReader(value, applicationParameters))
        case Failure(f) =>
          Left(println("FAILURE" + f.getMessage()))
      }
    }*/

  }*/

}
