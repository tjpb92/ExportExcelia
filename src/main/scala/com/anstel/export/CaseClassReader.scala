package com.anstel.export

import java.util.concurrent.TimeUnit

import com.anstel.`export`.CustomReader.JournalEvent
import com.anstel.export.CustomReader.{NonDealtRequest, Patrimony, Request, Tickets, TicketsOpenedFromSimplifiedRequest, User, UsersFromTickets}
import com.anstel.libutilsscala.ApplicationParameters

/**
 * Objet qui regroupe les fonctions pour transformer les listes de CustomReader renvoyer par les modéle en liste de liste de string
 * Ensuite passer à l'object ExcelWriter pour l'export en .xlsx
 *
 * @author William Machi
 *  @version 1.0
 */
object CaseClassReader {

  /**
   * @param patrimonies: List[CustomReader]
   * @return List avec les valeurs du CustomReader passer en paramétre + les titres
   */
  def patrimonyReader(patrimonyFromClient: Boolean, patrimonies: List[Patrimony], applicationParameters: ApplicationParameters): List[List[String]] = {

    val sheetName: List[String] = patrimonyFromClient match {
      case true =>  List("patrimony associé au client")
      case false =>  List("patrimony non géolocalisé")
    }

    val headers: List[String] = applicationParameters.debugMode match {
      case true => List("uid", "ref", "label", "callCenterReference", "company.uid", "company.name", "agency.uid", "agency.name")
      case false => List("uid", "ref", "label", "callCenterReference", "company.uid", "company.name", "agency.uid", "agency.name")
    }

    val values = patrimonies.map {
      patrimony => List(
        patrimony.uid,
        patrimony.ref,
        patrimony.label,
        patrimony.callCenterReferences.head,
        patrimony.company.uid,
        patrimony.company.name,
        patrimony.agency.head.uid,
        patrimony.agency.head.name)
    }
    sheetName :: headers :: values
  }


  /**
   *
   * @param requests: List[CustomReader]
   * @return List avec les valeurs du CustomReader passer en paramétre + les titres
   */
  def requestReader(requests: List[Request], applicationParameters: ApplicationParameters): List[List[String]] = {
    val sheetName: List[String] = List("request")

    val headers: List[String] = applicationParameters.debugMode match {
      case true => List(
        "uid",
        "requestDate",
        "requester.name",
        "patrimony.uid",
        "patrimony.ref",
        "patrimony.label",
        "callCenterReference",
        "company.uid",
        "company.name",
        "agency.uid",
        "agency.name"
      )
      case false => List(
        "uid",
        "requestDate",
        "requester.name",
        "patrimony.uid",
        "patrimony.ref",
        "patrimony.label",
        "callCenterReference",
        "company.uid",
        "company.name",
        "agency.uid",
        "agency.name"
      )
    }

    val values = requests.map {
      request => List(
        request.uid,
        request.requestDate,
        request.requester.name) ::: patrimonyReader(false, request.patrimony, applicationParameters).tail.tail.head
    }
    sheetName :: headers :: values
  }

  /**
   *
   * @param users
   * @return
   */
  def userReader(users: List[User]): List[List[String]] = {
    for(user <- users) yield List(user.uid, user.firstName, user.lastName, user.job)
  }


  /**
   *
   * @param nonDealtRequests
   * @param applicationParameters
   * @return
   */
  def nonDealtRequestReader(nonDealtRequests: List[NonDealtRequest], applicationParameters: ApplicationParameters): List[List[String]] = {
    val sheetName: List[String] = List("nonDealtRequest")

    val headers: List[String] = applicationParameters.debugMode match {
      case true => List(
        "uid",
        "requestDate",
        "requester.name"
      )
      case false => List(
        "uid",
        "requestDate",
        "requester.name"
      )
    }

    val values: List[List[String]] = nonDealtRequests.map {
      nonDealtRequest => List(
        nonDealtRequest.uid,
        nonDealtRequest.requestDate,
        nonDealtRequest.requester.name
      ) ::: userReader(nonDealtRequest.users).flatten
    }


    val usersNumbers: List[Int] = for(value <- values) yield (value.length - 3) / 4

    val usersHeaders: List[String] = headers ::: List.fill(usersNumbers.maxOption.getOrElse(0))(List("user.uid", "user.firstname", "user.lastname", "user.job")).flatten

    sheetName :: usersHeaders :: values
  }

  def eventReader(events: List[JournalEvent]): List[List[String]] = {
    for(event <- events) yield List(event.eventType, event.sendDate)
  }

  /**
   *
   */
  def TicketsReader(tickets: List[Tickets], applicationParameters: ApplicationParameters): List[List[String]] = {
    val sheetName: List[String] = List("tickets du client")

    val headers: List[String] = applicationParameters.debugMode match {
      case true => List(
        "uid",
        "ref",
        "created",
        "closed",
        "transmissionDelay",
        "openingTicketPurpose.callPurpose.",
        "caller.name.value",
        "patrimony.uid",
        "patrimony.ref",
        "address.street",
        "address.zipCode",
        "address.city",
        "agency.uid",
        "agency.name",
        "user.lastName",
        "user.firstName",
        "user.job",
      )
      case false => List(
        "uid du ticket",
        "ref du ticket",
        "date de création",
        "date de cloture",
        "Delai de transmission",
        "Delai de transmission (s)",
        "qualification",
        "appelant",
        "uid patrimoine",
        "ref patrimoine",
        "address",
        "code postale",
        "ville",
        "uid agence",
        "nom agence",
        "nom primo-intervenant",
        "prenom primo-intervenant",
        "poste primo-intervenant",
      )
    }


    val values = tickets.map {
      ticket => {
        val patrimony: Patrimony = ticket.linkedEntities.patrimony.uid match {
          case "non renseigné" => {
            ticket.patrimony
          }
          case _ => {
            ticket.linkedEntities.patrimony
          }
        }

        val name: String  = ticket.caller.ticketCallerName.nameType match {
          case "CivilName" => s"${ticket.caller.ticketCallerName.firstName} ${ticket.caller.ticketCallerName.lastName}"
          case "PoorName" => s"${ticket.caller.ticketCallerName.value}"
          case _ => "non renseigné"
        }

        val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val interventionAcceptedDate = ticket.journal.filter( event => event.eventType == "InterventionAccepted" || event.eventType == "MissionAccepted")

        val transmissionDelay: Either[String, Long] = interventionAcceptedDate match {
          case list if list.length == 0 => Left("intervention non accepté")
          case list => Right(format.parse(list.head.sendDate).getTime - format.parse(ticket.created).getTime)
        }

        val TransmissionDelaySeconds = transmissionDelay match {
          case Left(value) => value
          case Right(millis) => TimeUnit.MILLISECONDS.toSeconds(millis).toString
        }

        val TransmissionDelayParsed = transmissionDelay match {
          case Left(value) => value
          case Right(millis) => String.format("%02dj %02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toDays(millis),

            TimeUnit.MILLISECONDS.toHours(millis) -
              TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis)),

            TimeUnit.MILLISECONDS.toMinutes(millis) -
              TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),

            TimeUnit.MILLISECONDS.toSeconds(millis) -
              TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }

        List(
          ticket.uid,
          ticket.ref,
          ticket.created,
          ticket.closed,
          TransmissionDelayParsed,
          TransmissionDelaySeconds,
          ticket.openingTicketPurpose.callPurpose,
          name,
          patrimony.uid,
          patrimony.ref,
          ticket.address.street,
          ticket.address.zipCode,
          ticket.address.city,
          ticket.agency.uid,
          ticket.agency.name,
          ticket.user.lastName,
          ticket.user.firstName,
          ticket.user.job,
        ) ::: eventReader(ticket.journal).flatten
      }
    }

    // val usersNumbers: List[Int] = for(value <- values) yield (value.length - 3) / 4
    // val usersHeaders: List[String] = headers ::: List.fill(usersNumbers.maxOption.getOrElse(0))(List("user.uid", "user.firstname", "user.lastname", "user.job")).flatten


    val eventNumbers: List[Int] = for(value <- values) yield (value.length - 16) / 2
    val journalEventHeaders: List[String] = headers ::: List.fill(eventNumbers.maxOption.getOrElse(0))(List("type d'evenement", "date d'evenement")).flatten

    sheetName :: journalEventHeaders :: values

  }

  /**
   * @param tickets: List[CustomReader]
   * @return List avec les valeurs du CustomReader passer en paramétre + les titres
   */
  def TicketsOpenedFromSimplifiedRequestReader(tickets: List[TicketsOpenedFromSimplifiedRequest], applicationParameters: ApplicationParameters): List[List[String]] = {
    val sheetName: List[String] = List("temps request-tickets")

    val headers: List[String] = applicationParameters.debugMode match {
      case true => List(
        "uid",
        "user.uid",
        "user.firstName",
        "user.lastName",
        "user.job",
        "created",
        "agency.uid",
        "agency.name",
        "poorRequest.uid",
        "poorRequest.requestDate",
        "parsedTimeDifference",
        "timeDifferenceInSeconds"
      )
      case false => List(
        "uid",
        "user.uid",
        "user.firstName",
        "user.lastName",
        "user.job",
        "created",
        "agency.uid",
        "agency.name",
        "Request.uid",
        "Request.requestDate",
        "parsedTimeDifference",
        "timeDifferenceInSeconds"
      )
    }

    val values = tickets.map {
      ticketsOpenedFromSimplifiedRequest => List(
        ticketsOpenedFromSimplifiedRequest.uid,
        ticketsOpenedFromSimplifiedRequest.user.uid,
        ticketsOpenedFromSimplifiedRequest.user.firstName,
        ticketsOpenedFromSimplifiedRequest.user.lastName,
        ticketsOpenedFromSimplifiedRequest.user.job,
        ticketsOpenedFromSimplifiedRequest.created,
        ticketsOpenedFromSimplifiedRequest.agency.uid,
        ticketsOpenedFromSimplifiedRequest.agency.name,
        ticketsOpenedFromSimplifiedRequest.poorRequest.uid,
        ticketsOpenedFromSimplifiedRequest.poorRequest.requestDate,
        ticketsOpenedFromSimplifiedRequest.parsedTimeDifference,
        ticketsOpenedFromSimplifiedRequest.timeDifferenceInSeconds
      )
    }

    sheetName :: headers :: values

  }

  /**
   * @param users: List[CustomReader]
   * @return List avec les valeurs du CustomReader passer en paramétre + les titres
   */
  def UsersFromTicketsReader(request: Boolean, users: List[UsersFromTickets], applicationParameters: ApplicationParameters): List[List[String]] = {
    val sheetName: List[String] = request match {
      case true =>  List("#ticket avec demande")
      case false =>  List("#ticket sans demande")
    }



    val headers: List[String] = applicationParameters.debugMode match {
      case true => List(
        "user.uid",
        "user.firstName",
        "user.lastName",
        "user.job",
        "agency.uid",
        "agency.name",
        "ticketCount"
      )
      case false => List(
        "user.uid",
        "user.firstName",
        "user.lastName",
        "user.job",
        "agency.uid",
        "agency.name",
        "ticketCount"
      )
    }

    val values = users.map {
      ticketCount => List (
        ticketCount.user.uid,
        ticketCount.user.firstName,
        ticketCount.user.lastName,
        ticketCount.user.job,
        ticketCount.agency.uid,
        ticketCount.agency.name,
      )
    }

    sheetName :: headers :: mapToList(mapOfTicketCount(values))

  }

  /**
   * Helper method pour compter le nombre de ticket par utilisateur
   *
   * @param users
   * @return Map associant les données de l'utilisateur avec le nombre de ticket créé
   */
  def mapOfTicketCount(users: List[List[String]]): Map[List[String], Int] = {
    users.foldLeft(Map[List[String], Int]().withDefaultValue(0)){
      case (acc, list) =>
        acc + (list -> (1 + acc(list)))
    }
  }

  /**
   * Helper method pour transformer le MAP renvoyer par mapOfTicketCount en liste
   *
   * @param ticketCount
   * @return List avec les valeurs du CustomReader passer en paramétre + le nombre de ticket par utilsateur
   */
  def mapToList(ticketCount: Map[List[String], Int]): List[List[String]] = {

    def mapToListAcc(ticketCount: Map[List[String], Int], acc: List[List[String]] = List()): List[List[String]] = {
      if(ticketCount.isEmpty) acc
      else mapToListAcc(ticketCount.tail, acc ::: List(ticketCount.head._1 :+ ticketCount.head._2.toString))
    }

    mapToListAcc(ticketCount)

  }

}