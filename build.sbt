name := """postal"""
organization := "com.quatrolabs"
maintainer := "carlos@quatrolabs.com"

version := "0.1.2"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.8"

libraryDependencies += guice
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.11.488"
libraryDependencies += "com.sun.mail" % "javax.mail" % "1.6.2"
libraryDependencies += "org.apache.commons" % "commons-email" % "1.5"
libraryDependencies += "org.bouncycastle" % "bcprov-jdk16" % "1.46"
libraryDependencies += "org.bouncycastle" % "bcprov-ext-jdk16" % "1.46"
libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.61"
libraryDependencies += "org.bouncycastle" % "bcpkix-jdk15on" % "1.61"
libraryDependencies += "org.apache.logging.log4j" % "log4j" % "2.11.2"
libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.8"
libraryDependencies += "com.googlecode.usc" % "jdbcdslog" % "1.0.6.2"
libraryDependencies += "com.auth0" % "java-jwt" % "3.10.2"

includeFilter in (Assets, LessKeys.less) := "*.less"
excludeFilter in (Assets, LessKeys.less) := "_*.less"
