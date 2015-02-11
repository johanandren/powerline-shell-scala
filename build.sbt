import AssemblyKeys._

// Project settings
organization := "com.markatta"

name := "powerline-shell-scala"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.4"

scalacOptions += "-deprecation"

resolvers ++= Seq(
  "jgit-repository" at "http://download.eclipse.org/jgit/maven",
  "Sonatype Nexus releases" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  // JGit for interacting with Git repo
  "org.eclipse.jgit" % "org.eclipse.jgit" % "3.0.0.201306101825-r",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  // Akuma -- provides daemonization on posix os:es
  "org.kohsuke" % "akuma" % "1.9"
)

// sbt-assembly plugin settings
assemblySettings

Revolver.settings
