package codacy.stylelint

import java.nio.file.{Path, Paths}

import better.files._
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.plugins.api.{Options, Source}
import com.codacy.tools.scala.seed.utils.{CommandResult, CommandRunner}
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object Stylelint extends Tool {

  private lazy val configFileNames = Set(
    ".stylelintrc",
    ".stylelintrc.json",
    ".stylelintrc.yaml",
    ".stylelintrc.yml",
    ".stylelintrc.js",
    "stylelint.config.js")

  override def apply(
    source: Source.Directory,
    configuration: Option[List[Pattern.Definition]],
    files: Option[Set[Source.File]],
    options: Map[Options.Key, Options.Value])(implicit specification: Tool.Specification): Try[List[Result]] = {

    val configFilePath = getConfigFile(source, configuration)

    val commandResult = run(source, configFilePath, files)

    val parsedResults = parseJson(commandResult)

    convertToResult(parsedResults)
  }

  def checkForExistingConfigFile(source: Source.Directory): Option[Path] = {

    findConfigurationFile(Paths.get(source.path), configFileNames)

  }

  def findConfigurationFile(root: Path, configFileNames: Set[String], maxDepth: Int = 5): Option[Path] = {
    val allFiles = File(root).walk(maxDepth = maxDepth).toList

    val configFiles: List[Path] = configFileNames.flatMap { nativeConfigFileName =>
      allFiles.filter(_.name == nativeConfigFileName).map(_.path)
    }(collection.breakOut)

    configFiles.sortBy(_.toString.length).headOption
  }

  def getConfigFile(source: Source.Directory, configuration: Option[List[Pattern.Definition]]): Path = {
    configuration.map { config =>
      // Generate config file from pattern
      val patterns = config.map { pattern =>
        val parameter = pattern.parameters
          .flatMap(_.headOption.map { param =>
            val parameterValue: JsValue = param.value
            parameterValue
          })
          .getOrElse(JsNull)

        (pattern.patternId.value, parameter)
      }
      // save config file
      File
        .newTemporaryFile("codacy-stylelint", ".json")
        .write(Json.prettyPrint(Json.toJson(JsObject(Seq(("rules", JsObject(patterns)))))))
        .path
      // return config file path

    }.orElse {

      // find config file
      checkForExistingConfigFile(source)

    }.getOrElse {

      // save default file
      File
        .newTemporaryFile("codacy-stylelint", ".json")
        .write(Json.prettyPrint(Json.toJson(JsObject(Seq(("extends", JsString("stylelint-config-standard")))))))
        .path
      // return default file path
    }
  }

  def run(source: Source.Directory,
                   configFilePath: Path,
                   filesOpt: Option[Set[Source.File]]): Try[CommandResult] = {
    val fileArgument = filesOpt.map(files => files.map(_.path)).getOrElse(List("**/**.{css,scss,less,sass}"))

    val command = List("stylelint") ++ fileArgument ++ List("--config", configFilePath.toString) ++ List(
      "--formatter",
      "json") ++ List("--config-basedir", "/usr/local/lib/node_modules")

    CommandRunner.exec(command, Option(File(source.path).toJava)).fold(Failure(_), Success(_))
  }

  def parseJson(commandResult: Try[CommandResult]): Try[List[StylelintResult]] = {
    implicit val warningResultFmt: Format[StylelintPatternResult] = Json.format[StylelintPatternResult]
    implicit val resultFmt: Format[StylelintResult] = Json.format[StylelintResult]

    commandResult.flatMap { result =>
      val jsonString = result.stdout.mkString("\n")
      Try(Json.parse(jsonString).as[List[StylelintResult]]).recoverWith {
        case err =>
          Failure(new Exception(s"""|Could not parse results json: ${err.getMessage}
                  |
                  |Json:
                  |$jsonString
              """.stripMargin))
      }
    }
  }

  def convertToResult(parsedResults: Try[List[StylelintResult]]): Try[List[Result]] = {
    parsedResults match {
      case Success(results) =>
        val issues = results.flatMap { fileResultsData =>
          fileResultsData.warnings.map { resultData =>
            Result.Issue(
              Source.File(fileResultsData.source),
              Result.Message(resultData.text),
              Pattern.Id(resultData.rule),
              Source.Line(resultData.line))
          }
        }

        Success(issues)

      case Failure(err) => Failure(err)
    }
  }

}
