package com.anstel.libutilsscala

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.{Failure, Success, Try}
import com.anstel.libutilsscala.ApplicationParameters._
/**
 * Classe définissant l'objet ApplicationParameters
 * Récupère les paramètres en ligne de commune
 *
 * @param db         : base de données de travail (dev, prod, test, train), dev par défaut,
 * @param begDate    : date de début de l'extraction à 0h au format JJ/MM/AAAA. Amorcée à une semaine en arrière par défaut (paramètre optionnel),
 * @param endDate    : date de fin de l'extraction à 0h au format JJ/MM/AAAA. Amorcée à aujourd'hui par défaut (paramètre optionnel),
 * @param nbDay      : précise le nombre de jour(s) à compter de la date courante. Désactivé par défaut (paramètre optionnel),
 * @param callCenter : est la référence au centre d'appel. Armorcée à Excelia par défaut (paramètre optionnel),
 * @param client     : est la référence au client servant à filtrer les résultats. Désactivé par défaut (paramètre optionnel),
 * @param clientUuid : est l'identifiant unique du client servant à filtrer les résultats. Désactivé par défaut (paramètre optionnel),
 * @param path       : est le répertoire vers lequel exporter les fichiers contenant les résultats. Par défaut c'est le répertoire courant du programme (paramètre optionnel),
 * @param filename   : est le fichier Excel qui recevra les résultats. Par défaut, c'est le fichier *Export.xlsx* (paramètre optionnel),
 * @param suffix     : est le suffixe à ajouter au nom du fichier. Par défaut il n'y a pas de suffixe (paramètre optionnel),
 * @param debugMode  : le programme s'exécute en mode débug, il est beaucoup plus verbeux. Désactivé par défaut (paramètre optionnel).
 * @param testMode   : le programme s'exécute en mode test, les transactions en base de données ne sont pas faites. Désactivé par défaut (paramètre optionnel).
 * @author Thierry Baribaud
 * @version 1.02
 */
case class ApplicationParameters(db: String = DEFAULT_DB,
                                 begDate: LocalDate = DEFAULT_BEGDATE, endDate: LocalDate = DEFAULT_ENDDATE,
                                 nbDay: Option[Int],
                                 callCenter: String = DEFAULT_CALL_CENTER,
                                 client: Option[String], clientUuid: Option[String],
                                 path: String = DEFAULT_PATH, filename: String = DEFAULT_FILENAME, suffix: Option[String],
                                 debugMode: Boolean = DEFAULT_DEBUG_MODE, testMode: Boolean = DEFAULT_TEST_MODE)

object ApplicationParameters {

  /**
   * Valeurs par défaut pour les attributs
   */
  val DEFAULT_DB = "dev"
  val DEFAULT_ENDDATE: LocalDate = LocalDate.now()
  val DEFAULT_BEGDATE: LocalDate = DEFAULT_ENDDATE.minusDays(7)
  val DEFAULT_CALL_CENTER = "Excelia"
  val DEFAULT_PATH = "."
  val DEFAULT_FILENAME = "Extract.xlsx"
  val DEFAULT_DEBUG_MODE = false
  val DEFAULT_TEST_MODE = false

  /**
   * Formatage des dates
   */
  val ddmmyyyyFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  /**
   * Constructeur de la classe ApplicationParameters
   *
   * @param args : paramètres en ligne de commande
   * @return un objet ApplicationParameters en cas de succès, un message d'erreur sinon.
   */
  def builder(args: Array[String]): Either[String, ApplicationParameters] = {
    println("Lecture des paramètres en ligne de commande ...")
    val result = for {
      db <- getStringParameter("-db", args)
      begDate <- getLocalDateParameter("-b", args)
      endDate <- getLocalDateParameter("-e", args)
      nbDay <- getIntParameter("-n", args)
      callCenter <- getStringParameter("-cc", args)
      client <- getStringParameter("-client", args)
      clientUuid <- getStringParameter("-clientUuid", args)
      path <- getStringParameter("-path", args)
      filename <- getStringParameter("-file", args)
      suffix <- getStringParameter("-s", args)
      debugMode <- getBooleanParameter("-d", args)
      testMode <- getBooleanParameter("-t", args)
      applicationParameters <- checkAndCreate(db, begDate, endDate, nbDay, callCenter, client, clientUuid, path, filename, suffix,
        debugMode, testMode)
    } yield applicationParameters
    result match {
      case Left(value) =>
        println(value)
        usage()
      case Right(value) => println(s"""$value lus.""")
    }
    result
  }

  /**
   * Vérifie si le paramètre est un commutateur
   *
   * @param parameter : paramètre à vérifier
   * @return indique si le paramètre est un commutateur
   */
  def isSwitch(parameter: String): Boolean = parameter(0) == '-'

  /**
   * Récupère un paramètre donné avec une valeur de type chaine de caractères s'il existe
   *
   * @param parameter : paramètre à rechercher
   * @param args      : paramètres en ligne de commande
   * @return la valeur associée au paramètre
   */
  def getStringParameter(parameter: String, args: Array[String]): Either[String, Option[String]] = {
    val i = args.indexOf(parameter)
    val ip1 = i + 1

    if (i == -1) Right(None)
    else if (ip1 >= args.length) Left(s"ERREUR : paramètre $parameter n'est pas défini")
    else if (isSwitch(args(ip1))) Left(s"ERREUR : paramètre $parameter n'est pas défini (commutateur à la place)")
    else Right(Some(args(ip1)))
  }

  /**
   * Récupère un paramètre donné avec une valeur de type entier s'il existe
   *
   * @param parameter : paramètre à rechercher
   * @param args      : paramètres en ligne de commande
   * @return la valeur associée au paramètre
   */
  def getIntParameter(parameter: String, args: Array[String]): Either[String, Option[Int]] = {
    val i = args.indexOf(parameter)
    val ip1 = i + 1

    if (i == -1) Right(None)
    else if (ip1 >= args.length) Left(s"ERREUR : paramètre $parameter n'est pas défini")
    else if (isSwitch(args(ip1))) Left(s"ERREUR : paramètre $parameter n'est pas défini (commutateur à la place)")
    else {
      Try(Integer.parseInt(args(ip1))) match {
        case Failure(exception) => Left(s"ERREUR : la valeur '${args(ip1)}' du paramètre $parameter n'est pas numérique")
        case Success(value) => Right(Some(value))
      }
    }
  }

  /**
   * Récupère un paramètre donné avec une valeur de type date s'il existe
   *
   * @param parameter : paramètre à rechercher
   * @param args      : paramètres en ligne de commande
   * @return la valeur associée au paramètre
   */
  def getLocalDateParameter(parameter: String, args: Array[String]): Either[String, Option[LocalDate]] = {
    val i = args.indexOf(parameter)
    val ip1 = i + 1

    if (i == -1) Right(None)
    else if (ip1 >= args.length) Left(s"ERREUR : paramètre $parameter n'est pas défini")
    else if (isSwitch(args(ip1))) Left(s"ERREUR : paramètre $parameter n'est pas défini (commutateur à la place)")
    else {
      Try(LocalDate.parse(args(ip1), ddmmyyyyFormat)) match {
        case Failure(exception) => Left(s"ERREUR : la valeur '${args(ip1)}' du paramètre $parameter n'est pas une date")
        case Success(value) => Right(Some(value))
      }
    }
  }

  /**
   * Récupère un paramètre donné avec une valeur de type booléen s'il existe
   *
   * @param parameter : paramètre à rechercher
   * @param args      : paramètres en ligne de commande
   * @return la valeur associée au paramètre
   */
  def getBooleanParameter(parameter: String, args: Array[String]): Either[String, Option[Boolean]] = {
    val i = args.indexOf(parameter)

    if (i == -1) Right(None)
    else Right(Some(true))
  }

  def checkAndCreate(db: Option[String], begDate: Option[LocalDate], endDate: Option[LocalDate], nbDay: Option[Int],
                     callCenter: Option[String], client: Option[String], clientUuid: Option[String],
                     path: Option[String], filename: Option[String], suffix: Option[String],
                     debugMode: Option[Boolean], testMode: Option[Boolean]) : Either[String, ApplicationParameters] = for {
    checkedDb <- Right(db.getOrElse(DEFAULT_DB))
    checkedBegDate <- Right(begDate.getOrElse(DEFAULT_BEGDATE))
    checkedEndDate <- Right(endDate.getOrElse(DEFAULT_ENDDATE))
    checkedNbDay <- Right(nbDay)
    checkedCallCenter <- Right(callCenter.getOrElse(DEFAULT_CALL_CENTER))
    checkedClient <- Right(client)
    checkedClientUuid <- Right(clientUuid)
    checkedPath <- Right(path.getOrElse(DEFAULT_PATH))
    checkedFilename <- Right(filename.getOrElse(DEFAULT_FILENAME))
    checkedSuffix <- Right(suffix)
    checkedDebugMode <- Right(debugMode.getOrElse(DEFAULT_DEBUG_MODE))
    checkedTestMode <- Right(testMode.getOrElse(DEFAULT_TEST_MODE))
    checkedDatesFilter <- checkDatesFilter(checkedBegDate, checkedEndDate)
    checkedClientFilter <- checkClientFilter(checkedClient, checkedClientUuid)
    checkedClientDatesSelector <- checkDatesSelector(begDate, endDate, nbDay)
  } yield ApplicationParameters(checkedDb, checkedBegDate, checkedEndDate, checkedNbDay, checkedCallCenter ,
    checkedClient, checkedClientUuid, checkedPath, checkedFilename, checkedSuffix, checkedDebugMode, checkedTestMode)

  /**
   * Vérifie que la date de début est antérieure à la date de fin
   * @param begDate : date de début
   * @param endDate : date de fin
   * @return condition vérifiée (true) ou un message d'erreur
   */
  def checkDatesFilter(begDate: LocalDate, endDate: LocalDate): Either[String, Boolean] =
    if (begDate.isAfter(endDate))
      Left(s"ERREUR : La date de début ${ddmmyyyyFormat.format(begDate)} doit être antérieure à la date de fin ${ddmmyyyyFormat.format(endDate)}")
    else Right(true)

  /**
   * Vérifie la non utilisation conjointe des options -client et -clientUuid :
   *
   * @param client     : le nom du client
   * @param clientUuid : l'identifiant unique du client
   * @return condition vérifiée (true) ou un message d'erreur
   */
  def checkClientFilter(client: Option[String], clientUuid: Option[String]): Either[String, Boolean] =
    if (client.isEmpty || clientUuid.isEmpty) Right(true)
    else Left("Erreur : ne pas utiliser -client et -clientUuid en même temps")

  /**
   * Vérifie la non utilisation conjointe des options -n avec -b ou -e
   * @param begDate : date de début
   * @param endDate : date de fin
   * @param nbDay   : nombre de jours par rapport à aujourd'hui
   * @return condition vérifiée (true) ou un message d'erreur
   */
  def checkDatesSelector(begDate: Option[LocalDate], endDate: Option[LocalDate], nbDay:Option[Int]): Either[String, Boolean] =
    if ((begDate.isDefined || endDate.isDefined) && nbDay.isDefined)
      Left("Erreur : ne pas utiliser -n en même temps que -b ou -e")
    else Right(true)

  /**
   * Affiche les paramètres en ligne de commande autorisés
   */
  def usage(): Unit = {
    println("Usage : java -jar ExtractExcelia.jar [-db db] [[-b début] [-e fin]|-n nbJour]\n\t\t[-cc callCenter] [-client client|-clientUuid uuid]\n\t\t[-p path] [-f fichier] [-s suffixe] [-d] [-t]")
  }

}
