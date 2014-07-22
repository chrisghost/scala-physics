name := "scala-physics"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  cache
)

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.10.4")
