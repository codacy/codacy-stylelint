// creates a patterns.json File with all rules

import java.nio.charset.Charset

import scala.sys.process.Process
import better.files.{File, Resource}
import com.codacy.plugins.api.results.{Parameter, Pattern, Result, Tool}
import com.codacy.plugins.api._
import play.api.libs.json.{JsNull, JsValue, Json}
import com.codacy.stylelint.documentation.parser.ParseMarkupRule

import scala.collection.immutable

object DocGenerator {
  def main(args: Array[String]): Unit = {
    //tmpDirectory <- File.temporaryDirectory()
    val tmpDirectory = File("/tmp/clone")
    val version = "9.3.0" //for first test

    //cloneFromGitToTmpDir(tmpDirectory,version)

    val dir = tmpDirectory + "/lib/rules"

    initializePatternsFile(dir, version)

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
      .filterNot(_.name != "index")
      .toList
      .filter(file => extensions.exists(file.name.endsWith(_)))
      .map(_.nameWithoutExtension)
  }

  def initializePatternsFile(rulesdir: String, version: String): Unit = {
    //val file = File("main/resources/docs/patterns.json")

    //get non-documented rules
    val folders = getListOfSubDirectories(rulesdir)
    folders.foreach(println)

    //get non-documented rules(not enclosed in folders)
    val okFileExtensions = List("js")
    val files = getListOfFiles(rulesdir, okFileExtensions)
    files.foreach(println)

    val default = getPatternsFromDefaultConfig()

    //TODO patterns from non directories not done
    val toolpatterns: Set[Pattern.Specification] = folders.map {
      patternid => addNewPattern(patternid,default.getOrElse(patternid, Parameter.Value(JsNull)))
    }(collection.breakOut)
    val tool = Tool.Specification(Tool.Name("stylelint"), Option(Tool.Version(version)), toolpatterns)
    println(Json.prettyPrint(Json.toJson(tool)))


  }

  def addNewPattern(patternName: String, default: Parameter.Value): Pattern.Specification = {

    val param = Option(Set(Parameter.Specification(Parameter.Name(patternName), default)))

    Pattern.Specification(Pattern.Id(patternName), Result.Level.Err, Pattern.Category.CodeStyle, param)
  }

  def getPatternsFromDefaultConfig(): Map[String, Parameter.Value]={
    var stylelintConfigStandard = Resource.getAsString("default_configs/stylelint-config-recommended-standard.json")(Charset.defaultCharset())
    Json.parse(stylelintConfigStandard).as[Map[String, Parameter.Value]]
  }
}
