package codacy.stylelint

import java.nio.charset.Charset

import scala.sys.process.Process
import better.files.{File, Resource}
import com.codacy.plugins.api.results.{Parameter, Pattern, Result, Tool}
import com.codacy.plugins.api._
import play.api.libs.json.{JsNull, Json}

object DocGenerator {
  def main(args: Array[String]): Unit = {
    //tmpDirectory <- File.temporaryDirectory()
    val tmpDirectory = File("/tmp/clone")
    val version = "9.3.0" //for first test

    //cloneFromGitToTmpDir(tmpDirectory,version)

    val rulesdir = tmpDirectory + "/lib/rules"

    val filePathForDocs = "src/main/resources/docs/"
    //val patternsfile = File(filePathForDocs+"/patterns.json")
    //val descriptionfile = File(filePathForDocs+"/description.json")

    val (folders, patterns) = getPatterns(rulesdir)
    initializePatternsFile(patterns , version,filePathForDocs)
    initializeDescriptionFile(folders, patterns ,rulesdir,filePathForDocs)
    copyDescriptionFiles(folders,rulesdir,filePathForDocs)

  }

  def cloneFromGitToTmpDir(tmpDirectory: better.files.File, version: String): Unit = {
    Process(
      Seq("git",
        "clone",
        "git://github.com/stylelint/stylelint.git",
        tmpDirectory.pathAsString)).!
    Process(Seq("git", "reset", "--hard", version), tmpDirectory.toJava).!
  }

  def getListOfSubDirectories(directoryName: String): List[String] = {
    val directory = File(directoryName)
    directory
      .walk(maxDepth = 1)
      .filter(_.isDirectory)
      .filterNot(_.isSamePathAs(directory))
      .map(_.name)
      .toList
  }

  def getListOfFiles(directoryName: String,
                     extensions: List[String]): List[String] = {
    val directory = File(directoryName)
    directory
      .walk(maxDepth = 1)
      .filter(_.isRegularFile)
      .filterNot(_.nameWithoutExtension == "index")
      .toList
      .filter(file => extensions.exists(file.name.endsWith(_)))
      .map(_.nameWithoutExtension)
  }

  def initializePatternsFile(patterns: List[String], version: String, filePathForDocs: String): Unit = {

    val default = PatternsFromDefaultConfig()

    val toolpatterns: Set[Pattern.Specification] = patterns.map {
      patternid => addNewPattern(patternid,default.getOrElse(patternid, Parameter.Value(JsNull)))
    }(collection.breakOut)
    val tool = Tool.Specification(Tool.Name("stylelint"), Option(Tool.Version(version)), toolpatterns)
    File(filePathForDocs+"/patterns.json").write(Json.prettyPrint(Json.toJson(tool)))


  }

  def addNewPattern(patternName: String, default: Parameter.Value): Pattern.Specification = {

    val param = Option(Set(Parameter.Specification(Parameter.Name(patternName), default)))

    Pattern.Specification(Pattern.Id(patternName), Result.Level.Err, Pattern.Category.CodeStyle, param)
  }

  def PatternsFromDefaultConfig(): Map[String, Parameter.Value]={
    val stylelintConfigStandard = Resource.getAsString("default_configs/stylelint-config-recommended-standard.json")(Charset.defaultCharset())
    Json.parse(stylelintConfigStandard).as[Map[String, Parameter.Value]]
  }

  def getPatterns (rulesdir: String): (List[String],List[String])={
    //get non-documented rules
    val folders = getListOfSubDirectories(rulesdir)
    //folders.foreach(println)

    //get non-documented rules(not enclosed in folders)
    val okFileExtensions = List("js")
    val patterns = getListOfFiles(rulesdir, okFileExtensions) ++ folders

    (folders, patterns)
  }

  def initializeDescriptionFile( folders: List[String],patterns: List[String], rulesdir: String, filePathForDocs: String): Unit = {

    val patternsDescription: Set[Pattern.Description] = patterns.map {
      patternid =>
        val patternDescription = if(folders contains patternid) {
          ParseMarkupRule.parseForDescriptions(File(rulesdir + "/" + patternid + "/README.md"))
        } else {
          patternid + ": further description not available"
        }
        addNewDescription(patternid, patternDescription) //need to remove non folder
    }(collection.breakOut)
    File(filePathForDocs+"/description.json").write(Json.prettyPrint(Json.toJson(patternsDescription)))
  }

  def addNewDescription(patternName: String, patternDescription: String): Pattern.Description = {

    val param = Option(Set(Parameter.Description(Parameter.Name(patternName),Parameter.DescriptionText(patternName))))

    Pattern.Description(Pattern.Id(patternName), Pattern.Title(patternDescription), None, None , param)
  }

  def copyDescriptionFiles(folderNames: List[String], temporaryFileLocation: String, filePathForDocs: String): Unit = {
    folderNames.map{
      patternName =>
        File(temporaryFileLocation + "/" + patternName + "/README.md")
          .copyTo(File(filePathForDocs + "/description/" + patternName + ".md"), overwrite = true)
    }
  }
}
