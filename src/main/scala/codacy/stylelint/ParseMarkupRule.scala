package codacy.stylelint

import better.files.File
import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.parser.Parser

object ParseMarkupRule {

  private def findDescriptionAsString(root: Node): String = {
    val stringBuilder = new java.lang.StringBuilder()
    root.getChildIterator.next().getNext.getChars.appendTo(stringBuilder)
    stringBuilder.toString
  }

  def parseForDescriptions(rule: File): String = {
    val parser = Parser.builder().build

    val document = parser.parse(rule.contentAsString)

    val descriptionstr = findDescriptionAsString(document)

    descriptionstr
  }
}
