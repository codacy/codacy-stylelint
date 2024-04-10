resolvers += Resolver.jcenterRepo
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "25.2.0")

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.2")
