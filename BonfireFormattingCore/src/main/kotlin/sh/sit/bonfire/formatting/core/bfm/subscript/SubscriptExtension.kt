package sh.sit.bonfire.formatting.core.bfm.subscript

import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

class SubscriptExtension : ParserExtension {
    override fun extend(parserBuilder: Parser.Builder) {
        parserBuilder.customDelimiterProcessor(SubscriptDelimiterProcessor())
    }
}
