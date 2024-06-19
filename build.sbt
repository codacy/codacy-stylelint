ThisBuild / scalaVersion := "2.13.13"

ThisBuild / libraryDependencies ++= Seq(
  "com.codacy" %% "codacy-engine-scala-seed" % "6.1.3",
  "com.github.pathikrit" %% "better-files" % "3.9.2")

name := "codacy-stylelint"

lazy val `doc-generator` = project.settings(libraryDependencies += "com.vladsch.flexmark" % "flexmark-all" % "0.64.8")

dockerBaseImage := "openjdk:11-jdk"

enablePlugins(JavaAppPackaging)

Universal / javaOptions ++= Seq("-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-XX:MaxRAMPercentage=90.0")
