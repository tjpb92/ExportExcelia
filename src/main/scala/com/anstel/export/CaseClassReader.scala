package com.anstel.export

import com.anstel.export.CustomReader.{Patrimony, Request, NonDealtRequest, TicketsOpenedFromSimplifiedRequest, UsersFromTickets, User}
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
  def patrimonyReader(patrimonies: List[Patrimony], applicationParameters: ApplicationParameters): List[List[String]] = {
    val sheetName: List[String] = List("patrimony")

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
        request.requester.name) ::: patrimonyReader(request.patrimony, applicationParameters).tail.tail.head
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

    println(s"max users is ${usersNumbers.max}")

    val usersHeaders: List[String] = headers ::: List.fill(usersNumbers.max)(List("user.uid", "user.firstname", "user.lastname", "user.job")).flatten

    sheetName :: usersHeaders :: values
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

/*
val x: List[List[String]] = List(List("hello"), List("halo"), List("hola"))
val y: List[List[String]] = List(List("one"), List("eins"), List("one"))

case class User(firstname: String, lastname: String, job: String)
case class UnHandle(uid: String, requester: String, date: String, users: List[User] )

val users: List[User] = List(User("joe", "black", "manager"), User("jane", "white", "chief"), User("jack", "blue", "manager"), User("jess", "green", "assistant"))
val requests: List[UnHandle] = List(
  UnHandle("1", "albert", "01-02-1996", List(User("joe", "black", "manager"), User("jack", "blue", "manager"))),
  UnHandle("2", "simone", "06-08-2012", List(User("jane", "white", "chief"))),
  UnHandle("3", "henry", "04-06-1985", List(User("jack", "blue", "manager"))),
  UnHandle("4", "gertrude", "10-12-2020", List(User("jess", "green", "assistant"))),
)

def breakDown(users: List[User]) = {
  for(user <- users) yield List(user.firstname, user.lastname, user.job)
}

def read(requests: List[UnHandle]) = {
  requests.map {
    request => List(
      request.uid,
      request.requester,
      request.date
    ) ::: breakDown(request.users)
  }
}

val test = read(requests)

test

 */