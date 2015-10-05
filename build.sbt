import AssemblyKeys._

// Project settings
organization := "com.markatta"

name := "powerline-shell-scala"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.7"
scalacOptions += "-deprecation"

resolvers ++= Seq(
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "Sonatype Nexus releases" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

fork in run := true
javaOptions in run ++= Seq("-Xmx15m", "-Djava.awt.headless=true")

// sbt-assembly plugin settings
assemblySettings

Revolver.settings
