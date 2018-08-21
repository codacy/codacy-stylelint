import com.typesafe.sbt.packager.Keys.{
  daemonUser,
  defaultLinuxInstallLocation,
  dockerAlias,
  dockerBaseImage,
  dockerCmd,
  dockerEntrypoint,
  dockerUpdateLatest,
  maintainer,
  packageName
}
import com.typesafe.sbt.packager.docker.{Cmd, DockerAlias, ExecCmd}

name := "codacy-stylelint"

version := "1.0.0-SNAPSHOT"

val languageVersion = "2.12.4"

scalaVersion := languageVersion

resolvers := Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases") ++
  resolvers.value ++
  Seq("Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.codacy" %% "codacy-engine-scala-seed" % "3.0.183",
  "com.vladsch.flexmark" % "flexmark-all" % "0.34.8",
  "org.specs2" %% "specs2-core" % "4.2.0" % Test)

scalacOptions ++= Common.compilerFlags
scalacOptions.in(Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused:imports", "-Xfatal-warnings")
scalacOptions in Test ++= Seq("-Yrangepos")
enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

version in Docker := "1.0.0-SNAPSHOT"

organization := "com.codacy"

lazy val toolVersion = taskKey[String]("Retrieve the version of the underlying tool from patterns.json")
toolVersion := {
  import better.files.File
  import play.api.libs.json.{JsString, JsValue, Json}

  val jsonFile = resourceDirectory.in(Compile).value / "docs" / "patterns.json"
  val patternsJsonValues = Json.parse(File(jsonFile.toPath).contentAsString).as[Map[String, JsValue]]

  patternsJsonValues.collectFirst {
    case ("version", JsString(ver)) => ver
  }.getOrElse(throw new Exception("Failed to retrieve version from docs/patterns.json"))
}

mappings.in(Universal) ++= resourceDirectory
  .in(Compile)
  .map { resourceDir: File =>
    val src = resourceDir / "docs"
    val dest = "/docs"

    val docFiles = (for {
      path <- better.files.File(src.toPath).listRecursively()
      if !path.isDirectory
    } yield path.toJava -> path.toString.replaceFirst(src.toString, dest)).toSeq

    val scripts = Seq((file("./scripts/install.sh"), "install.sh"), (file(".stylelint-version"), ".stylelint-version"))

    docFiles ++ scripts
  }
  .value

def installAll() =
  s"""apk update &&
     |apk add bash curl nodejs-npm &&
     |./install.sh
     ||rm -rf /tmp/* &&
     |rm -rf /var/cache/apk/*
     |rm ./install.sh""".stripMargin.replaceAll(System.lineSeparator(), " ")

val defaultDockerInstallationPath = "/opt/docker"
mainClass in Compile := Some("codacy.Engine")
packageName in Docker := name.value
dockerAlias := DockerAlias(None, Some("codacy"), name.value, Some(version.value))
version in Docker := version.value
maintainer in Docker := "Rodrigo Fernandes <rodrigo@codacy.com>"
dockerBaseImage := "library/openjdk:8-jre-alpine"
dockerUpdateLatest := true
defaultLinuxInstallLocation in Docker := defaultDockerInstallationPath
daemonUser in Docker := "docker"
daemonGroup in Docker := "docker"
dockerEntrypoint := Seq(s"$defaultDockerInstallationPath/bin/${name.value}")
dockerCmd := Seq()
dockerCommands := dockerCommands.dependsOn(toolVersion).value.flatMap {
  case cmd @ Cmd("ADD", _) =>
    List(
      Cmd("RUN", "adduser -u 2004 -D docker"),
      Cmd("ENV", s"TOOL_VERSION $toolVersion"),
      Cmd("ENV", s"STYLELINT_CONFIG_BASEDIR /usr/lib/node_modules"),
      cmd,
      Cmd("RUN", installAll()),
      Cmd("RUN", "mv /opt/docker/docs /docs"),
      ExecCmd("RUN", Seq("chown", "-R", s"docker:docker", "/docs"): _*),
      Cmd("ENV", "NODE_PATH /usr/lib/node_modules")
    )
  case other => List(other)
}
