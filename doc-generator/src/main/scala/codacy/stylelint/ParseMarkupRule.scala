package codacy.stylelint

import better.files.File
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.parser.Parser

object ParseMarkupRule {

  private def findDescriptionAsString(root: Node): String = {
    val firstLineOfDescription = root.getChildIterator.next().getNext.getChars.toString()
    val secondLineOfDescription = root.getChildIterator.next().getNext.getNext.getChars.toString()

    if (!isRuleDeprecationWarning(firstLineOfDescription)) {
      firstLineOfDescription
    } else {
      s"[Deprecated] $secondLineOfDescription"
    }
  }

  def parseForDescriptions(rule: File): String = {
    val parser = Parser.builder().build
    val document = parser.parse(rule.contentAsString)
    findDescriptionAsString(document)
  }

  private def isRuleDeprecationWarning(description: String): Boolean = {
    description.startsWith("> **Warning** This rule is deprecated and will be removed in the future.")
  }

}
