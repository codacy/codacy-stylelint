ThisBuild / scalaVersion := "2.13.10"

ThisBuild / libraryDependencies += "com.codacy" %% "codacy-engine-scala-seed" % "6.1.0"
ThisBuild / libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.9.2"

name := "codacy-stylelint"

lazy val `doc-generator` = project.settings(libraryDependencies += "com.vladsch.flexmark" % "flexmark-all" % "0.64.6")

enablePlugins(JavaAppPackaging)

Universal / javaOptions ++= Seq("-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-XX:MaxRAMPercentage=90.0")
