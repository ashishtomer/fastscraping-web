name := """fastscraping-web"""
organization := "com.fastscraping"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.10"

libraryDependencies ++= Dependencies.projectDependencies

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.fastscraping.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.fastscraping.binders._"
