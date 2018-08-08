package codacy.stylelint

import better.files.File
import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator

import scala.collection.mutable.ArrayBuffer

object ParseMarkupRule {

//  val defaultParameters = Map(
//    ("unit-blacklist",JsArray())
//  )

  private def findDescriptionAsString(root: Node): String = {
    val stringBuilder = new java.lang.StringBuilder()
    root.getChildIterator.next().getNext.getChars.appendTo(stringBuilder)
    stringBuilder.toString
  }


  private def toList(nodes: ReversiblePeekingIterator[Node]): List[Node] = {
    val nodesArrayBuffer = ArrayBuffer[Node](nodes.peek())
    while (nodes.hasNext) {
      nodes.next()
      nodesArrayBuffer.append(nodes.peek())
    }
    nodesArrayBuffer.toList
  }


  def parseForDescriptions(rule: File): String = {
    val parser = Parser.builder().build

    val document = parser.parse(rule.contentAsString)

    val descriptionstr = findDescriptionAsString(document)
    //System.out.println("Description: " + descriptionstr + "\n")

    descriptionstr
  }
}
