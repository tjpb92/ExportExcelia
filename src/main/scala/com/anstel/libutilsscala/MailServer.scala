package com.anstel.libutilsscala

/**
 * Classe définissant l'objet MailServer
 *
 * @param name        : nom de l'environnement de travail
 * @param host        : nom ou adresse IP du serveur de messagerie
 * @param port        : numéro du port
 * @param username : identifiant de l'utilisateur
 * @param password    : mot de passe de l'utilisateur
 * @param fromAddress : adresse mail de l'émetteur
 * @param toAddress   : adresse mail du destinataire
 * @author Thierry Baribaud
 * @version 1.01
 */
case class MailServer(override val name: String, override val host: String, override val port: Int,
                      authentication: String, override val username: String, override val password: String,
                      fromAddress: String, toAddress: String) extends Server (name, host, port, username, password)

object MailServer {
  def builder(serverType: String, service: String, applicationProperties: ApplicationProperties): Either[String, MailServer] = {
    println("Construction d'un MailServer ...")
    val result = for {
      name <- applicationProperties.getPropertyValue(serverType, service, "name")
      host <- applicationProperties.getPropertyValue(serverType, service, "host")
      port <- Server.checkPortNumber(applicationProperties.getPropertyValue(serverType, service, "port"))
      authentication <- applicationProperties.getPropertyValue(serverType, service, "authentication")
      username <- applicationProperties.getPropertyValue(serverType, service, "username")
      passwd <- applicationProperties.getPropertyValue(serverType, service, "passwd")
      fromAddress <- applicationProperties.getPropertyValue(serverType, service, "fromAddress")
      toAddress <- applicationProperties.getPropertyValue(serverType, service, "toAddress")
    } yield MailServer(name, host, port, authentication, username, passwd, fromAddress, toAddress)
    result match {
      case Left(value) => println(s"Erreur lors de la construction : $value")
      case Right(value) => println(s"${value} contruit")
    }
    result
  }
}