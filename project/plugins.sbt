resolvers += Resolver.jcenterRepo
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "20.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.15")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
