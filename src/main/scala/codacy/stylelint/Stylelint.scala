package codacy.stylelint

import java.nio.file.{Path, Paths}

import better.files._
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.plugins.api.{Options, Source}
import com.codacy.tools.scala.seed.utils.{CommandResult, CommandRunner}
import com.codacy.tools.scala.seed.utils.FileHelper._
import com.codacy.tools.scala.seed.utils.ToolHelper._
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

  //from https://stylelint.io/user-guide/usage/cli/#exit-codes
  private object ExitCodes {
    val NO_ISSUES = 0
    val EXECUTION_ERROR = 1
    val DETECTED_ISSUES = 2
    val CONFIGURATION_ERROR = 78
  }

  override def apply(
    source: Source.Directory,
    configuration: Option[List[Pattern.Definition]],
    files: Option[Set[Source.File]],
    options: Map[Options.Key, Options.Value])(implicit specification: Tool.Specification): Try[List[Result]] = {

    files match {
      case Some(set) if set.isEmpty =>
        Success(List.empty)
      case _ =>
        val configFilePath = getConfigFile(source, configuration.withDefaultParameters)
        val commandResult = run(source, configFilePath, files)
        commandResult match {
          case Failure(err) =>
            Failure(new Exception(s"Could not run stylelint: ${err.getCause}"))
          case _ =>
            val filesArgument = files.fold(List("All"))(_.map(_.path).toList)
            val parsedResults = parseCommandResult(commandResult, filesArgument)

            convertToResult(parsedResults)
        }
    }
  }

  def checkForExistingConfigFile(source: Source.Directory): Option[Path] = {
    findConfigurationFile(Paths.get(source.path), configFileNames)
  }

  def getConfigFile(source: Source.Directory, configuration: Option[List[Pattern.Definition]]): Path = {
    configuration.map { config =>
      val patterns = config.map { pattern =>
        val parameter = pattern.parameters.headOption.map { param =>
          val parameterValue: JsValue = param.value
          parameterValue
        }.getOrElse(JsNull)

        (pattern.patternId.value, parameter)
      }
      File
        .newTemporaryFile("codacy-stylelint", ".json")
        .write(Json.prettyPrint(Json.toJson(JsObject(Seq(("rules", JsObject(patterns)))))))
        .path
    }.orElse {
      checkForExistingConfigFile(source)
    }.getOrElse {
      File
        .newTemporaryFile("codacy-stylelint", ".json")
        .write(Json.prettyPrint(Json.toJson(JsObject(Seq(("extends", JsString("stylelint-config-standard")))))))
        .path
    }
  }

  def run(source: Source.Directory, configFilePath: Path, filesOpt: Option[Set[Source.File]]): Try[CommandResult] = {
    val nodeModulesDir = "/workdir/node_modules"
    val executableFile = List(s"$nodeModulesDir/stylelint/bin/stylelint.mjs")
    val fileArgument = filesOpt
      .map(files => files.map(_.path))
      .getOrElse(List("**/**.{css,scss,less,sass}", "--custom-syntax", "postcss-syntax"))

    val configurationBaseDir = List("--config-basedir", nodeModulesDir)
    val configurationFile = List("--config", configFilePath.toString)
    val formatter = List("--formatter", "json")
    val options = List("--allow-empty-input")

    val command = executableFile ++ fileArgument ++ configurationFile ++ configurationBaseDir ++ formatter ++ options

    CommandRunner.exec(command, Option(File(source.path).toJava)).fold(Failure(_), Success(_))
  }

  implicit val warningResultFmt: Format[StylelintPatternResult] = Json.format[StylelintPatternResult]
  implicit val resultFmt: Format[StylelintResult] = Json.format[StylelintResult]

  def parseCommandResult(commandResult: Try[CommandResult], targetFiles: List[String]): Try[List[StylelintResult]] = {
    commandResult.flatMap {
      case CommandResult(ExitCodes.NO_ISSUES | ExitCodes.DETECTED_ISSUES, _, results) =>
        parseJson(results)
      case CommandResult(exitCode, stdOut, stdErr) =>
        val toolErrorMessage =
          s"""Stylelint exited with code ${printExitCode(exitCode)}
             |  - targeting files: $targetFiles
             |  - stderr: $stdErr
             |  - stdout: $stdOut
             |""".stripMargin
        scala.util.Failure(new Exception(toolErrorMessage))
    }
  }

  def printExitCode(value: Int) = value match {
    case ExitCodes.EXECUTION_ERROR =>
      s"$value - something unknown went wrong when executing the tool"
    case ExitCodes.CONFIGURATION_ERROR =>
      s"$value - there was some problem with the configuration file"
    case _ =>
      s"$value - unknown error"
  }

  def parseJson(jsonLines: List[String]): Try[List[StylelintResult]] = {
    val jsonString = jsonLines.mkString("\n")
    Try(Json.parse(jsonString).as[List[StylelintResult]]).recoverWith {
      case err =>
        val errorString =
          s"""Could not parse results json:
            |$jsonString
            |
            |Exception: ${err.getMessage}
              """.stripMargin
        Failure(new Exception(errorString))
    }
  }

  def convertToResult(parsedResults: Try[List[StylelintResult]]): Try[List[Result]] = {
    parsedResults.map { results =>
      results.flatMap { fileResultsData =>
        fileResultsData.warnings.map { resultData =>
          Result.Issue(
            Source.File(fileResultsData.source),
            Result.Message(resultData.text),
            Pattern.Id(resultData.rule),
            Source.Line(resultData.line))
        }
      }
    }
  }

}
