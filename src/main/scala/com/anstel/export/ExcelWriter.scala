package com.anstel.export

import java.io._
import java.util.Date

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel._

import com.anstel.libutilsscala.ApplicationParameters

/**
 * Objet pour la création du fichier excel
 *
 * @author William Machi
 *  @version 1.0
 */
object ExcelWriter {

  /**
   *
   * @param file: Les données a écrire dans le fichier reçu depuis un CaseClassReader
   * @param applicationParameters: paramétre de l'application utilisé pour récupérer le chemin et le nom du fichier
   */
  def excelExport(file: List[List[String]], applicationParameters: ApplicationParameters): Unit = {

    println("création du fichier excel")
    println(s"chemin de destination ${applicationParameters.path}")

    val workbook: XSSFWorkbook = new XSSFWorkbook()

    val sheet: Sheet = workbook.createSheet("test")

    // Create a Font for styling header cells
    val headerFont: Font = workbook.createFont()
    headerFont.setBold(true)
    headerFont.setFontHeightInPoints(14.toShort)
    headerFont.setColor(IndexedColors.RED.getIndex)

    // Create a CellStyle with the font
    val headerCellStyle = workbook.createCellStyle
    headerCellStyle.setFont(headerFont)

    for(line <- file) {
      val row: Row = sheet.createRow(file.indexOf(line))

      if(file.indexOf(line) == 0) {
        for (i <- 0 until line.length) {
          val headerCellStyle = workbook.createCellStyle
          headerCellStyle.setFont(headerFont)
          row.createCell(i).setCellValue(line(i))
        }
      }  else {
        for (i <- 0 until line.length) {
          row.createCell(i).setCellValue(line(i))
        }
      }
    }

    //sheet.autoSizeColumn(i) faire une fois le fichier construit

    for (i <- 0 until file.length){ sheet.autoSizeColumn(i) }

    // Write the output to a file// Write the output to a file

    val fileOut: FileOutputStream = new FileOutputStream(new File(applicationParameters.path + "/" + applicationParameters.filename))
    workbook.write(fileOut)
    fileOut.close()

    // Closing the workbook
    workbook.close()

  }

}