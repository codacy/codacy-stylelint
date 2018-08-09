package codacy.stylelint

import better.files._
import com.codacy.plugins.api.Source
import com.codacy.plugins.api.results.{Parameter, Pattern, Result}
import org.specs2.mutable.Specification

import scala.util.Try

class ConfigTesting extends Specification {

  "This is a specification for the functioning of the generation/read of the configuration file for the stylelint tool. \n As such ".txt

  "Stylelint" should {
    "utilize the users configuration file when one is provided" in {
      val workspace = createTemporaryWorkspace()
      val source = Source.Directory(workspace.pathAsString)
      val expectedResult = Try(
        List(
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 300 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 300 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 300 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 300 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9))))
      createConfigWithIndentationRule(300, workspace)

      val result = Stylelint.apply(source, None, None, Map())(null)

      result should beEqualTo(expectedResult)
    }

    "generate a configuration file with the rules provided" in {
      val workspace = createTemporaryWorkspace()
      val source = Source.Directory(workspace.pathAsString)
      val expectedResult = Try(
        List(
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9))))

      val configuration = createGivenPatternsWithIdentation(100)

      val result = Stylelint.apply(source, configuration, None, Map())(null)

      result should beEqualTo(expectedResult)
    }

    "generate a configuration file with the rules provided even if a configuration file is provided" in {
      val workspace = createTemporaryWorkspace()
      val source = Source.Directory(workspace.pathAsString)
      val expectedResult = Try(
        List(
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9))))
      val configuration = createGivenPatternsWithIdentation(100)
      createConfigWithIndentationRule(300, workspace)

      val result = Stylelint.apply(source, configuration, None, Map())(null)
      result should beEqualTo(expectedResult)
    }

    "default to the standard configuration file when neither a configuration file nor a set of rules are provided" in {
      val workspace = createTemporaryWorkspace()
      val source = Source.Directory(workspace.pathAsString)
      val expectedResult = Try(List())
      val result = Stylelint.apply(source, None, None, Map())(null)

      result should beEqualTo(expectedResult)
    }

    "Select all files in the source directory and below when no files are given" in {
      val workspace = createTemporaryWorkspaceWithMultipleFiles()
      val source = Source.Directory(workspace.pathAsString)
      val expectedResult = Try(
        List(
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test2.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test2.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test2.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test2.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/test3.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/test3.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/test3.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/test3.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9))))
      val configuration = createGivenPatternsWithIdentation(100)

      val result = Stylelint.apply(source, configuration, None, Map())(null)
      result should beEqualTo(expectedResult)
    }

    "Select only the given files in the source directory and below" in {
      val workspace = createTemporaryWorkspaceWithMultipleFiles()
      val source = Source.Directory(workspace.pathAsString)
      val expectedResult = Try(
        List(
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(6)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(7)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(8)),
          Result.Issue(
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"),
            Result.Message("Expected indentation of 100 spaces (indentation)"),
            Pattern.Id("indentation"),
            Source.Line(9))))
      val configuration = createGivenPatternsWithIdentation(100)

      val result = Stylelint.apply(
        source,
        configuration,
        Option(
          Set(
            Source.File(workspace.toJava.getCanonicalPath + "/test.css"),
            Source.File(workspace.toJava.getCanonicalPath + "/depth1/depth2/depth3/test4.css"))),
        Map())(null)
      result should beEqualTo(expectedResult)
    }
  }

  def createConfigWithIndentationRule(ident: Int, dir: File): File = {
    val rcText = "{\n  \"extends\": \"stylelint-config-standard\",\n  \"rules\": {\n    \"indentation\": " + ident.toString + "\n  }\n}"

    val file = File(dir.pathAsString + "/.stylelintrc").write(rcText)

    file
  }

  def createGivenPatternsWithIdentation(ident: Int): Option[List[Pattern.Definition]] = {
    val pattern = "indentation"
    val parameterdef = Option(Set(Parameter.Definition(Parameter.Name(pattern), Parameter.Value(ident.toString))))
    Option(List(Pattern.Definition(Pattern.Id(pattern), parameterdef)))
  }

  def createTemporaryWorkspace(): File = {
    val dir = File.newTemporaryDirectory()
    val testFilePath = "src/test/resources/codacy/stylelint/"
    val file = File(testFilePath + "test.css")

    file.copyToDirectory(dir)

    dir
  }

  def createTemporaryWorkspaceWithMultipleFiles(): File = {
    val dir = File.newTemporaryDirectory()
    val testFilePath = "src/test/resources/codacy/stylelint/"
    val file = File(testFilePath + "test.css")

    val file2 = file.copyToDirectory(dir)
    file2.renameTo("test2.css")
    file.copyToDirectory(dir)

    val depth1 = File(dir + "/depth1/").createIfNotExists(true)
    val depth2 = File(depth1 + "/depth2/").createIfNotExists(true)
    val depth3 = File(depth2 + "/depth3/").createIfNotExists(true)
    val file3 = file.copyToDirectory(depth1)
    file3.renameTo("test3.css")

    val file4 = file.copyToDirectory(depth3)
    file4.renameTo("test4.css")

    dir
  }
}
