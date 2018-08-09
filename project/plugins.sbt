libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.6.0",
  "com.typesafe.play" %% "play-json" % "2.6.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.6")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.12")
