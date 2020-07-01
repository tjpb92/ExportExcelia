package com.anstel.libutilscalatest

import com.anstel.libutilsscala.ApplicationParameters
import com.anstel.libutilsscala.ApplicationParameters._
import org.junit.Assert._
import org.junit.Test

/**
 * Jeux de tests pour la classe ApplicationParameters
 *
 * @author Thierry Baribaud
 * @version 1.02
 */
class ApplicationParametersTest {

  /**
   * Imprime un titre suivi de suffisamment de tirets pour faire 80 caractères
   *
   * @param title : titre à afficher
   */
  def printTitle(title: String): Unit = {
    println("\n" + title + " " + "-" * (128 - title.length - 1))
  }

  /**
   * Simule aucun paramètre en ligne de commande
   */
  val args: Array[String] = Array()

  /**
   * Construction de l'objet ApplicationParameters en conséquence
   */
  val applicationParameters: ApplicationParameters = ApplicationParameters.builder(args) match {
    //    case Left(value) => println(value)
    case Right(value) => value
  }

  // Règles métier -----------------------------------------------------------------------------------------------------
  @Test
  def begDateShouldBeAnteriorToEndDate(): Unit = {
    val args: Array[String] = Array("-b", "05/05/2020", "-e", "01/05/2020")
    printTitle(s"La date de début doit être antérieure à la date de fin")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  @Test
  def b_e_n_SwitchForbiden(): Unit = {
    val args: Array[String] = Array("-b", "01/05/2020", "-e", "05/05/2020", "-n", "10")
    printTitle(s"Utilisation conjointe des commutateurs -b, -e et -n interdite")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  @Test
  def client_clientUid_SwitchForbiden(): Unit = {
    val args: Array[String] = Array("-client", "Anstel", "-clientUuid", "10-11-12-14")
    printTitle(s"Utilisation conjointe des commutateurs -client et -clientUuid interdite")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  // Tests du paramètre définissant la base de données -----------------------------------------------------------------
  @Test
  def defaultDbAsDefault(): Unit = {
    printTitle(s"Base de données par défaut attendue : $DEFAULT_DB")
    assertEquals(DEFAULT_DB, applicationParameters.db)
  }

  @Test
  def undefinedDbShoulBeRejected(): Unit = {
    val args: Array[String] = Array("-db")
    printTitle(s"Rejet d'une base de données non définie")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  @Test
  def dbFollowedBySwitchShoulbeRejected(): Unit = {
    val args: Array[String] = Array("-db", "-bad")
    printTitle(s"Rejet d'une base de données non définie (cas commutateur)")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  // Tests du paramètre définissant la date de début -------------------------------------------------------------------
  @Test
  def defaultBegDateAsDefault(): Unit = {
    printTitle(s"Date de début par défaut attendue : $DEFAULT_BEGDATE")
    assertEquals(DEFAULT_BEGDATE, applicationParameters.begDate)
  }

  @Test
  def undefinedBegDateShoulbeRejected(): Unit = {
    val args: Array[String] = Array("-b")
    printTitle(s"Date de début non définie rejetée")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  @Test
  def begDateFollowedBySwitchShoulbeRejected(): Unit = {
    val args: Array[String] = Array("-b", "-bad")
    printTitle(s"Date de début rejetée si immédiatement suivi d'un commutateur")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  @Test
  def badBegDateShoulbeRejected(): Unit = {
    val args: Array[String] = Array("-b", "01/05/Vingt")
    printTitle(s"Mauvaise date de début rejetée")
    val applicationParameters = ApplicationParameters.builder(args)
    assertTrue(applicationParameters.isLeft)
  }

  // Tests du paramètre définissant la date de fin ---------------------------------------------------------------------
  @Test
  def defaultEndDateAsDefault(): Unit = {
    printTitle(s"Date de fin par défaut attendue : $DEFAULT_ENDDATE")
    assertEquals(DEFAULT_ENDDATE, applicationParameters.endDate)
  }

  // Tests du paramètre définissant le nombre de jours -----------------------------------------------------------------
  @Test
  def nbDayShouldBeUndefined(): Unit = {
    printTitle(s"Nombre de jour par défaut non défini")
    assertEquals(None, applicationParameters.nbDay)
  }

  // Tests du paramètre définissant le centre d'appel ------------------------------------------------------------------
  @Test
  def defaultCallCenterAsDefault(): Unit = {
    printTitle(s"Centre d'appel par défaut attendue : $DEFAULT_CALL_CENTER")
    assertEquals(DEFAULT_CALL_CENTER, applicationParameters.callCenter)
  }

  // Tests du paramètre définissant le client à filtrer ----------------------------------------------------------------
  @Test
  def clientShouldBeUndefined(): Unit = {
    printTitle(s"Client par défaut non défini")
    assertEquals(None, applicationParameters.client)
  }

  // Tests du paramètre définissant l'identifiant client à filtrer -----------------------------------------------------
  @Test
  def clientUuidShouldBeUndefined(): Unit = {
    printTitle(s"Identifiant client par défaut non défini")
    assertEquals(None, applicationParameters.clientUuid)
  }

  // Tests du paramètre définissant le répertoire ----------------------------------------------------------------------
  @Test
  def defaultPathAsDefault(): Unit = {
    printTitle(s"Répertoire par défaut attendu : $DEFAULT_PATH")
    assertEquals(DEFAULT_PATH, applicationParameters.path)
  }

  // Tests du paramètre définissant le fichier -------------------------------------------------------------------------
  @Test
  def defaultFilenameAsDefault(): Unit = {
    printTitle(s"Fichier par défaut attendu : $DEFAULT_FILENAME")
    assertEquals(DEFAULT_FILENAME, applicationParameters.filename)
  }

  // Tests du paramètre définissant le suffixe -------------------------------------------------------------------------
  @Test
  def suffixShouldBeUndefined(): Unit = {
    printTitle(s"Suffixe par défaut non défini")
    assertEquals(None, applicationParameters.suffix)
  }

  // Tests du paramètre définissant le mode debug ----------------------------------------------------------------------
  @Test
  def debugModeShouldBeDefaultDebugMode(): Unit = {
    printTitle(s"Mode débug par défaut attendu : $DEFAULT_DEBUG_MODE")
    assertEquals(DEFAULT_DEBUG_MODE, applicationParameters.debugMode)
  }

  // Tests du paramètre définissant le test debug ----------------------------------------------------------------------
  @Test
  def testModeShouldBeDefaultTestMode(): Unit = {
    printTitle(s"Test débug par défaut attendu : $DEFAULT_TEST_MODE")
    assertEquals(DEFAULT_TEST_MODE, applicationParameters.testMode)
  }
}
