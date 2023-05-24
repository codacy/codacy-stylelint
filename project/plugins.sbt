resolvers += Resolver.jcenterRepo
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "25.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
