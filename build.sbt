import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

import scala.io.Source
import scala.util.parsing.json.JSON

name := "codacy-stylelint"

version := "1.0.0-SNAPSHOT"

val languageVersion = "2.12.4"

scalaVersion := languageVersion

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases",
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.codacy" %% "codacy-engine-scala-seed" % "3.0.183",
  "com.vladsch.flexmark" % "flexmark-all" % "0.34.8",
  "org.specs2" %% "specs2-core" % "4.2.0" % Test
)
scalacOptions in Test  ++= Seq("-Yrangepos")
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

  patternsJsonValues
    .collectFirst {
      case ("version", JsString(version)) => version
    }
    .getOrElse(throw new Exception("Failed to retrieve version from docs/patterns.json"))
}

mappings.in(Universal) ++= resourceDirectory
  .in(Compile)
  .map { resourceDir: File =>
    val src = resourceDir / "docs"
    val dest = "/docs"

    (for {
      path <- better.files.File(src.toPath).listRecursively()
      if !path.isDirectory
    } yield path.toJava -> path.toString.replaceFirst(src.toString, dest)).toSeq
  }
  .value

val dockerUser = "docker"
val dockerGroup = "docker"

daemonUser in Docker := dockerUser

daemonGroup in Docker := dockerGroup

dockerBaseImage := "openjdk:8-jre-alpine"

def installAll(toolVersion: String) =
  s"""apk update &&
     |apk add bash curl nodejs-npm &&
     |npm install -g npm@5 &&
     |npm install -g stylelint@$toolVersion &&
     |npm install -g stylelint-config-standard@18.2.0 &&
     |npm install -g stylelint-config-recommended@2.1.0 &&
     |npm install -g stylelint-order@0.8.1 &&
     |npm install -g stylelint-suitcss@3.0.0 &&
     |npm install -g stylelint-config-suitcss@14.0.0 &&
     |npm install -g stylelint-scss@3.2.0 &&
     |npm install -g stylelint-config-wordpress@13.0.0 &&
     |npm install -g stylelint-csstree-validator@1.3.0 &&
     |npm install -g stylelint-declaration-strict-value@1.0.4 &&
     |npm install -g stylelint-declaration-use-variable@1.7.0 &&
     |npm install -g stylelint-rscss@0.4.0 &&
     |npm install -g stylelint-selector-bem-pattern@2.0.0 &&
     |npm install -g stylelint-config-slds@1.0.7 &&
     |npm install -g stylelint-config-prettier@4.0.0 &&
     |rm -rf /tmp/* &&
     |rm -rf /var/cache/apk/*""".stripMargin
    .replaceAll(System.lineSeparator(), " ")

dockerCommands := {
  dockerCommands.dependsOn(toolVersion).value.flatMap {
    case cmd @ Cmd("ADD", _) =>
      List(Cmd("RUN", "adduser -u 2004 -D docker"),
        cmd,
        Cmd("RUN", installAll(toolVersion.value)),
        Cmd("RUN", "mv /opt/docker/docs /docs"),
        ExecCmd("RUN", Seq("chown", "-R", s"$dockerUser:$dockerGroup", "/docs"): _*),
        Cmd("ENV", "NODE_PATH /usr/lib/node_modules"))
    case other => List(other)
  }
}