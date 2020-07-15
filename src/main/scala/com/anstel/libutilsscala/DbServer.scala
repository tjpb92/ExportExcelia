package com.anstel.libutilsscala

/**
 * Classe définissant l'objet DbServer
 *
 * @param name     : nom de l'environnement de travail
 * @param host     : nom ou adresse IP du serveur de base de données
 * @param port     : numéro du port
 * @param username : identifiant de l'utilisateur
 * @param password : mot de passe de l'utilisateur
 * @param database : base de données
 * @author Thierry Baribaud
 * @version 1.01
 */
case class DbServer(override val name: String, override val host: String, override val port: Int,
                    override val username: String, override val password: String, database: String)
  extends Server(name, host, port, username, password)

object DbServer {
  def builder(serverType: String, service: String, applicationProperties: ApplicationProperties): Either[String, DbServer] = {
    println("Construction d'un DbServer ...")
    val result = for {
      name <- applicationProperties.getPropertyValue(serverType, service, "name")
      host <- applicationProperties.getPropertyValue(serverType, service, "host")
      port <- Server.checkPortNumber(applicationProperties.getPropertyValue(serverType, service, "port"))
      username <- applicationProperties.getPropertyValue(serverType, service, "username")
      passwd <- applicationProperties.getPropertyValue(serverType, service, "passwd")
      database <- applicationProperties.getPropertyValue(serverType, service, "database")
    } yield DbServer(name, host, port, username, passwd, database)
    result match {
      case Left(value) => println(s"Erreur lors de la construction : $value")
      case Right(value) => println(s"${value} construit")
    }
    result
  }
}