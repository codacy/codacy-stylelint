resolvers += Resolver.jcenterRepo
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "17.0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.15")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "2.3")
  