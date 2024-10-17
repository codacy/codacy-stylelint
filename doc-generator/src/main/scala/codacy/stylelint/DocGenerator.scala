package codacy.stylelint

import java.nio.charset.Charset
import better.files.{File, Resource}
import com.codacy.plugins.api._
import com.codacy.plugins.api.results.{Parameter, Pattern, Result, Tool}
import play.api.libs.json.{JsNull, Json}

import scala.sys.process.Process

object DocGenerator {

  def main(args: Array[String]): Unit = {

    val pluginsList = processPlugins()

    initializePatternsFile(pluginsList)
    initializeDescriptionFile(pluginsList)
    copyDescriptionFiles(pluginsList)
  }

  def processPlugins(): List[Plugin] = {

    val pluginsList = listOfPlugins()
    var tempList: List[Plugin] = List()

    pluginsList.map { plugin =>
      val version = File("package.json").inputStream() { file =>
        Json.parse(file)("dependencies")(plugin.pluginName).as[String].stripPrefix("^")
      }
      val tmpDirectory = File.newTemporaryDirectory()
      cloneFromGitToTmpDir(tmpDirectory, version, plugin.url)
      val rulesdir = tmpDirectory + "/" + plugin.relativeRulesDir
      val patterns = getListOfSubDirectories(rulesdir)
      val tempPlugin =
        Plugin(plugin.pluginName, plugin.relativeRulesDir, plugin.url, plugin.tree, patterns, version, tmpDirectory)

      tempList = tempList :+ tempPlugin
    }
    (tempList)
  }

  case class Plugin(pluginName: String,
                    relativeRulesDir: String,
                    url: String,
                    tree: String,
                    patterns: List[String],
                    version: String,
                    tempDirectory: better.files.File)

  def listOfPlugins(): List[Plugin] = {
    List(
      Plugin(
        "stylelint",
        "lib/rules",
        "https://github.com/stylelint/stylelint.git",
        "https://github.com/stylelint/stylelint",
        null,
        null,
        null),
      Plugin(
        "stylelint-a11y",
        "src/rules",
        "https://github.com/YozhikM/stylelint-a11y.git",
        "https://github.com/YozhikM/stylelint-a11y",
        null,
        null,
        null))
  }

  def cloneFromGitToTmpDir(tmpDirectory: better.files.File, version: String, url: String): Int = {
    Process(Seq("git", "clone", url, tmpDirectory.pathAsString)).!
    Process(Seq("git", "reset", "--hard", version), tmpDirectory.toJava).!
  }

  def getListOfSubDirectories(directoryName: String): List[String] = {
    val directory = File(directoryName)
    directory
      .walk(maxDepth = 1)
      .filter(_.isDirectory)
      .filterNot(_.isSamePathAs(directory))
      .filterNot(_.name.contains("__tests__"))
      .map(_.name)
      .toList
  }

  def initializePatternsFile(plugins: List[Plugin]): File = {
    val default = PatternsFromDefaultConfig()

    val version = File("package.json").inputStream() { file =>
      Json.parse(file)("dependencies")("stylelint").as[String].stripPrefix("^")
    }

    val patterns: List[String] = plugins.flatMap(_.patterns)

    val toolpatterns: Set[Pattern.Specification] = patterns.view.map { patternid =>
      addNewPattern(patternid, default.getOrElse(patternid, Parameter.Value(JsNull)))
    }.to(Set)
    val tool = Tool.Specification(Tool.Name("stylelint"), Option(Tool.Version(version)), toolpatterns)
    File("docs/patterns.json").write(Json.prettyPrint(Json.toJson(tool)))
  }

  def addNewPattern(patternName: String, default: Parameter.Value): Pattern.Specification = {
    val param = Set(Parameter.Specification(Parameter.Name(patternName), default))
    val enabled = CodacyValues.patternsEnabled.contains(patternName)
    val level = if (CodacyValues.possibleErrorsPatterns.contains(patternName)) Result.Level.Warn else Result.Level.Info
    Pattern
      .Specification(Pattern.Id(patternName), level, Pattern.Category.CodeStyle, None, None, param, enabled = enabled)
  }

  def PatternsFromDefaultConfig(): Map[String, Parameter.Value] = {
    val stylelintConfigStandard =
      Resource.getAsString("default_configs/stylelint-config-recommended-standard.json")(Charset.defaultCharset())
    Json.parse(stylelintConfigStandard).as[Map[String, Parameter.Value]]
  }

  def initializeDescriptionFile(plugins: List[Plugin]): File = {

    var finalPatternsDescription: Set[Pattern.Description] = Set()
    plugins.map { plugin =>
      val patternsDescription: Set[Pattern.Description] = plugin.patterns.map { pattern =>
        val patternDescription =
          ParseMarkupRule.parseForDescriptions(File(plugin.tempDirectory + "/" + plugin.relativeRulesDir + "/" + pattern + "/README.md"))

        // looking for markdown links, e.g., [text](https://www.example.com)
        val urlRegex = """\[(.+?)\]\((.+?)\)""".r
        val descriptionWithoutUrl = urlRegex.replaceAllIn(patternDescription, m => m.group(1)).trim
        addNewDescription(pattern, descriptionWithoutUrl)
      }.to(Set)

      finalPatternsDescription ++= patternsDescription
    }
    File("docs/description/description.json").write(Json.prettyPrint(Json.toJson(finalPatternsDescription)))
  }

  def addNewDescription(patternName: String, patternDescription: String): Pattern.Description = {
    val param = Set(Parameter.Description(Parameter.Name(patternName), Parameter.DescriptionText(patternName)))

    Pattern.Description(Pattern.Id(patternName), Pattern.Title(patternDescription), None, None, param)
  }

  def copyDescriptionFiles(plugins: List[Plugin]): Unit = {
    val descriptionDir = File("docs/description/")
    descriptionDir.createDirectories()
    plugins.map { plugin =>
      plugin.patterns.foreach { patternName =>
        val documentationFile = File(s"${plugin.tempDirectory}/${plugin.relativeRulesDir}/$patternName/README.md")
        val fileContent = documentationFile.contentAsString

        // looking for markdown link to local resources, e.g, [`fix` option](../../../docs/user-guide/usage/options.md#fix)
        // assuming local URLs start with "../" this is the pattern used at the time of this solution
        val localUrlRegex = """\[(.+?)\]\((\.\./.+?)\)""".r

        val contentWithReplacedUrls = localUrlRegex.replaceAllIn(fileContent, m => {
          val linkText = m.group(1)
          val localUrl = m.group(2)
          val absoluteFilePath = documentationFile.parent / localUrl
          val relativePath = plugin.tempDirectory.relativize(absoluteFilePath).toString
          s"[$linkText](${plugin.tree}/${plugin.version}/$relativePath)"
        })

        File(s"$descriptionDir/$patternName.md").write(contentWithReplacedUrls)
      }
    }

  }
}
