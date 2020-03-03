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
    val version = File(".stylelint-version").contentAsString.trim

    cloneFromGitToTmpDir(tmpDirectory, version)

    val rulesdir = tmpDirectory + "/lib/rules"
    val filePathForDocs = "src/main/resources/docs/"

    val patterns = getListOfSubDirectories(rulesdir)
    initializePatternsFile(patterns, version, filePathForDocs)
    initializeDescriptionFile(patterns, rulesdir, filePathForDocs)
    copyDescriptionFiles(patterns, rulesdir, filePathForDocs)
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

    val toolpatterns: Set[Pattern.Specification] = patterns.map { patternid =>
      addNewPattern(patternid, default.getOrElse(patternid, Parameter.Value(JsNull)))
    }(collection.breakOut)
    val tool = Tool.Specification(Tool.Name("stylelint"), Option(Tool.Version(version)), toolpatterns)
    File(filePathForDocs + "/patterns.json").write(Json.prettyPrint(Json.toJson(tool)))
  }

  def addNewPattern(patternName: String, default: Parameter.Value): Pattern.Specification = {
    val param = Option(Set(Parameter.Specification(Parameter.Name(patternName), default)))

    Pattern.Specification(Pattern.Id(patternName), Result.Level.Err, Pattern.Category.CodeStyle, None, param)
  }

  def PatternsFromDefaultConfig(): Map[String, Parameter.Value] = {
    val stylelintConfigStandard =
      Resource.getAsString("default_configs/stylelint-config-recommended-standard.json")(Charset.defaultCharset())
    Json.parse(stylelintConfigStandard).as[Map[String, Parameter.Value]]
  }

  def initializeDescriptionFile(patterns: List[String], rulesdir: String, filePathForDocs: String): File = {

    val patternsDescription: Set[Pattern.Description] = patterns.map { patternid =>
      val patternDescription =
        ParseMarkupRule.parseForDescriptions(File(rulesdir + "/" + patternid + "/README.md"))
      addNewDescription(patternid, patternDescription)
    }(collection.breakOut)
    File(filePathForDocs + "/description/description.json").write(Json.prettyPrint(Json.toJson(patternsDescription)))
  }

  def addNewDescription(patternName: String, patternDescription: String): Pattern.Description = {
    val param = Option(Set(Parameter.Description(Parameter.Name(patternName), Parameter.DescriptionText(patternName))))

    Pattern.Description(Pattern.Id(patternName), Pattern.Title(patternDescription), None, None, param)
  }

  def copyDescriptionFiles(folderNames: List[String], temporaryFileLocation: String, filePathForDocs: String): Unit = {
    val descriptionDir = File(filePathForDocs + "/description/")
    descriptionDir.createDirectories()
    folderNames.foreach { patternName =>
      File(s"$temporaryFileLocation/$patternName/README.md")
        .copyTo(File(s"$descriptionDir/$patternName.md"), overwrite = true)
    }
  }
}
