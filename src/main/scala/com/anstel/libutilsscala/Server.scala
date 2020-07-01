package com.anstel.libutilsscala

import scala.util.{Failure, Success, Try}

/**
 * Classe abstraite définissant l'objet Server
 *
 * @param name     : nom de l'environnement de travail
 * @param host     : nom ou adresse IP du serveur de base de données
 * @param port     : numéro du port
 * @param username : identifiant de l'utilisateur
 * @param password : mot de passe de l'utilisateur
 * @author Thierry Baribaud
 * @version 1.01
 */
abstract class Server(val name: String, val host: String, val port: Int, val username: String, val password: String)

object Server {
  /**
   * Fonction qui convertit un numéro de port contenu dans une chaine de caractères en entier long
   *
   * @param portAsString numéro de port à traduire en entier long
   * @return entier long traduit ou message d'erreur
   */
  def checkPortNumber(portAsString: Either[String, String]): Either[String, Int] = {
    portAsString match {
      case Left(value) => Left(value)
      case Right(value) => Try(Integer.parseInt(value)) match {
        case Failure(exception) => Left(s"ERREUR : le numéro de port '$value' n'est pas numérique")
        case Success(value) => Right(value)
      }
    }
  }
}

