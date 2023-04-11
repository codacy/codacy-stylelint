ThisBuild / scalaVersion := "2.13.7"

ThisBuild / libraryDependencies += "com.codacy" %% "codacy-engine-scala-seed" % "5.0.1"

name := "codacy-stylelint"

lazy val `doc-generator` = project.settings(libraryDependencies += "com.vladsch.flexmark" % "flexmark-all" % "0.50.20")

enablePlugins(JavaAppPackaging)

Universal / javaOptions ++= Seq("-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-XX:MaxRAMPercentage=90.0")
