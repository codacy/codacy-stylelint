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
import sjsonnew._
import sjsonnew.BasicJsonProtocol._
import sjsonnew.support.scalajson.unsafe._
import java.nio.file.Files
import scala.collection.JavaConverters._
import Path.flat

organization := "com.codacy"

name := "codacy-stylelint"

val languageVersion = "2.12.8"

scalaVersion := languageVersion

resolvers := Seq("Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases") ++
  resolvers.value ++
  Seq("Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.codacy" %% "codacy-engine-scala-seed" % "3.0.296",
  "com.vladsch.flexmark" % "flexmark-all" % "0.50.20",
  "org.specs2" %% "specs2-core" % "4.6.0" % Test)

scalacOptions.in(Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused:imports", "-Xfatal-warnings")
scalacOptions in Test ++= Seq("-Yrangepos")
enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

lazy val toolVersion = settingKey[String]("The version of the underlying tool retrieved from patterns.json")
toolVersion := {
  case class Patterns(name: String, version: String)
  implicit val patternsIso: IsoLList[Patterns] =
    LList.isoCurried((p: Patterns) => ("name", p.name) :*: ("version", p.version) :*: LNil) {
      case (_, n) :*: (_, v) :*: LNil => Patterns(n, v)
    }

  val jsonFile = (resourceDirectory in Compile).value / "docs" / "patterns.json"
  val json = Parser.parseFromFile(jsonFile)
  val patterns = json.flatMap(Converter.fromJson[Patterns])
  patterns.map(_.version).getOrElse(throw new Exception("Failed to retrieve version from docs/patterns.json"))
}

lazy val stylelintVersionFile = Def.setting {
  val f = file(".stylelint-version")
  IO.write(f, toolVersion.value)
  f
}

mappings.in(Universal) ++= resourceDirectory
  .in(Compile)
  .zip(stylelintVersionFile)
  .map { case (resourceDir: File, versionFile: File) =>
    val src = resourceDir / "docs"
    val dest = "/docs"

    val docFiles = {
      val res = for {
        path <- Files.walk(src.toPath).iterator().asScala
        if !Files.isDirectory(path)
      } yield path.toFile -> path.toString.replaceFirst(src.toString, dest)
      res.toSeq
    }

    val scripts = Seq(file("./scripts/install.sh"), versionFile).pair(flat)
    
    docFiles ++ scripts
  }.value

def installAll() =
  s"""apk update &&
     |apk add bash curl npm &&
     |./install.sh
     ||rm -rf /tmp/* &&
     |rm -rf /var/cache/apk/*
     |rm ./install.sh""".stripMargin.replaceAll(System.lineSeparator(), " ")

val defaultDockerInstallationPath = "/opt/docker"
mainClass in Compile := Some("codacy.Engine")
packageName in Docker := name.value
version in Docker := version.value
maintainer in Docker := "Rodrigo Fernandes <rodrigo@codacy.com>"
dockerBaseImage := "library/openjdk:8-jre-alpine"
dockerUpdateLatest := true
defaultLinuxInstallLocation in Docker := defaultDockerInstallationPath
daemonUser in Docker := "docker"
daemonGroup in Docker := "docker"
dockerEntrypoint := Seq(s"$defaultDockerInstallationPath/bin/${name.value}")
dockerCmd := Seq()
dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("ADD", _) =>
    List(
      Cmd("RUN", "adduser -u 2004 -D docker"),
      Cmd("ENV", s"TOOL_VERSION ${toolVersion.value}"),
      Cmd("ENV", s"STYLELINT_CONFIG_BASEDIR /usr/lib/node_modules"),
      cmd,
      Cmd("RUN", installAll()),
      Cmd("RUN", "mv /opt/docker/docs /docs"),
      ExecCmd("RUN", Seq("chown", "-R", s"docker:docker", "/docs"): _*),
      Cmd("ENV", "NODE_PATH /usr/lib/node_modules")
    )
  case other => List(other)
}
