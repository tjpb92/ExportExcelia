name := "ExportExcelia"

version := "0.1"

scalaVersion := "2.13.2"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.20.3",
  "org.slf4j" % "slf4j-simple" % "1.6.4",
  "org.apache.poi" % "poi-ooxml" % "4.1.2",
  "org.scalactic" %% "scalactic" % "3.1.2",
  "org.scalatest" %% "scalatest" % "3.1.2" % "test"
)

fork in run := true