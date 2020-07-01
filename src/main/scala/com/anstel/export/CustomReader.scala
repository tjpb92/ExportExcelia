package com.anstel.export

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader}

/**
 * Objet qui regroupe les CustomReader utilisé par les modéles pour renvoyer les résultats des requétes
 *
 * @author William Machi
 *  @version 1.0
 */
object CustomReader {

  /**
   * Class abstraite pour pour conformer le type des CustomReader
   */
  abstract class AbstractCustomReader

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
        val requestDate = doc.getAs[String]("requestDate").get
        val requesterName = doc.getAs[Requester]("requester").get
        val patrimony = doc.getAs[List[Patrimony]]("patrimony").get

        Request(uid, requestDate, requesterName, patrimony)
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
   * CustomReader Utilisé pour lire les données de l'utilisateur qui a ouvert le ticket
   *
   * @param uid
   * @param firstName
   * @param lastName
   * @param userType
   */
  case class User(uid: String, firstName: String, lastName: String, userType: String)

  object User {
    implicit object UserReader extends BSONDocumentReader[User] {
      def read(doc: BSONDocument): User = {
        val uid = doc.getAs[String]("uid").getOrElse("non renseigné")
        val firstName = doc.getAs[String]("firstName").getOrElse("non renseigné")
        val lastName = doc.getAs[String]("lastName").getOrElse("non renseigné")
        val userType = doc.getAs[String]("userType").getOrElse("non renseigné")

        User(uid, firstName, lastName, userType)
      }
    }
  }

  /**
   *  CustomReader Utiliser pour l'export de DetailedTicket.getTicketsOpenedFromSimplifiedRequest
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
        val created = doc.getAs[BSONDateTime]("created").get
        val agency = doc.getAs[Agency]("firstAgency").get
        val poorRequest = doc.getAs[PoorRequest]("request").get
        val user = doc.getAs[User]("user").getOrElse(User("non renseigné", "non renseigné", "non renseigné", "non renseigné"))

        val createdParsed: String = created match {
          case BSONDateTime(value) => Instant.ofEpochMilli(value).toString()
        }

        // Calcul de la différence de temps entre le requéte et le ticket

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
        val agency = doc.getAs[Agency]("firstAgency").get

        UsersFromTickets(user, agency)

      }
    }
  }

}
