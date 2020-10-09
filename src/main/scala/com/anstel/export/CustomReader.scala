package com.anstel.export

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader}

/**
 * Objet qui regroupe les CustomReader utilisé par les modéles pour renvoyer les résultats des requétes
 *
 * @author William Machi
 * @version 1.0
 */
object CustomReader {

  /**
   * Class abstraite pour pour conformer le type des CustomReader
   */
  abstract class AbstractCustomReader /*{
    var counter = 0
    //def name: String
    def count(): Unit = {
      counter += 1
      if(counter <= 10 || counter % 100 == 0) println(s"$counter ${this.getClass.getName} créé")
    }
  }*/

  /**
   * CustomReader pour le champ Company
   *
   * @param uid
   * @param name
   * @param holding
   */
  case class Company(uid: String, name: String, holding: String)

  object Company {
    implicit object CompanyReader extends BSONDocumentReader[Company] {
      def read(doc: BSONDocument): Company = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val name = doc.getAs[String]("name").getOrElse("non renseigné")
        val holding = doc.getAs[String]("holding").getOrElse("non renseigné")

        Company(uid, name, holding)
      }
    }
  }


  /**
   * CustomReader pour le champ Agency
   *
   * @param uid
   * @param name
   */
  case class Agency(uid: String, name: String)

  object Agency {
    implicit object AgencyReader extends BSONDocumentReader[Agency] {
      def read(doc: BSONDocument): Agency = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val name = doc.getAs[String]("name").getOrElse("non renseigné")

        Agency(uid, name)
      }
    }
  }

  /**
   * CustomReader Utiliser pour l'export de Patrimonies.getNonGeolocalisePatrimonies
   *
   * @param uid
   * @param ref
   * @param label
   * @param callCenterReferences
   * @param company
   * @param agency
   */
  case class Patrimony(uid: String, ref: String, label: String, callCenterReferences: List[String], company: Company, agency: List[Agency]) extends AbstractCustomReader

  object Patrimony {
    implicit object PatrimonyReader extends BSONDocumentReader[Patrimony] {
      def read(doc: BSONDocument): Patrimony = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val ref = doc.getAs[String]("ref").getOrElse("non renseigné")
        val label = doc.getAs[String]("label").getOrElse("non renseigné")
        val callCenterReferences = doc.getAs[List[String]]("callCenterReferences").getOrElse(List("non renseigné"))
        val company = doc.getAs[Company]("company").getOrElse(Company("non renseigné", "non renseigné", "non renseigné"))
        val agency = doc.getAs[List[Agency]]("agencies").getOrElse(List(Agency("non renseigné", "non renseigné")))

        Patrimony(uid, ref, label, callCenterReferences, company, agency)
      }
    }
  }

  /*case class PoorPatrimony(uid: String, name: String, ref: String,  callCenterReferences: List[String], company: Company, agency: List[Agency]) extends AbstractCustomReader

  object Patrimony {
    implicit object PatrimonyReader extends BSONDocumentReader[Patrimony] {
      def read(doc: BSONDocument): Patrimony = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val ref = doc.getAs[String]("ref").getOrElse("non renseigné")
        val label = doc.getAs[String]("label").getOrElse("non renseigné")
        val callCenterReferences = doc.getAs[List[String]]("callCenterReferences").getOrElse(List("non renseigné"))
        val company = doc.getAs[Company]("company").getOrElse(Company("non renseigné", "non renseigné", "non renseigné"))
        val agency = doc.getAs[List[Agency]]("agencies").getOrElse(List(Agency("non renseigné", "non renseigné")))

        Patrimony(uid, ref, label, callCenterReferences, company, agency)
      }
    }
  }*/

  /**
   * CustomReader pour le champ Requester
   * Le nom de la personne qui a fait la demande depusi l'appllication mobile
   *
   * @param name
   */
  case class Requester(name: String)

  object Requester {
    implicit object RequesterReader extends BSONDocumentReader[Requester] {
      def read(doc: BSONDocument): Requester = {
        val name = doc.getAs[String]("name").getOrElse("non renseigné")

        Requester(name)
      }
    }
  }

  /**
   * CustomReader Utiliser pour l'export de SimplifiedRequest.getSimplifiedRequestBetween
   *
   * @param uid
   * @param requestDate
   * @param requester
   * @param patrimony
   */
  case class Request(uid: String, requestDate: String, requester: Requester, patrimony: List[Patrimony]) extends AbstractCustomReader

  object Request {
    implicit object RequestReader extends BSONDocumentReader[Request] {
      def read(doc: BSONDocument): Request = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val requestDate = doc.getAs[String]("requestDate").getOrElse("non renseigné")
        val requesterName = doc.getAs[Requester]("requester").getOrElse(Requester("non renseigné"))
        val patrimony = doc.getAs[List[Patrimony]]("patrimony").getOrElse(List(Patrimony("non renseigné", "non renseigné", "non renseigné", List("non renseigné"), Company("non renseigné", "non renseigné", "non renseigné"), List(Agency("non renseigné", "non renseigné")))))

        Request(uid, requestDate, requesterName, patrimony)
      }
    }
  }


  /**
   * CustomReader Utilisé pour lire les données de l'utilisateur qui a ouvert le ticket
   *
   * @param uid
   * @param firstName
   * @param lastName
   * @param job
   */
  case class User(uid: String, firstName: String, lastName: String, job: String)

  object User {
    implicit object UserReader extends BSONDocumentReader[User] {
      def read(doc: BSONDocument): User = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val firstName = doc.getAs[String]("firstName").getOrElse("non renseigné")
        val lastName = doc.getAs[String]("lastName").getOrElse("non renseigné")
        val job = doc.getAs[String]("job").getOrElse(doc.getAs[String]("userType").getOrElse("non renseigné"))

        User(uid, firstName, lastName, job)
      }
    }
  }

  case class NonDealtRequest(uid: String, requestDate: String, requester: Requester, users: List[User]) extends AbstractCustomReader

  object NonDealtRequest {
    implicit object NonDealtRequestReader extends BSONDocumentReader[NonDealtRequest] {
      def read(doc: BSONDocument): NonDealtRequest = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val requestDate = doc.getAs[String]("requestDate").getOrElse("non renseigné")
        val requesterName = doc.getAs[Requester]("requester").getOrElse(Requester("non renseigné"))
        val users = doc.getAs[List[User]]("users").getOrElse(List(User("non renseigné", "non renseigné", "non renseigné", "non renseigné")))

        NonDealtRequest(uid, requestDate, requesterName, users)
      }
    }
  }


  /**
   * CustomReader renvoyent uniquement l'uid et la date
   * Utilisé pour calculer le temps entre la SimplifiedRequest et l'ouverture du ticket
   *
   * @param uid
   * @param requestDate
   */
  case class PoorRequest(uid: String, requestDate: String)

  object PoorRequest {
    implicit object PoorRequestReader extends BSONDocumentReader[PoorRequest] {
      def read(doc: BSONDocument): PoorRequest = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val requestDate = doc.getAs[String]("requestDate").getOrElse("non renseigné")

        PoorRequest(uid, requestDate)
      }
    }
  }

  /**
   *
   */
  case class OpeningTicketPurpose(callPurpose: String)

  object OpeningTicketPurpose {
    implicit object OpeningTicketPurposeReader extends BSONDocumentReader[OpeningTicketPurpose] {
      def read(doc: BSONDocument): OpeningTicketPurpose = {
        val callPurpose = doc.getAs[String]("callPurpose").getOrElse("non renseigné")

        OpeningTicketPurpose(callPurpose)
      }
    }
  }

  /**
   *
   */
  case class LinkedEntities(patrimony: Patrimony)

  object LinkedEntities {
    implicit object LinkedEntitiesReader extends BSONDocumentReader[LinkedEntities] {
      def read(doc: BSONDocument): LinkedEntities = {
        val patrimony = doc.getAs[Patrimony]("patrimony").getOrElse(
          Patrimony("non renseigné", "non renseigné", "non renseigné", List("non renseigné"), Company("non renseigné", "non renseigné", "non renseigné"), List(Agency("non renseigné", "non renseigné")))
        )

        LinkedEntities(patrimony)
      }
    }
  }

  /**
   *
   */
  case class TicketCallerNameValue(value: String, firstName: String, lastName: String, nameType: String)

  object TicketCallerNameValue {
    implicit object TicketCallerNameValueReader extends BSONDocumentReader[TicketCallerNameValue] {
      def read(doc: BSONDocument): TicketCallerNameValue = {
        val value = doc.getAs[String]("value").getOrElse("non renseigné")
        val firstName = doc.getAs[String]("firstName").getOrElse("non renseigné")
        val lastName = doc.getAs[String]("lastName").getOrElse("non renseigné")
        val nameType = doc.getAs[String]("nameType").getOrElse("non renseigné")

        TicketCallerNameValue(value, firstName, lastName, nameType)
      }
    }
  }

  /**
   *
   */
  case class TicketCaller(ticketCallerName: TicketCallerNameValue)

  object TicketCaller {
    implicit object TicketCallerReader extends BSONDocumentReader[TicketCaller] {
      def read(doc: BSONDocument): TicketCaller = {
        val nameValue = doc.getAs[TicketCallerNameValue]("name").get//OrElse(TicketCaller(TicketCallerNameValue("non renseigné")))

        TicketCaller(nameValue)
      }
    }
  }

  /**
   *
   */
  case class TicketAddress(street: String, zipCode: String, city: String)

  object TicketAddress {
    implicit object TicketAddressReader extends BSONDocumentReader[TicketAddress] {
      def read(doc: BSONDocument): TicketAddress = {
        val street = doc.getAs[String]("street").getOrElse("non renseigné")
        val zipCode = doc.getAs[String]("zipCode").getOrElse("non renseigné")
        val city = doc.getAs[String]("city").getOrElse("non renseigné")

        TicketAddress(street, zipCode, city)
      }
    }
  }

  case class JournalEvent(sendDate: String, eventType: String)

  object JournalEvent {
    implicit object JournalEventReader extends BSONDocumentReader[JournalEvent] {
      def read(doc: BSONDocument): JournalEvent = {
        val sentDate = doc.getAs[String]("sentDate").getOrElse("non renseigné")
        val eventType = doc.getAs[String]("eventType").getOrElse("non renseigné")

        JournalEvent(sentDate, eventType)
      }
    }
  }

  /**
   * CustomReader utilisé pour l'export de DetailedTicket.getTicketsFromAClientCompany
   */
  case class Tickets(
                      uid: String, ref: String, created: String, closed: String, openingTicketPurpose: OpeningTicketPurpose, caller: TicketCaller,
                      linkedEntities: LinkedEntities, patrimony: Patrimony, address: TicketAddress,
                      agency: Agency, user: User,
                      journal: List[JournalEvent]
                    ) extends AbstractCustomReader

  object Tickets {
    implicit object TicketsReader extends BSONDocumentReader[Tickets] {
      def read(doc: BSONDocument): Tickets = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val ref = doc.getAs[String]("ref").getOrElse("non renseigné")
        val created = doc.getAs[BSONDateTime]("created").getOrElse(BSONDateTime(0L))
        val closed = doc.getAs[BSONDateTime]("closed").getOrElse(BSONDateTime(0L))
        val callPurpose = doc.getAs[OpeningTicketPurpose]("openingTicketPurpose").getOrElse(OpeningTicketPurpose("non renseigné"))
        val caller = doc.getAs[TicketCaller]("caller").getOrElse(TicketCaller(TicketCallerNameValue("non renseigné", "non renseigné", "non renseigné", "non renseigné")))
        val linkedEntities = doc.getAs[LinkedEntities]("linkedEntities").getOrElse(LinkedEntities(Patrimony("non renseigné", "non renseigné", "non renseigné", List("non renseigné"), Company("non renseigné", "non renseigné", "non renseigné"), List(Agency("non renseigné", "non renseigné")))))
        val patrimony = doc.getAs[Patrimony]("patrimony").getOrElse(Patrimony("non renseigné", "non renseigné", "non renseigné", List("non renseigné"), Company("non renseigné", "non renseigné", "non renseigné"), List(Agency("non renseigné", "non renseigné"))))
        val ticketAddress = doc.getAs[TicketAddress]("address").getOrElse(TicketAddress("non renseigné", "non renseigné", "non renseigné"))
        val agency = doc.getAs[Agency]("firstAgency").getOrElse(Agency("non, renseigné", "non renseigné"))
        val user = doc.getAs[User]("user").getOrElse(User("non renseigné", "non renseigné", "non renseigné", "non renseigné"))
        val journal: List[JournalEvent] = doc.getAs[List[JournalEvent]]("journal").getOrElse(List(JournalEvent("non renseigné", "non renseigné")))

        val createdParsed: String = created match {
          case BSONDateTime(0L) => "date de création inconnue"
          case BSONDateTime(value) => Instant.ofEpochMilli(value).toString()
        }

        val closedParsed: String = closed match {
          case BSONDateTime(0l) => "Ticket Non cloturé"
          case BSONDateTime(value) => Instant.ofEpochMilli(value).toString()
        }

        Tickets(uid, ref, createdParsed, closedParsed, callPurpose, caller, linkedEntities, patrimony, ticketAddress, agency, user, journal)
      }
    }
  }

  /**
   *  CustomReader Utilisé pour l'export de DetailedTicket.getTicketsOpenedFromSimplifiedRequest
   *
   * @param uid
   * @param created
   * @param agency
   * @param poorRequest
   * @param user
   * @param parsedTimeDifference
   * @param timeDifferenceInSeconds
   */
  case class TicketsOpenedFromSimplifiedRequest(uid: String, created: String, agency: Agency, poorRequest: PoorRequest, user: User, parsedTimeDifference: String, timeDifferenceInSeconds: String) extends AbstractCustomReader

  object TicketsOpenedFromSimplifiedRequest {
    implicit object TicketsOpenedFromSimplifiedRequestReader extends BSONDocumentReader[TicketsOpenedFromSimplifiedRequest] {
      def read(doc: BSONDocument): TicketsOpenedFromSimplifiedRequest = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val created = doc.getAs[BSONDateTime]("created").getOrElse(BSONDateTime(0L))
        val agency = doc.getAs[Agency]("firstAgency").getOrElse(Agency("non, renseigné", "non renseigné"))
        val poorRequest = doc.getAs[PoorRequest]("request").getOrElse(PoorRequest("non renseigné", "non renseigné"))
        val user = doc.getAs[User]("user").getOrElse(User("non renseigné", "non renseigné", "non renseigné", "non renseigné"))

        val createdParsed: String = created match {
          case BSONDateTime(value) => Instant.ofEpochMilli(value).toString()
        }

        // Calcul de la différence de temps entre la requete et le ticket

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        val request = LocalDateTime.parse(poorRequest.requestDate, formatter)
        val ticket = LocalDateTime.parse(createdParsed, formatter)

        val timeDifferenceInSeconds: Long = ticket.toEpochSecond(ZoneOffset.UTC) - request.toEpochSecond(ZoneOffset.UTC)

        val day: Long = TimeUnit.SECONDS.toDays(timeDifferenceInSeconds)
        val hours: Long = TimeUnit.SECONDS.toHours(timeDifferenceInSeconds) - (day *24)
        val minute: Long = TimeUnit.SECONDS.toMinutes(timeDifferenceInSeconds) - (TimeUnit.SECONDS.toHours(timeDifferenceInSeconds)* 60)
        val second: Long = TimeUnit.SECONDS.toSeconds(timeDifferenceInSeconds) - (TimeUnit.SECONDS.toMinutes(timeDifferenceInSeconds) *60)

        val parsedTimeDifference: String = s"${day} jours ${hours} heures ${minute} minutes ${second} secondes"

        TicketsOpenedFromSimplifiedRequest(uid, createdParsed, agency, poorRequest, user, parsedTimeDifference, timeDifferenceInSeconds.toString())

      }
    }
  }

  /**
   * CustomReader Utiliser pour l'export de DetailedTicket.getUsersFromTickets
   *
   * @param user
   * @param agency
   */
  case class UsersFromTickets(user: User, agency: Agency) extends AbstractCustomReader

  object UsersFromTickets {
    implicit object UsersFromTicketsReader extends BSONDocumentReader[UsersFromTickets] {
      def read(doc: BSONDocument): UsersFromTickets = {
        val user = doc.getAs[User]("user").getOrElse(User("non renseigné", "non renseigné", "non renseigné", "non renseigné"))
        val agency = doc.getAs[Agency]("firstAgency").getOrElse(Agency("non renseigné", "non renseigné"))

        UsersFromTickets(user, agency)

      }
    }
  }

}
