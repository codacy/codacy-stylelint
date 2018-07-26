package com.codacy.stylelint.documentation.parser

import better.files.File
import com.vladsch.flexmark.ast.util.TextCollectingVisitor
import com.vladsch.flexmark.ast.{Heading, Node}
import com.vladsch.flexmark.ext.toc.internal.TocUtils
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.profiles.pegdown.{Extensions, PegdownOptionsAdapter}
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator
import com.vladsch.flexmark.util.html.Escaping
import play.api.libs.json.{JsArray, Json}

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
