package com.anstel.export

import scala.collection.mutable.ListBuffer

class Aggregator {
  private val files: ListBuffer[List[List[String]]] = ListBuffer()

  def setFiles(file: List[List[String]]) = {
    files += file
  }

  def getFiles(): ListBuffer[List[List[String]]] = {
    return files
  }
}
