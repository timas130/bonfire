package sh.sit.bonfire.formatting.core.bfm.superscript

import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

class SuperscriptExtension : ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(SuperscriptDelimiterProcessor())
    }
}
