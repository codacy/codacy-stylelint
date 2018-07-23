// creates a patterns.json File with all rules

import scala.sys.process.Process

import better.files.File

object DocGenerator {
  def main(args: Array[String]): Unit = {
    //tmpDirectory <- File.temporaryDirectory()
    val tmpDirectory = File("/tmp/clone")
    val version = "9.3.0" //for first test

    //cloneFromGitToTmpDir(tmpDirectory,version)

    val dir = tmpDirectory + "/lib/rules"

    //get non-documented rules
    val folders = getListOfSubDirectories(dir)
    folders.foreach(println)

    //get non-documented rules(not enclosed in folders)
    val okFileExtensions = List("js")
    val files = getListOfFiles(dir, okFileExtensions)
    files.foreach(println)
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
    File(directoryName)
      .walk(maxDepth = 1)
      .filter(_.isDirectory)
      .map(_.name)
      .toList
  }

  def getListOfFiles(directoryName: String,
                     extensions: List[String]): List[String] = {
    File(directoryName)
      .walk(maxDepth = 1)
      .filter(_.isRegularFile)
      .toList
      .filter(file => extensions.exists(file.name.endsWith(_)))
      .map(_.nameWithoutExtension)
  } //still returns index and shouldn't!!!! fix

}
