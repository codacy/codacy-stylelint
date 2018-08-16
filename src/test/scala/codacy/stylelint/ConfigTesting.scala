package codacy.stylelint

import java.nio.charset.Charset

import better.files._
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results._
import org.specs2.mutable.Specification
import play.api.libs.json._

class ConfigTesting extends Specification {

  "This is a specification for the functioning of the generation/read of the configuration file for the stylelint tool. \n As such ".txt

  "Stylelint" should {
    "utilize the users configuration file when one is provided" in {
      val (workspace, source) = createTemporaryWorkspace()

      val expectedResult =
        fileIssues(s"${workspace.toJava.getCanonicalPath}/test.css", 300)

      createConfigWithIndentationRule(300, workspace)

      val result = Stylelint.apply(source, None, None, Map())(
        Tool.Specification(Tool.Name("codacy-stylelint"), Option.empty[Tool.Version], Set()))

      result should beSuccessfulTry[List[Result]](expectedResult)
    }

    "generate a configuration file with the rules provided" in {
      val (workspace, source) = createTemporaryWorkspace()
      val expectedResult =
        fileIssues(s"${workspace.toJava.getCanonicalPath}/test.css", 100)

      val configuration = createIdentationPattern(100)

      val result = Stylelint.apply(source, configuration, None, Map())(
        Tool.Specification(Tool.Name("codacy-stylelint"), Option.empty[Tool.Version], Set()))

      result should beSuccessfulTry[List[Result]](expectedResult)
    }

    "generate a configuration file with the rules provided even if a configuration file is provided" in {
      val (workspace, source) = createTemporaryWorkspace()
      val expectedResult =
        fileIssues(s"${workspace.toJava.getCanonicalPath}/test.css", 100)

      val configuration = createIdentationPattern(100)
      createConfigWithIndentationRule(300, workspace)

      val result = Stylelint.apply(source, configuration, None, Map())(
        Tool.Specification(Tool.Name("codacy-stylelint"), Option.empty[Tool.Version], Set()))
      result should beSuccessfulTry[List[Result]](expectedResult)
    }

    "default to the standard configuration file when neither a configuration file nor a set of rules are provided" in {
      val (_, source) = createTemporaryWorkspace()
      val expectedResult = List()
      val result = Stylelint.apply(source, None, None, Map())(
        Tool.Specification(Tool.Name("codacy-stylelint"), Option.empty[Tool.Version], Set()))

      result should beSuccessfulTry[List[Result]](expectedResult)
    }

    "select all files in the source directory and below when no files are given" in {
      val (workspace, source) = createTemporaryWorkspaceWithMultipleFiles()

      val testCssFilePath = s"${workspace.toJava.getCanonicalPath}/test.css"
      val testCssFile2Path = s"${workspace.toJava.getCanonicalPath}/test2.css"
      val testCssFile3Path = s"${workspace.toJava.getCanonicalPath}/depth1/test3.css"
      val testCssFile4Path = s"${workspace.toJava.getCanonicalPath}/depth1/depth2/depth3/test4.css"
      val ident = 100

      val expectedResult =
        fileIssues(testCssFilePath, ident) ++ fileIssues(testCssFile2Path, ident) ++ fileIssues(testCssFile3Path, ident) ++ fileIssues(
          testCssFile4Path,
          ident)

      val configuration = createIdentationPattern(100)

      val result = Stylelint.apply(source, configuration, None, Map())(
        Tool.Specification(Tool.Name("codacy-stylelint"), Option.empty[Tool.Version], Set()))

      result should beSuccessfulTry[List[Result]](expectedResult)
    }

    "select only the given files in the source directory and below" in {
      val (workspace, source) = createTemporaryWorkspaceWithMultipleFiles()

      val testCssFilePath = s"${workspace.toJava.getCanonicalPath}/test.css"
      val testCssFile4Path = s"${workspace.toJava.getCanonicalPath}/depth1/depth2/depth3/test4.css"
      val ident = 100

      val expectedResult =
        fileIssues(testCssFilePath, ident) ++ fileIssues(testCssFile4Path, ident)

      val configuration = createIdentationPattern(ident)

      val result = Stylelint.apply(
        source,
        configuration,
        Option(
          Set(
            Source.File(s"${workspace.toJava.getCanonicalPath}/test.css"),
            Source.File(s"${workspace.toJava.getCanonicalPath}/depth1/depth2/depth3/test4.css"))),
        Map())(Tool.Specification(Tool.Name("codacy-stylelint"), Option.empty[Tool.Version], Set()))

      result should beSuccessfulTry[List[Result]](expectedResult)
    }
  }

  private def createConfigWithIndentationRule(ident: Int, dir: File): File = {
    val rcText = JsObject(
      Seq(
        ("extends", JsString("stylelint-config-standard")),
        ("rules", JsObject(Seq(("indentation", JsNumber(ident)))))))

    File(s"${dir.pathAsString}/.stylelintrc").write(Json.prettyPrint(rcText))
  }

  private def createIdentationPattern(ident: Int): Option[List[Pattern.Definition]] = {
    val pattern = "indentation"
    val parameterdef = Option(Set(Parameter.Definition(Parameter.Name(pattern), Parameter.Value(ident.toString))))
    Option(List(Pattern.Definition(Pattern.Id(pattern), parameterdef)))
  }

  private def createTemporaryWorkspace(): (File, Source.Directory) = {
    val dir = File.newTemporaryDirectory()
    val testFilePath = "src/test/resources/codacy/stylelint"
    val originalTestSourceFile = File(s"$testFilePath/test.css")

    originalTestSourceFile.copyToDirectory(dir)

    val source = Source.Directory(dir.pathAsString)

    (dir, source)
  }

  private def createTemporaryWorkspaceWithMultipleFiles(): (File, Source.Directory) = {
    //creates 2 files on the workspace (test.css and test2.css) then creates folders up to depth 3 with file test3.css at
    // depth 1 and test4.css at depth 3

    def writeToFile(file: File, contents: String) = {
      file.parent.createDirectories()
      file.write(contents)
    }
    val dir = File.newTemporaryDirectory()

    val testFile1 = dir / "test.css"
    val testFile2 = dir / "test2.css"
    val testFile3 = dir / "depth1" / "test3.css"
    val testFile4 = dir / "depth1" / "depth2" / "depth3" / "test4.css"

    val testFileContents = Resource.getAsString("codacy/stylelint/test.css")(Charset.defaultCharset())

    writeToFile(testFile1, testFileContents)
    writeToFile(testFile2, testFileContents)
    writeToFile(testFile3, testFileContents)
    writeToFile(testFile4, testFileContents)

    val source = Source.Directory(dir.pathAsString)

    (dir, source)
  }

  private def indentationIssue(file: String, line: Int, ident: Int) = {
    Result.Issue(
      Source.File(file),
      Result.Message(s"Expected indentation of $ident spaces (indentation)"),
      Pattern.Id("indentation"),
      Source.Line(line))
  }

  private def fileIssues(file: String, ident: Int) = {
    List.range(6, 10).map(indentationIssue(file, _, ident))
  }

}
