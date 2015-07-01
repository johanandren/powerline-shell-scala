import AssemblyKeys._

// Project settings
organization := "com.markatta"

name := "powerline-shell-scala"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.6"

scalacOptions += "-deprecation"

resolvers ++= Seq(
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "Sonatype Nexus releases" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  // JGit for interacting with Git repo
  "org.eclipse.jgit" % "org.eclipse.jgit" % "3.6.2.201501210735-r",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

// sbt-assembly plugin settings
assemblySettings

Revolver.settings
