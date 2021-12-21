package codacy.stylelint

import java.nio.charset.Charset

import better.files.{File, Resource}
import com.codacy.plugins.api._
import com.codacy.plugins.api.results.{Parameter, Pattern, Result, Tool}
import play.api.libs.json.{JsNull, Json}

import scala.sys.process.Process

object DocGenerator {

  def main(args: Array[String]): Unit = {
    val tmpDirectory = File.newTemporaryDirectory()

    val version = File("package.json").inputStream() { file =>
      Json.parse(file)("dependencies")("stylelint").as[String].stripPrefix("^")
    }

    cloneFromGitToTmpDir(tmpDirectory, version)

    val rulesdir = tmpDirectory + "/lib/rules"
    val filePathForDocs = "docs/"

    val patterns = getListOfSubDirectories(rulesdir)
    initializePatternsFile(patterns, version, filePathForDocs)
    initializeDescriptionFile(patterns, rulesdir, filePathForDocs)
    copyDescriptionFiles(patterns, rulesdir, tmpDirectory, filePathForDocs, version)
  }

  def cloneFromGitToTmpDir(tmpDirectory: better.files.File, version: String): Int = {
    Process(Seq("git", "clone", "git://github.com/stylelint/stylelint.git", tmpDirectory.pathAsString)).!
    Process(Seq("git", "reset", "--hard", version), tmpDirectory.toJava).!
  }

  def getListOfSubDirectories(directoryName: String): List[String] = {
    val directory = File(directoryName)
    directory.walk(maxDepth = 1).filter(_.isDirectory).filterNot(_.isSamePathAs(directory)).map(_.name).toList
  }

  def initializePatternsFile(patterns: List[String], version: String, filePathForDocs: String): File = {
    val default = PatternsFromDefaultConfig()

    val toolpatterns: Set[Pattern.Specification] = patterns.view.map { patternid =>
      addNewPattern(patternid, default.getOrElse(patternid, Parameter.Value(JsNull)))
    }.to(Set)
    val tool = Tool.Specification(Tool.Name("stylelint"), Option(Tool.Version(version)), toolpatterns)
    File(filePathForDocs + "/patterns.json").write(Json.prettyPrint(Json.toJson(tool)))
  }

  def addNewPattern(patternName: String, default: Parameter.Value): Pattern.Specification = {
    val param = Set(Parameter.Specification(Parameter.Name(patternName), default))
    val enabled = CodacyValues.patternsEnabled.contains(patternName)
    val level = if (CodacyValues.possibleErrorsPatterns.contains(patternName)) Result.Level.Warn else Result.Level.Info
    Pattern.Specification(Pattern.Id(patternName), level, Pattern.Category.CodeStyle, None, param, enabled = enabled)
  }

  def PatternsFromDefaultConfig(): Map[String, Parameter.Value] = {
    val stylelintConfigStandard =
      Resource.getAsString("default_configs/stylelint-config-recommended-standard.json")(Charset.defaultCharset())
    Json.parse(stylelintConfigStandard).as[Map[String, Parameter.Value]]
  }

  def initializeDescriptionFile(patterns: List[String], rulesdir: String, filePathForDocs: String): File = {

    val patternsDescription: Set[Pattern.Description] = patterns.view.map { patternid =>
      val patternDescription =
        ParseMarkupRule.parseForDescriptions(File(rulesdir + "/" + patternid + "/README.md"))

      // looking for markdown links, e.g., [text](https://www.example.com)
      val urlRegex = """\[(.+?)\]\((.+?)\)""".r
      val descriptionWithoutUrl = urlRegex.replaceAllIn(patternDescription, m => m.group(1)).trim
      addNewDescription(patternid, descriptionWithoutUrl)
    }.to(Set)
    File(filePathForDocs + "/description/description.json").write(Json.prettyPrint(Json.toJson(patternsDescription)))
  }

  def addNewDescription(patternName: String, patternDescription: String): Pattern.Description = {
    val param = Set(Parameter.Description(Parameter.Name(patternName), Parameter.DescriptionText(patternName)))

    Pattern.Description(Pattern.Id(patternName), Pattern.Title(patternDescription), None, None, param)
  }

  def copyDescriptionFiles(folderNames: List[String],
                           rulesDirectory: String,
                           mainDirectory: File,
                           docsDirectory: String,
                           version: String): Unit = {
    val descriptionDir = File(docsDirectory + "/description/")
    descriptionDir.createDirectories()
    folderNames.foreach { patternName =>
      val documentationFile = File(s"$rulesDirectory/$patternName/README.md")
      val fileContent = documentationFile.contentAsString

      // looking for markdown link to local resources, e.g, [`fix` option](../../../docs/user-guide/usage/options.md#fix)
      // assuming local URLs start with "../" this is the pattern used at the time of this solution
      val localUrlRegex = """\[(.+?)\]\((\.\./.+?)\)""".r

      val contentWithReplacedUrls = localUrlRegex.replaceAllIn(fileContent, m => {
        val linkText = m.group(1)
        val localUrl = m.group(2)
        val absoluteFilePath = documentationFile.parent / localUrl
        val relativePath = mainDirectory.relativize(absoluteFilePath).toString
        s"[$linkText](https://github.com/stylelint/stylelint/tree/$version/$relativePath)"
      })

      File(s"$descriptionDir/$patternName.md").write(contentWithReplacedUrls)
    }
  }
}
