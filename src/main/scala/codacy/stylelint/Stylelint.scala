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
          case Success(result) if result.stderr.nonEmpty =>
            Failure(new Exception(s"Stylelint failed with: ${result.stderr.headOption.getOrElse("")}"))
          case _ =>
            val parsedResults = parseJson(commandResult)

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
    val fileArgument = filesOpt.map(files => files.map(_.path)).getOrElse(List("**/**.{css,scss,less,sass}"))
    val basedir = sys.env.getOrElse("STYLELINT_CONFIG_BASEDIR", "/usr/local/lib/node_modules")
    val configurationBaseDirectory = List("--config-basedir", basedir)
    val configurationFile = List("--config", configFilePath.toString)
    val configuration = configurationFile ++ configurationBaseDirectory
    val formatter = List("--formatter", "json")

    val command = List("stylelint") ++ fileArgument ++ configuration ++ formatter

    CommandRunner.exec(command, Option(File(source.path).toJava)).fold(Failure(_), Success(_))
  }

  implicit val warningResultFmt: Format[StylelintPatternResult] = Json.format[StylelintPatternResult]
  implicit val resultFmt: Format[StylelintResult] = Json.format[StylelintResult]

  def parseJson(commandResult: Try[CommandResult]): Try[List[StylelintResult]] = {
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
