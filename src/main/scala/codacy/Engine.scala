package codacy

import codacy.stylelint.Stylelint
import com.codacy.tools.scala.seed.DockerEngine

object Engine extends DockerEngine(Stylelint)()
