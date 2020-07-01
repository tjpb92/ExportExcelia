package com.anstel.libutilsscala

import java.io.FileInputStream
import java.util.Properties

import scala.util.{Failure, Success, Try}

class ApplicationProperties(filename: String) extends Properties {

  println("Lecture du fichier des propriétés d'application" + filename + " ...")
  load(new FileInputStream(filename))
  println("Proppriétés d'application lues.")

  def getPropertyValue(serverType: String, service: String, propertyName: String): Either[String, String] = {
    val key = s"$serverType.$service.$propertyName"
    val property = getProperty(key)
    if (property == null) Left(s"ERREUR : Propriété $key non définie $propertyName")
    else if (property.isEmpty) Left(s"ERREUR : Propriété $key vide")
    else Right(property)
  }
}
