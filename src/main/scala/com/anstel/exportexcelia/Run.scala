package com.anstel.exportexcelia

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.{Failure, Success}

import reactivemongo.api.bson.collection.BSONCollection

import com.anstel.`export`.CustomReader.AbstractCustomReader
import com.anstel.`export`.ExcelWriter
import com.anstel.database.{Connect, Models}
import com.anstel.libutilsscala.{ApplicationParameters, DbServer}

/**
 * Objet contenant la function run
 *
 * @author William Machi
 * @version 1.0
 */
object Run {

  /**
   * Funciton run résout les Future un a un et créé le fichier xlsx
   *
   * @param applicationParameters
   * @param dbServer
   * @param model
   * @param request
   * @param caseClassReader
   * @tparam A
   */
  def runExport[A <: AbstractCustomReader](
    applicationParameters: ApplicationParameters,
    dbServer: DbServer,
    model: Models,
    request: (BSONCollection, ApplicationParameters) => Future[List[A]],
    caseClassReader: (List[A]) => List[List[String]]
  ) =
  {
    Connect.connection(dbServer).onComplete {
      case Success(connection) => {
        model.getCollection(connection, dbServer).onComplete {
          case Success(collection) => {
            request(collection, applicationParameters).onComplete {
              case Success(data) => {
                ExcelWriter.excelExport(caseClassReader(data), applicationParameters)
                System.exit(0)
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
