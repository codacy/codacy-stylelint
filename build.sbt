import java.nio.file.Files

import com.typesafe.sbt.packager.Keys.{
  daemonUser,
  defaultLinuxInstallLocation,
  dockerBaseImage,
  dockerCmd,
  dockerEntrypoint,
  dockerUpdateLatest,
  maintainer,
  packageName
}
import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import sjsonnew.BasicJsonProtocol._
import sjsonnew._
import sjsonnew.support.scalajson.unsafe._

import scala.collection.JavaConverters._

organization := "com.codacy"

name := "codacy-stylelint"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "com.codacy" %% "codacy-engine-scala-seed" % "4.0.0",
  "com.vladsch.flexmark" % "flexmark-all" % "0.50.20",
  "org.specs2" %% "specs2-core" % "4.6.0" % Test)

scalacOptions.in(Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused:imports", "-Xfatal-warnings")
scalacOptions in Test ++= Seq("-Yrangepos")
enablePlugins(JavaAppPackaging)

enablePlugins(DockerPlugin)

lazy val toolVersion = settingKey[String]("The version of the underlying tool retrieved from patterns.json")
toolVersion := {
  case class PackageJson(dependencies: Map[String, String])
  implicit val patternsIso: IsoLList[PackageJson] =
    LList.isoCurried((p: PackageJson) => ("dependencies", p.dependencies) :*: LNil) {
      case (_, d) :*: LNil => PackageJson(d)
    }

  val jsonFile = file("./package.json")
  val json = Parser.parseFromFile(jsonFile)
  val patterns = json.flatMap(Converter.fromJson[PackageJson])
  patterns
    .map(_.dependencies("stylelint").stripPrefix("^"))
    .getOrElse(throw new Exception("Failed to retrieve version from package.json"))
}

lazy val stylelintVersionFile = Def.setting {
  val f = file(".stylelint-version")
  IO.write(f, toolVersion.value)
  f
}

mappings.in(Universal) ++= resourceDirectory
  .in(Compile)
  .zip(stylelintVersionFile)
  .map {
    case (resourceDir: File, versionFile: File) =>
      val src = resourceDir / "docs"
      val dest = "/docs"

      val docs = for {
        path <- Files.walk(src.toPath).iterator().asScala
        if !Files.isDirectory(path)
      } yield path.toFile -> path.toString.replaceFirst(src.toString, dest)

      docs.toSeq
  }
  .value

val defaultDockerInstallationPath = "/opt/docker"
mainClass in Compile := Some("codacy.Engine")
packageName in Docker := name.value
version in Docker := version.value
maintainer in Docker := "Rodrigo Fernandes <rodrigo@codacy.com>"
dockerBaseImage := "codacy-stylelint-base:latest"
dockerUpdateLatest := true
defaultLinuxInstallLocation in Docker := defaultDockerInstallationPath
daemonUser in Docker := "docker"
daemonGroup in Docker := "docker"
dockerEntrypoint := Seq(s"$defaultDockerInstallationPath/bin/${name.value}")
dockerCmd := Seq()
dockerCommands := dockerCommands.value.flatMap {
  case cmd @ Cmd("ADD", _) =>
    List(
      ExecCmd("RUN", "adduser", "-u", "2004", "-D", "docker"),
      Cmd("ENV", s"STYLELINT_CONFIG_BASEDIR $defaultDockerInstallationPath/node_modules"),
      cmd,
      ExecCmd("RUN", s"mv", s"$defaultDockerInstallationPath/docs", "/docs"),
      ExecCmd("RUN", "chown", "-R", "docker:docker", "/docs"),
      Cmd("ENV", s"NODE_PATH $defaultDockerInstallationPath/node_modules"),
      Cmd("ENV", s"PATH $$PATH:$defaultDockerInstallationPath/node_modules/.bin"))
  case other => List(other)
}
