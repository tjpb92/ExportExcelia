package com.anstel.export

import com.anstel.export.CustomReader.{Patrimony, Request, TicketsOpenedFromSimplifiedRequest, UsersFromTickets}

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
  def patrimonyReader(patrimonies: List[Patrimony]): List[List[String]] = {
    println("reading patrimony")
    val headers: List[String] = List("uid", "ref", "label", "callCenterReference", "company.uid", "company.name", "agency.uid", "agency.name")
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
    headers :: values
  }


  /**
   *
   * @param requests: List[CustomReader]
   * @return List avec les valeurs du CustomReader passer en paramétre + les titres
   */
  def requestReader(requests: List[Request]): List[List[String]] = {
    val headers: List[String] = List("uid", "requestDate", "requester.name", "patrimony.uid", "patrimony.ref", "patrimony.label", "callCenterReference", "company.uid", "company.name", "agency.uid", "agency.name")
    val values = requests.map {
      request => List(
        request.uid,
        request.requestDate,
        request.requester.name) ++ patrimonyReader(request.patrimony).tail.head
    }
    headers :: values
  }

  /**
   * @param tickets: List[CustomReader]
   * @return List avec les valeurs du CustomReader passer en paramétre + les titres
   */
  def TicketsOpenedFromSimplifiedRequestReader(tickets: List[TicketsOpenedFromSimplifiedRequest]): List[List[String]] = {
    val headers: List[String] = List(
      "uid",
      "user.uid",
      "user.firstName",
      "user.lastName",
      "created",
      "agency.uid",
      "agency.name",
      "poorRequest.uid",
      "poorRequest.requestDate",
      "parsedTimeDifference",
      "timeDifferenceInSeconds"
    )

    val values = tickets.map {
      ticketsOpenedFromSimplifiedRequest => List(
        ticketsOpenedFromSimplifiedRequest.uid,
        ticketsOpenedFromSimplifiedRequest.user.uid,
        ticketsOpenedFromSimplifiedRequest.user.firstName,
        ticketsOpenedFromSimplifiedRequest.user.lastName,
        ticketsOpenedFromSimplifiedRequest.created,
        ticketsOpenedFromSimplifiedRequest.agency.uid,
        ticketsOpenedFromSimplifiedRequest.agency.name,
        ticketsOpenedFromSimplifiedRequest.poorRequest.uid,
        ticketsOpenedFromSimplifiedRequest.poorRequest.requestDate,
        ticketsOpenedFromSimplifiedRequest.parsedTimeDifference,
        ticketsOpenedFromSimplifiedRequest.timeDifferenceInSeconds
      )
    }

    headers :: values

  }

  /**
   * @param users: List[CustomReader]
   * @return List avec les valeurs du CustomReader passer en paramétre + les titres
   */
  def UsersFromTicketsReader(users: List[UsersFromTickets]): List[List[String]] = {
    val headers: List[String] = List(
      "user.uid",
      "user.firstName",
      "user.lastName",
      "agency.uid",
      "agency.name",
      "ticketCount"
    )

    val values = users.map {
      ticketCount => List (
        ticketCount.user.uid,
        ticketCount.user.firstName,
        ticketCount.user.lastName,
        ticketCount.agency.uid,
        ticketCount.agency.name,
      )
    }

    headers :: mapToList(mapOfTicketCount(values))

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