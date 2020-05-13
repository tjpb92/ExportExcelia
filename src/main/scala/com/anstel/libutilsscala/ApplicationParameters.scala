package com.anstel.libutilsscala

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scala.util.{Failure, Success, Try}

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
 * @version 1.01
 */
case class ApplicationParameters(db: String = ApplicationParameters.DEFAULT_DB,
                                 begDate: LocalDate = ApplicationParameters.DEFAULT_BEGDATE,
                                 endDate: LocalDate = ApplicationParameters.DEFAULT_ENDDATE,
                                 nbDay: Option[Int],
                                 callCenter: String = ApplicationParameters.DEFAULT_CALL_CENTER,
                                 client: Option[String],
                                 clientUuid: Option[String],
                                 path: String = ApplicationParameters.DEFAULT_PATH,
                                 filename: String = ApplicationParameters.DEFAULT_FILENAME,
                                 suffix: Option[String],
                                 debugMode: Boolean = ApplicationParameters.DEFAULT_DEBUG_MODE,
                                 testMode: Boolean = ApplicationParameters.DEFAULT_TEST_MODE)

object ApplicationParameters {

  // Valeurs par défaut pour les attributs
  val DEFAULT_DB = "dev"
  val DEFAULT_ENDDATE = LocalDate.now()
  val DEFAULT_BEGDATE = DEFAULT_ENDDATE.minusDays(7)
  val DEFAULT_CALL_CENTER = "Excelia"
  val DEFAULT_PATH = "."
  val DEFAULT_FILENAME = "Extract.xlsx"
  val DEFAULT_DEBUG_MODE = false
  val DEFAULT_TEST_MODE = false

  // Formatage des dates
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
    } yield ApplicationParameters(db.getOrElse(DEFAULT_DB), begDate.getOrElse(DEFAULT_BEGDATE),
      endDate.getOrElse(DEFAULT_ENDDATE),
      nbDay, callCenter.getOrElse(DEFAULT_CALL_CENTER),
      client, clientUuid,
      path.getOrElse(DEFAULT_PATH), filename.getOrElse(DEFAULT_FILENAME), suffix,
      debugMode.getOrElse(DEFAULT_DEBUG_MODE), testMode.getOrElse(DEFAULT_TEST_MODE))
    val validatedResult = validate(result)
    validatedResult match {
      case Left(value) => usage()
      case Right(value) => println(s"${value} lus.")
    }
    validatedResult
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
    else if (ip1 >= args.length || isSwitch(args(ip1))) Left(s"ERREUR : paramètre $parameter n'est pas défini")
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
    else if (ip1 >= args.length || isSwitch(args(ip1))) Left(s"ERREUR : paramètre $parameter n'est pas défini")
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
    else if (ip1 >= args.length || isSwitch(args(ip1))) Left(s"ERREUR : paramètre $parameter n'est pas défini")
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

  /**
   * Valide la bonne construction de l'objet ApplicationParameters
   *
   * @param applicationParameters : paramètres de l'application
   * @return les paramètres de l'application ou un message d'erreur
   */
  def validate(applicationParameters: Either[String, ApplicationParameters]): Either[String, ApplicationParameters] = {
    val result = applicationParameters match {
      case Right(value) => value
    }
    if (result.begDate.isAfter(result.endDate))
      Left(s"ERREUR : La date de début ${ddmmyyyyFormat.format(result.begDate)} doit être antérieure à la date de fin ${ddmmyyyyFormat.format(result.endDate)}")
    else
      applicationParameters
  }

  /**
   * Affiche les paramètres en ligne de commande autorisés
   */
  def usage(): Unit = {
    println("Usage : java -jar ExtractExcelia.jar [-db db] [[-b début] [-e fin]|-n nbJour]\n\t\t[-cc callCenter] [-client client|-clientUuid uuid]\n\t\t[-p path] [-f fichier] [-s suffixe] [-d] [-t]")
  }

}

