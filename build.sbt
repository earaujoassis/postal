name := """postal"""
organization := "com.quatrolabs"

version := "1.0-SNAPSHOT"

autoCompilerPlugins := true

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.488"
libraryDependencies += "com.sun.mail" % "javax.mail" % "1.6.2"
